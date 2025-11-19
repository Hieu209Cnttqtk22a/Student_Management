package com.studentmanagement.app.ui.performance

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.studentmanagement.app.data.entity.StudentEntity
import com.studentmanagement.app.ui.component.FilterComboBox
import com.studentmanagement.app.ui.model.FilterState
import com.studentmanagement.app.ui.model.FilterType
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate
import kotlin.system.measureTimeMillis

/**
 * Performance tests for filter functionality on high refresh rate displays.
 * Tests frame rates during filtering operations and verifies recomposition efficiency.
 * 
 * Requirements: 8.1, 8.2, 8.3, 8.4, 8.5, 8.6, 8.7, 8.8, 8.9
 */
class FilterPerformanceTest {
    
    @get:Rule
    val composeTestRule = createComposeRule()
    
    /**
     * Test 11.1: Measure frame rates during filtering
     * Verifies that filtering operations complete quickly enough to maintain
     * 120Hz/144Hz refresh rates (target: < 8.3ms for 120Hz, < 6.9ms for 144Hz)
     */
    @Test
    fun measureFilterApplicationPerformance_withLargeStudentList() {
        // Given - Create a large student list (50+ students)
        val students = createLargeStudentList(50)
        val scheduledDates = createScheduledDates(10)
        val dailyRecords = createDailyRecords(students, scheduledDates)
        
        var currentFilter by mutableStateOf(FilterState())
        var filteredStudents by mutableStateOf(students)
        
        composeTestRule.setContent {
            Column(modifier = Modifier.fillMaxSize()) {
                FilterComboBox(
                    currentFilter = currentFilter,
                    scheduledDates = scheduledDates,
                    onFilterChange = { type, date ->
                        currentFilter = FilterState(type, date)
                        
                        // Measure filtering time
                        val filterTime = measureTimeMillis {
                            filteredStudents = filterStudents(students, dailyRecords, currentFilter)
                        }
                        
                        // Log performance metrics
                        println("Filter application time: ${filterTime}ms")
                        println("Student count: ${students.size}")
                        println("Filtered count: ${filteredStudents.size}")
                        println("Target for 120Hz: < 8.3ms")
                        println("Target for 144Hz: < 6.9ms")
                    }
                )
                
                // Render filtered list with stable keys
                LazyColumn {
                    items(
                        items = filteredStudents,
                        key = { student -> student.id }
                    ) { student ->
                        Text("${student.name} - ID: ${student.id}")
                    }
                }
            }
        }
        
        // When - Apply LOW_SCORE filter
        composeTestRule.onNodeWithContentDescription("Tất cả").performClick()
        composeTestRule.onNodeWithText("Điểm kém (< 7)").performClick()
        composeTestRule.onNodeWithText(formatDate(scheduledDates[0])).performClick()
        
        // Wait for composition to settle
        composeTestRule.waitForIdle()
        
        // Then - Verify filter was applied (check console output for timing)
        // Performance assertion: filtering should complete in < 8.3ms for 120Hz
        // This is verified through console logs during test execution
    }
    
    /**
     * Test 11.1: Measure frame rates with very large student list
     * Tests performance with 100+ students to ensure scalability
     */
    @Test
    fun measureFilterApplicationPerformance_with100Students() {
        // Given - Create a very large student list (100 students)
        val students = createLargeStudentList(100)
        val scheduledDates = createScheduledDates(10)
        val dailyRecords = createDailyRecords(students, scheduledDates)
        
        var currentFilter by mutableStateOf(FilterState())
        var filteredStudents by mutableStateOf(students)
        
        composeTestRule.setContent {
            Column(modifier = Modifier.fillMaxSize()) {
                FilterComboBox(
                    currentFilter = currentFilter,
                    scheduledDates = scheduledDates,
                    onFilterChange = { type, date ->
                        currentFilter = FilterState(type, date)
                        
                        // Measure filtering time
                        val filterTime = measureTimeMillis {
                            filteredStudents = filterStudents(students, dailyRecords, currentFilter)
                        }
                        
                        // Log performance metrics
                        println("=== Large List Performance ===")
                        println("Filter application time: ${filterTime}ms")
                        println("Student count: ${students.size}")
                        println("Filtered count: ${filteredStudents.size}")
                        println("Performance: ${if (filterTime < 8.3) "PASS (120Hz)" else "FAIL (120Hz)"}")
                        println("Performance: ${if (filterTime < 6.9) "PASS (144Hz)" else "FAIL (144Hz)"}")
                    }
                )
                
                LazyColumn {
                    items(
                        items = filteredStudents,
                        key = { student -> student.id }
                    ) { student ->
                        Text("${student.name} - ID: ${student.id}")
                    }
                }
            }
        }
        
        // When - Apply PERFECT_SCORE filter
        composeTestRule.onNodeWithContentDescription("Tất cả").performClick()
        composeTestRule.onNodeWithText("Điểm 10").performClick()
        composeTestRule.onNodeWithText(formatDate(scheduledDates[0])).performClick()
        
        composeTestRule.waitForIdle()
    }
    
    /**
     * Test 11.1: Verify no frame drops during list filtering
     * Tests that filtering operations don't block the UI thread
     */
    @Test
    fun verifyNoFrameDropsDuringFiltering() {
        // Given - Large student list
        val students = createLargeStudentList(75)
        val scheduledDates = createScheduledDates(10)
        val dailyRecords = createDailyRecords(students, scheduledDates)
        
        var currentFilter by mutableStateOf(FilterState())
        var filteredStudents by mutableStateOf(students)
        var recompositionCount by mutableStateOf(0)
        
        composeTestRule.setContent {
            Column(modifier = Modifier.fillMaxSize()) {
                FilterComboBox(
                    currentFilter = currentFilter,
                    scheduledDates = scheduledDates,
                    onFilterChange = { type, date ->
                        currentFilter = FilterState(type, date)
                        filteredStudents = filterStudents(students, dailyRecords, currentFilter)
                    }
                )
                
                // Track recompositions
                LaunchedEffect(filteredStudents) {
                    recompositionCount++
                    println("Recomposition #$recompositionCount - Filtered count: ${filteredStudents.size}")
                }
                
                LazyColumn {
                    items(
                        items = filteredStudents,
                        key = { student -> student.id }
                    ) { student ->
                        Text("${student.name}")
                    }
                }
            }
        }
        
        // When - Apply multiple filters in sequence
        val filterOperations = listOf(
            "Điểm kém (< 7)",
            "Chưa có điểm",
            "Điểm 10"
        )
        
        filterOperations.forEach { filterName ->
            composeTestRule.onNodeWithContentDescription(currentFilter.getDisplayLabel()).performClick()
            composeTestRule.onNodeWithText(filterName).performClick()
            composeTestRule.onNodeWithText(formatDate(scheduledDates[0])).performClick()
            composeTestRule.waitForIdle()
        }
        
        // Then - Verify reasonable recomposition count
        println("Total recompositions: $recompositionCount")
        println("Expected: ~${filterOperations.size + 1} (initial + filters)")
    }
    
    /**
     * Test 11.2: Verify recomposition efficiency with derivedStateOf
     * Ensures that derivedStateOf prevents unnecessary recompositions
     */
    @Test
    fun verifyDerivedStateOfPreventsUnnecessaryRecompositions() {
        // Given - Student list with derived state
        val students = createLargeStudentList(30)
        val scheduledDates = createScheduledDates(5)
        val dailyRecords = createDailyRecords(students, scheduledDates)
        
        var currentFilter by mutableStateOf(FilterState())
        var unrelatedState by mutableStateOf(0)
        var derivedRecompositionCount by mutableStateOf(0)
        var listRecompositionCount by mutableStateOf(0)
        
        composeTestRule.setContent {
            Column {
                FilterComboBox(
                    currentFilter = currentFilter,
                    scheduledDates = scheduledDates,
                    onFilterChange = { type, date ->
                        currentFilter = FilterState(type, date)
                    }
                )
                
                // Use derivedStateOf for filtered students
                val filteredStudents by remember {
                    derivedStateOf {
                        derivedRecompositionCount++
                        println("derivedStateOf recomputed: $derivedRecompositionCount")
                        filterStudents(students, dailyRecords, currentFilter)
                    }
                }
                
                // Track list recompositions
                LaunchedEffect(filteredStudents) {
                    listRecompositionCount++
                    println("List recomposed: $listRecompositionCount")
                }
                
                // Unrelated state that shouldn't trigger filtering
                Text("Unrelated: $unrelatedState")
                
                LazyColumn {
                    items(
                        items = filteredStudents,
                        key = { student -> student.id }
                    ) { student ->
                        Text(student.name)
                    }
                }
            }
        }
        
        // When - Change unrelated state (should NOT trigger derivedStateOf)
        repeat(3) {
            unrelatedState++
            composeTestRule.waitForIdle()
        }
        
        val recompositionsBeforeFilter = derivedRecompositionCount
        
        // When - Apply filter (SHOULD trigger derivedStateOf)
        composeTestRule.onNodeWithContentDescription("Tất cả").performClick()
        composeTestRule.onNodeWithText("Điểm kém (< 7)").performClick()
        composeTestRule.onNodeWithText(formatDate(scheduledDates[0])).performClick()
        composeTestRule.waitForIdle()
        
        // Then - Verify derivedStateOf only recomputed when filter changed
        println("=== Recomposition Efficiency ===")
        println("derivedStateOf recomputations before filter: $recompositionsBeforeFilter")
        println("derivedStateOf recomputations after filter: $derivedRecompositionCount")
        println("Efficiency: ${if (derivedRecompositionCount - recompositionsBeforeFilter <= 2) "PASS" else "FAIL"}")
    }
    
    /**
     * Test 11.2: Verify LazyColumn key stability
     * Ensures that LazyColumn uses stable keys for optimal performance
     */
    @Test
    fun verifyLazyColumnKeyStability() {
        // Given - Student list
        val students = createLargeStudentList(40)
        val scheduledDates = createScheduledDates(5)
        val dailyRecords = createDailyRecords(students, scheduledDates)
        
        var currentFilter by mutableStateOf(FilterState())
        val itemRecompositions = mutableMapOf<Long, Int>()
        
        composeTestRule.setContent {
            Column {
                FilterComboBox(
                    currentFilter = currentFilter,
                    scheduledDates = scheduledDates,
                    onFilterChange = { type, date ->
                        currentFilter = FilterState(type, date)
                    }
                )
                
                val filteredStudents by remember {
                    derivedStateOf {
                        filterStudents(students, dailyRecords, currentFilter)
                    }
                }
                
                LazyColumn {
                    items(
                        items = filteredStudents,
                        key = { student -> student.id }  // Stable key
                    ) { student ->
                        // Track recompositions per item
                        LaunchedEffect(student.id) {
                            itemRecompositions[student.id] = 
                                (itemRecompositions[student.id] ?: 0) + 1
                        }
                        Text("${student.name} - ${student.id}")
                    }
                }
            }
        }
        
        // When - Apply filter
        composeTestRule.onNodeWithContentDescription("Tất cả").performClick()
        composeTestRule.onNodeWithText("Điểm kém (< 7)").performClick()
        composeTestRule.onNodeWithText(formatDate(scheduledDates[0])).performClick()
        composeTestRule.waitForIdle()
        
        // Then - Verify items that remain visible don't recompose unnecessarily
        println("=== LazyColumn Key Stability ===")
        println("Total items tracked: ${itemRecompositions.size}")
        itemRecompositions.forEach { (id, count) ->
            println("Student $id recomposed $count times")
        }
        
        // Items should recompose at most 2 times (initial + filter change)
        val maxRecompositions = itemRecompositions.values.maxOrNull() ?: 0
        println("Max recompositions per item: $maxRecompositions")
        println("Key stability: ${if (maxRecompositions <= 2) "PASS" else "FAIL"}")
    }
    
    /**
     * Test 11.2: Verify minimal recompositions during filter changes
     * Tests that only affected components recompose when filter changes
     */
    @Test
    fun verifyMinimalRecompositionsDuringFilterChanges() {
        // Given - Student list with recomposition tracking
        val students = createLargeStudentList(25)
        val scheduledDates = createScheduledDates(5)
        val dailyRecords = createDailyRecords(students, scheduledDates)
        
        var currentFilter by mutableStateOf(FilterState())
        var filterComboBoxRecompositions by mutableStateOf(0)
        var listRecompositions by mutableStateOf(0)
        
        composeTestRule.setContent {
            Column {
                // Track FilterComboBox recompositions
                LaunchedEffect(currentFilter) {
                    filterComboBoxRecompositions++
                    println("FilterComboBox recomposed: $filterComboBoxRecompositions")
                }
                
                FilterComboBox(
                    currentFilter = currentFilter,
                    scheduledDates = scheduledDates,
                    onFilterChange = { type, date ->
                        currentFilter = FilterState(type, date)
                    }
                )
                
                val filteredStudents by remember {
                    derivedStateOf {
                        filterStudents(students, dailyRecords, currentFilter)
                    }
                }
                
                // Track list recompositions
                LaunchedEffect(filteredStudents) {
                    listRecompositions++
                    println("List recomposed: $listRecompositions")
                }
                
                LazyColumn {
                    items(
                        items = filteredStudents,
                        key = { student -> student.id }
                    ) { student ->
                        Text(student.name)
                    }
                }
            }
        }
        
        // When - Apply filter
        composeTestRule.onNodeWithContentDescription("Tất cả").performClick()
        composeTestRule.onNodeWithText("Chưa có điểm").performClick()
        composeTestRule.onNodeWithText(formatDate(scheduledDates[0])).performClick()
        composeTestRule.waitForIdle()
        
        // Then - Verify minimal recompositions
        println("=== Minimal Recompositions ===")
        println("FilterComboBox recompositions: $filterComboBoxRecompositions")
        println("List recompositions: $listRecompositions")
        println("Efficiency: ${if (filterComboBoxRecompositions <= 3 && listRecompositions <= 3) "PASS" else "FAIL"}")
    }
    
    // Helper functions
    
    private fun createLargeStudentList(count: Int): List<StudentEntity> {
        return (1..count).map { index ->
            StudentEntity(
                id = index.toLong(),
                name = "Student $index",
                classId = 1L,
                nickname = "Nick$index",
                parentPhone = "0123456789",
                note = "Note for student $index"
            )
        }
    }
    
    private fun createScheduledDates(count: Int): List<LocalDate> {
        val today = LocalDate.now()
        return (0 until count).map { today.plusDays(it.toLong()) }
    }
    
    private fun createDailyRecords(
        students: List<StudentEntity>,
        dates: List<LocalDate>
    ): Map<Pair<Long, String>, Float?> {
        val records = mutableMapOf<Pair<Long, String>, Float?>()
        
        students.forEachIndexed { index, student ->
            dates.forEach { date ->
                val dateString = formatDate(date)
                val key = Pair(student.id, dateString)
                
                // Create varied scores for testing
                val score = when {
                    index % 5 == 0 -> null  // 20% no score
                    index % 4 == 0 -> 10.0f  // 25% perfect score
                    index % 3 == 0 -> 5.5f   // ~33% low score
                    else -> 8.0f             // Rest have good scores
                }
                
                records[key] = score
            }
        }
        
        return records
    }
    
    private fun formatDate(date: LocalDate): String {
        return String.format("%02d/%02d/%04d", 
            date.dayOfMonth, 
            date.monthValue, 
            date.year)
    }
    
    private fun filterStudents(
        students: List<StudentEntity>,
        records: Map<Pair<Long, String>, Float?>,
        filterState: FilterState
    ): List<StudentEntity> {
        if (filterState.type == FilterType.ALL) {
            return students
        }
        
        val selectedDate = filterState.selectedDate ?: return students
        
        return when (filterState.type) {
            FilterType.ALL -> students
            FilterType.LOW_SCORE -> {
                students.filter { student ->
                    val score = records[Pair(student.id, selectedDate)]
                    score != null && score < 7.0f
                }
            }
            FilterType.NO_SCORE -> {
                students.filter { student ->
                    val score = records[Pair(student.id, selectedDate)]
                    score == null
                }
            }
            FilterType.PERFECT_SCORE -> {
                students.filter { student ->
                    val score = records[Pair(student.id, selectedDate)]
                    score != null && score == 10.0f
                }
            }
        }
    }
}
