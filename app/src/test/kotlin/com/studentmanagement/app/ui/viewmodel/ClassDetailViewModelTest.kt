package com.studentmanagement.app.ui.viewmodel

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import com.studentmanagement.app.data.entity.ClassEntity
import com.studentmanagement.app.data.entity.StudentEntity
import com.studentmanagement.app.data.repository.ClassRepository
import com.studentmanagement.app.data.repository.DailyRecordRepository
import com.studentmanagement.app.data.repository.StudentRepository
import io.mockk.every
import io.mockk.mockkStatic
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class ClassDetailViewModelTest {
    
    private lateinit var viewModel: ClassDetailViewModel
    private lateinit var classRepository: ClassRepository
    private lateinit var studentRepository: StudentRepository
    private lateinit var dailyRecordRepository: DailyRecordRepository
    
    private val testDispatcher = StandardTestDispatcher()
    
    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        
        // Mock Android Log class
        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0
        every { Log.e(any(), any()) } returns 0
        every { Log.e(any(), any(), any()) } returns 0
        
        classRepository = mock()
        studentRepository = mock()
        dailyRecordRepository = mock()
        
        viewModel = ClassDetailViewModel(
            classRepository = classRepository,
            studentRepository = studentRepository,
            dailyRecordRepository = dailyRecordRepository,
            savedStateHandle = SavedStateHandle()
        )
    }
    
    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }
    
    @Test
    fun `loadClassDetail with single weekday schedule calculates correct dates`() = runTest {
        // Given: A class with Monday only schedule for 1 week
        val startDate = LocalDate.of(2024, 1, 1) // Monday, January 1, 2024
        val classEntity = ClassEntity(
            id = 1L,
            name = "Math 101",
            scheduleDaysOfWeek = "[2]", // Monday (UI format: 1=Sunday, 2=Monday)
            startTimeMinutes = 480,
            repeatInterval = 1,
            repeatUnit = "WEEK",
            createdAt = startDate.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
        )
        
        whenever(classRepository.getClassById(1L)).thenReturn(classEntity)
        whenever(studentRepository.getStudentsByClass(1L)).thenReturn(flowOf(emptyList()))
        whenever(dailyRecordRepository.getRecordsByClassAndDate(any(), any())).thenReturn(flowOf(emptyList()))
        
        // When
        viewModel.loadClassDetail(1L)
        advanceUntilIdle()
        
        // Then: Should have Mondays within the 1 week period
        val uiState = viewModel.uiState.value
        assertTrue("UI state should be Success", uiState is ClassDetailUiState.Success)
        val scheduledDates = (uiState as ClassDetailUiState.Success).scheduledDates
        
        assertTrue("Should have at least one Monday", scheduledDates.isNotEmpty())
        assertTrue("First date should be Jan 1", scheduledDates.first() == LocalDate.of(2024, 1, 1))
        
        // All dates should be Mondays
        scheduledDates.forEach { date ->
            assertEquals("All dates should be Mondays", 1, date.dayOfWeek.value) // 1 = Monday in LocalDate
        }
    }
    
    @Test
    fun `loadClassDetail with multiple weekdays schedule calculates correct dates`() = runTest {
        // Given: A class with Monday, Wednesday, Friday schedule for 1 week
        val startDate = LocalDate.of(2024, 1, 1) // Monday, January 1, 2024
        val classEntity = ClassEntity(
            id = 1L,
            name = "Math 101",
            scheduleDaysOfWeek = "[2,4,6]", // Monday, Wednesday, Friday
            startTimeMinutes = 480,
            repeatInterval = 1,
            repeatUnit = "WEEK",
            createdAt = startDate.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
        )
        
        whenever(classRepository.getClassById(1L)).thenReturn(classEntity)
        whenever(studentRepository.getStudentsByClass(1L)).thenReturn(flowOf(emptyList()))
        whenever(dailyRecordRepository.getRecordsByClassAndDate(any(), any())).thenReturn(flowOf(emptyList()))
        
        // When
        viewModel.loadClassDetail(1L)
        advanceUntilIdle()
        
        // Then: Should have Mon, Wed, Fri dates
        val uiState = viewModel.uiState.value
        assertTrue("UI state should be Success", uiState is ClassDetailUiState.Success)
        val scheduledDates = (uiState as ClassDetailUiState.Success).scheduledDates
        
        assertTrue("Should have multiple dates", scheduledDates.size >= 3)
        
        // Verify first date is Monday Jan 1
        assertEquals("First date should be Monday Jan 1", LocalDate.of(2024, 1, 1), scheduledDates.first())
        
        // All dates should be Monday (1), Wednesday (3), or Friday (5)
        scheduledDates.forEach { date ->
            val dayOfWeek = date.dayOfWeek.value
            assertTrue("Date should be Mon, Wed, or Fri", 
                dayOfWeek == 1 || dayOfWeek == 3 || dayOfWeek == 5)
        }
    }
    
    @Test
    fun `loadClassDetail with WEEK repeatUnit calculates correct end date`() = runTest {
        // Given: A class with 4 weeks duration
        val startDate = LocalDate.of(2024, 1, 1)
        val classEntity = ClassEntity(
            id = 1L,
            name = "Math 101",
            scheduleDaysOfWeek = "[2]", // Monday
            startTimeMinutes = 480,
            repeatInterval = 4,
            repeatUnit = "WEEK",
            createdAt = startDate.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
        )
        
        whenever(classRepository.getClassById(1L)).thenReturn(classEntity)
        whenever(studentRepository.getStudentsByClass(1L)).thenReturn(flowOf(emptyList()))
        whenever(dailyRecordRepository.getRecordsByClassAndDate(any(), any())).thenReturn(flowOf(emptyList()))
        
        // When
        viewModel.loadClassDetail(1L)
        advanceUntilIdle()
        
        // Then: Should have Mondays within 4 week period
        val uiState = viewModel.uiState.value
        assertTrue("UI state should be Success", uiState is ClassDetailUiState.Success)
        val scheduledDates = (uiState as ClassDetailUiState.Success).scheduledDates
        
        assertTrue("Should have multiple Mondays", scheduledDates.size >= 4)
        assertEquals("First date should be Jan 1", LocalDate.of(2024, 1, 1), scheduledDates.first())
        
        // Last date should be within 4 weeks from start
        val expectedEndDate = startDate.plusWeeks(4)
        assertTrue("Last date should be within 4 weeks", 
            !scheduledDates.last().isAfter(expectedEndDate))
    }
    
    @Test
    fun `loadClassDetail with MONTH repeatUnit calculates correct end date`() = runTest {
        // Given: A class with 2 months duration
        val startDate = LocalDate.of(2024, 1, 1)
        val classEntity = ClassEntity(
            id = 1L,
            name = "Math 101",
            scheduleDaysOfWeek = "[2]", // Monday
            startTimeMinutes = 480,
            repeatInterval = 2,
            repeatUnit = "MONTH",
            createdAt = startDate.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
        )
        
        whenever(classRepository.getClassById(1L)).thenReturn(classEntity)
        whenever(studentRepository.getStudentsByClass(1L)).thenReturn(flowOf(emptyList()))
        whenever(dailyRecordRepository.getRecordsByClassAndDate(any(), any())).thenReturn(flowOf(emptyList()))
        
        // When
        viewModel.loadClassDetail(1L)
        advanceUntilIdle()
        
        // Then: Should have all Mondays from Jan 1 to Mar 1 (2 months)
        val uiState = viewModel.uiState.value
        assertTrue("UI state should be Success", uiState is ClassDetailUiState.Success)
        val scheduledDates = (uiState as ClassDetailUiState.Success).scheduledDates
        
        assertTrue("Should have multiple Mondays", scheduledDates.size >= 8)
        assertEquals("First date should be Jan 1", LocalDate.of(2024, 1, 1), scheduledDates.first())
        
        // Last date should be within 2 months from start
        val expectedEndDate = startDate.plusMonths(2)
        assertTrue("Last date should be within 2 months", 
            !scheduledDates.last().isAfter(expectedEndDate))
    }
    
    @Test
    fun `loadClassDetail with YEAR repeatUnit calculates correct end date`() = runTest {
        // Given: A class with 1 year duration
        val startDate = LocalDate.of(2024, 1, 1)
        val classEntity = ClassEntity(
            id = 1L,
            name = "Math 101",
            scheduleDaysOfWeek = "[2]", // Monday
            startTimeMinutes = 480,
            repeatInterval = 1,
            repeatUnit = "YEAR",
            createdAt = startDate.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
        )
        
        whenever(classRepository.getClassById(1L)).thenReturn(classEntity)
        whenever(studentRepository.getStudentsByClass(1L)).thenReturn(flowOf(emptyList()))
        whenever(dailyRecordRepository.getRecordsByClassAndDate(any(), any())).thenReturn(flowOf(emptyList()))
        
        // When
        viewModel.loadClassDetail(1L)
        advanceUntilIdle()
        
        // Then: Should have many Mondays throughout the year
        val uiState = viewModel.uiState.value
        assertTrue("UI state should be Success", uiState is ClassDetailUiState.Success)
        val scheduledDates = (uiState as ClassDetailUiState.Success).scheduledDates
        
        assertTrue("Should have many Mondays in a year", scheduledDates.size >= 52)
        assertEquals("First date should be Jan 1", LocalDate.of(2024, 1, 1), scheduledDates.first())
        
        // Last date should be within 1 year from start
        val expectedEndDate = startDate.plusYears(1)
        assertTrue("Last date should be within 1 year", 
            !scheduledDates.last().isAfter(expectedEndDate))
    }
    
    @Test
    fun `loadClassDetail with start date not on scheduled day includes first matching date`() = runTest {
        // Given: Class created on Tuesday (Jan 2) but scheduled for Mondays
        val startDate = LocalDate.of(2024, 1, 2) // Tuesday
        val classEntity = ClassEntity(
            id = 1L,
            name = "Math 101",
            scheduleDaysOfWeek = "[2]", // Monday
            startTimeMinutes = 480,
            repeatInterval = 1,
            repeatUnit = "WEEK",
            createdAt = startDate.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
        )
        
        whenever(classRepository.getClassById(1L)).thenReturn(classEntity)
        whenever(studentRepository.getStudentsByClass(1L)).thenReturn(flowOf(emptyList()))
        whenever(dailyRecordRepository.getRecordsByClassAndDate(any(), any())).thenReturn(flowOf(emptyList()))
        
        // When
        viewModel.loadClassDetail(1L)
        advanceUntilIdle()
        
        // Then: Should start from next Monday (Jan 8)
        val uiState = viewModel.uiState.value
        assertTrue("UI state should be Success", uiState is ClassDetailUiState.Success)
        val scheduledDates = (uiState as ClassDetailUiState.Success).scheduledDates
        
        assertTrue("Should have at least one Monday", scheduledDates.isNotEmpty())
        
        // First date should be a Monday after the start date
        val firstDate = scheduledDates.first()
        assertEquals("First date should be a Monday", 1, firstDate.dayOfWeek.value)
        assertTrue("First date should be after start date", firstDate.isAfter(startDate) || firstDate.isEqual(startDate))
    }
    
    @Test
    fun `loadClassDetail with empty scheduleDaysOfWeek returns empty dates list`() = runTest {
        // Given: A class with no scheduled days
        val startDate = LocalDate.of(2024, 1, 1)
        val classEntity = ClassEntity(
            id = 1L,
            name = "Math 101",
            scheduleDaysOfWeek = "[]", // Empty
            startTimeMinutes = 480,
            repeatInterval = 1,
            repeatUnit = "WEEK",
            createdAt = startDate.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
        )
        
        whenever(classRepository.getClassById(1L)).thenReturn(classEntity)
        whenever(studentRepository.getStudentsByClass(1L)).thenReturn(flowOf(emptyList()))
        
        // When
        viewModel.loadClassDetail(1L)
        advanceUntilIdle()
        
        // Then: Should have empty scheduled dates
        val uiState = viewModel.uiState.value
        assertTrue(uiState is ClassDetailUiState.Success)
        val scheduledDates = (uiState as ClassDetailUiState.Success).scheduledDates
        
        assertTrue("Should have no scheduled dates", scheduledDates.isEmpty())
    }
    
    @Test
    fun `loadClassDetail with invalid scheduleDaysOfWeek JSON returns empty dates list`() = runTest {
        // Given: A class with invalid JSON
        val startDate = LocalDate.of(2024, 1, 1)
        val classEntity = ClassEntity(
            id = 1L,
            name = "Math 101",
            scheduleDaysOfWeek = "invalid json", // Invalid
            startTimeMinutes = 480,
            repeatInterval = 1,
            repeatUnit = "WEEK",
            createdAt = startDate.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
        )
        
        whenever(classRepository.getClassById(1L)).thenReturn(classEntity)
        whenever(studentRepository.getStudentsByClass(1L)).thenReturn(flowOf(emptyList()))
        
        // When
        viewModel.loadClassDetail(1L)
        advanceUntilIdle()
        
        // Then: Should handle gracefully with empty dates
        val uiState = viewModel.uiState.value
        assertTrue(uiState is ClassDetailUiState.Success)
        val scheduledDates = (uiState as ClassDetailUiState.Success).scheduledDates
        
        assertTrue("Should have no scheduled dates", scheduledDates.isEmpty())
    }
    
    @Test
    fun `loadClassDetail returns dates in chronological order`() = runTest {
        // Given: A class with multiple days in random order
        val startDate = LocalDate.of(2024, 1, 1)
        val classEntity = ClassEntity(
            id = 1L,
            name = "Math 101",
            scheduleDaysOfWeek = "[6,2,4]", // Friday, Monday, Wednesday (unsorted)
            startTimeMinutes = 480,
            repeatInterval = 1,
            repeatUnit = "WEEK",
            createdAt = startDate.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
        )
        
        whenever(classRepository.getClassById(1L)).thenReturn(classEntity)
        whenever(studentRepository.getStudentsByClass(1L)).thenReturn(flowOf(emptyList()))
        whenever(dailyRecordRepository.getRecordsByClassAndDate(any(), any())).thenReturn(flowOf(emptyList()))
        
        // When
        viewModel.loadClassDetail(1L)
        advanceUntilIdle()
        
        // Then: Dates should be sorted chronologically
        val uiState = viewModel.uiState.value
        assertTrue("UI state should be Success", uiState is ClassDetailUiState.Success)
        val scheduledDates = (uiState as ClassDetailUiState.Success).scheduledDates
        
        assertTrue("Should have multiple dates", scheduledDates.size >= 3)
        
        // Verify dates are sorted
        for (i in 0 until scheduledDates.size - 1) {
            assertTrue("Dates should be in chronological order at index $i",
                scheduledDates[i].isBefore(scheduledDates[i + 1]))
        }
    }
    
    // Filter Tests
    
    @Test
    fun `setFilter updates filter state correctly`() = runTest {
        // Given
        val classEntity = ClassEntity(
            id = 1L,
            name = "Math 101",
            scheduleDaysOfWeek = "[2]",
            startTimeMinutes = 480,
            repeatInterval = 1,
            repeatUnit = "WEEK",
            createdAt = LocalDate.of(2024, 1, 1).atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
        )
        
        whenever(classRepository.getClassById(1L)).thenReturn(classEntity)
        whenever(studentRepository.getStudentsByClass(1L)).thenReturn(flowOf(emptyList()))
        whenever(dailyRecordRepository.getRecordsByClassAndDate(any(), any())).thenReturn(flowOf(emptyList()))
        
        viewModel.loadClassDetail(1L)
        advanceUntilIdle()
        
        // When
        viewModel.setFilter(com.studentmanagement.app.ui.model.FilterType.LOW_SCORE, "15/11/2025")
        advanceUntilIdle()
        
        // Then
        val filterState = viewModel.filterState.value
        assertEquals(com.studentmanagement.app.ui.model.FilterType.LOW_SCORE, filterState.type)
        assertEquals("15/11/2025", filterState.selectedDate)
        assertTrue(filterState.isActive())
    }
    
    @Test
    fun `resetFilter clears filter state`() = runTest {
        // Given
        val classEntity = ClassEntity(
            id = 1L,
            name = "Math 101",
            scheduleDaysOfWeek = "[2]",
            startTimeMinutes = 480,
            repeatInterval = 1,
            repeatUnit = "WEEK",
            createdAt = LocalDate.of(2024, 1, 1).atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
        )
        
        whenever(classRepository.getClassById(1L)).thenReturn(classEntity)
        whenever(studentRepository.getStudentsByClass(1L)).thenReturn(flowOf(emptyList()))
        whenever(dailyRecordRepository.getRecordsByClassAndDate(any(), any())).thenReturn(flowOf(emptyList()))
        
        viewModel.loadClassDetail(1L)
        advanceUntilIdle()
        
        // Set a filter first
        viewModel.setFilter(com.studentmanagement.app.ui.model.FilterType.LOW_SCORE, "15/11/2025")
        advanceUntilIdle()
        
        // When
        viewModel.resetFilter()
        advanceUntilIdle()
        
        // Then
        val filterState = viewModel.filterState.value
        assertEquals(com.studentmanagement.app.ui.model.FilterType.ALL, filterState.type)
        assertNull(filterState.selectedDate)
        assertFalse(filterState.isActive())
    }
    
    @Test
    fun `filterStudents with LOW_SCORE filter shows only students with scores less than 7`() = runTest {
        // Given - Use a date that matches the class schedule (Monday, Jan 1, 2024)
        val testDate = LocalDate.of(2024, 1, 1) // Monday
        val classEntity = ClassEntity(
            id = 1L,
            name = "Math 101",
            scheduleDaysOfWeek = "[2]", // Monday
            startTimeMinutes = 480,
            repeatInterval = 1,
            repeatUnit = "WEEK",
            createdAt = testDate.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
        )
        
        val students = listOf(
            StudentEntity(id = 1L, name = "Student 1", classId = 1L),
            StudentEntity(id = 2L, name = "Student 2", classId = 1L),
            StudentEntity(id = 3L, name = "Student 3", classId = 1L),
            StudentEntity(id = 4L, name = "Student 4", classId = 1L)
        )
        
        // Mock daily records for the test date
        val mockRecords = listOf(
            com.studentmanagement.app.data.entity.DailyRecordEntity(
                id = 1L, studentId = 1L, classId = 1L, date = "2024-01-01", score = 5.0f
            ),
            com.studentmanagement.app.data.entity.DailyRecordEntity(
                id = 2L, studentId = 2L, classId = 1L, date = "2024-01-01", score = 8.0f
            ),
            com.studentmanagement.app.data.entity.DailyRecordEntity(
                id = 3L, studentId = 3L, classId = 1L, date = "2024-01-01", score = 6.5f
            )
        )
        
        whenever(classRepository.getClassById(1L)).thenReturn(classEntity)
        whenever(studentRepository.getStudentsByClass(1L)).thenReturn(flowOf(students))
        whenever(dailyRecordRepository.getRecordsByClassAndDate(any(), any()))
            .thenReturn(flowOf(emptyList()))
        whenever(dailyRecordRepository.getRecordsByClassAndDate(1L, "2024-01-01"))
            .thenReturn(flowOf(mockRecords))
        
        viewModel.loadClassDetail(1L)
        advanceUntilIdle()
        
        // When - Use dd/MM/yyyy format for the filter
        viewModel.setFilter(com.studentmanagement.app.ui.model.FilterType.LOW_SCORE, "01/01/2024")
        advanceUntilIdle()
        
        // Then
        val filteredStudents = viewModel.filteredStudents.value
        assertEquals(2, filteredStudents.size)
        assertTrue(filteredStudents.any { it.id == 1L })
        assertTrue(filteredStudents.any { it.id == 3L })
    }
    
    @Test
    fun `filterStudents with NO_SCORE filter shows only students with null scores`() = runTest {
        // Given - Use a date that matches the class schedule
        val testDate = LocalDate.of(2024, 1, 1) // Monday
        val classEntity = ClassEntity(
            id = 1L,
            name = "Math 101",
            scheduleDaysOfWeek = "[2]", // Monday
            startTimeMinutes = 480,
            repeatInterval = 1,
            repeatUnit = "WEEK",
            createdAt = testDate.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
        )
        
        val students = listOf(
            StudentEntity(id = 1L, name = "Student 1", classId = 1L),
            StudentEntity(id = 2L, name = "Student 2", classId = 1L),
            StudentEntity(id = 3L, name = "Student 3", classId = 1L)
        )
        
        // Mock daily records - only student 1 has a score
        val mockRecords = listOf(
            com.studentmanagement.app.data.entity.DailyRecordEntity(
                id = 1L, studentId = 1L, classId = 1L, date = "2024-01-01", score = 5.0f
            )
        )
        
        whenever(classRepository.getClassById(1L)).thenReturn(classEntity)
        whenever(studentRepository.getStudentsByClass(1L)).thenReturn(flowOf(students))
        whenever(dailyRecordRepository.getRecordsByClassAndDate(any(), any()))
            .thenReturn(flowOf(emptyList()))
        whenever(dailyRecordRepository.getRecordsByClassAndDate(1L, "2024-01-01"))
            .thenReturn(flowOf(mockRecords))
        
        viewModel.loadClassDetail(1L)
        advanceUntilIdle()
        
        // When
        viewModel.setFilter(com.studentmanagement.app.ui.model.FilterType.NO_SCORE, "01/01/2024")
        advanceUntilIdle()
        
        // Then
        val filteredStudents = viewModel.filteredStudents.value
        assertEquals(2, filteredStudents.size)
        assertTrue(filteredStudents.any { it.id == 2L })
        assertTrue(filteredStudents.any { it.id == 3L })
    }
    
    @Test
    fun `filterStudents with PERFECT_SCORE filter shows only students with score 10`() = runTest {
        // Given - Use a date that matches the class schedule
        val testDate = LocalDate.of(2024, 1, 1) // Monday
        val classEntity = ClassEntity(
            id = 1L,
            name = "Math 101",
            scheduleDaysOfWeek = "[2]", // Monday
            startTimeMinutes = 480,
            repeatInterval = 1,
            repeatUnit = "WEEK",
            createdAt = testDate.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
        )
        
        val students = listOf(
            StudentEntity(id = 1L, name = "Student 1", classId = 1L),
            StudentEntity(id = 2L, name = "Student 2", classId = 1L),
            StudentEntity(id = 3L, name = "Student 3", classId = 1L),
            StudentEntity(id = 4L, name = "Student 4", classId = 1L)
        )
        
        // Mock daily records
        val mockRecords = listOf(
            com.studentmanagement.app.data.entity.DailyRecordEntity(
                id = 1L, studentId = 1L, classId = 1L, date = "2024-01-01", score = 10.0f
            ),
            com.studentmanagement.app.data.entity.DailyRecordEntity(
                id = 2L, studentId = 2L, classId = 1L, date = "2024-01-01", score = 9.5f
            ),
            com.studentmanagement.app.data.entity.DailyRecordEntity(
                id = 3L, studentId = 3L, classId = 1L, date = "2024-01-01", score = 10.0f
            )
        )
        
        whenever(classRepository.getClassById(1L)).thenReturn(classEntity)
        whenever(studentRepository.getStudentsByClass(1L)).thenReturn(flowOf(students))
        whenever(dailyRecordRepository.getRecordsByClassAndDate(any(), any()))
            .thenReturn(flowOf(emptyList()))
        whenever(dailyRecordRepository.getRecordsByClassAndDate(1L, "2024-01-01"))
            .thenReturn(flowOf(mockRecords))
        
        viewModel.loadClassDetail(1L)
        advanceUntilIdle()
        
        // When
        viewModel.setFilter(com.studentmanagement.app.ui.model.FilterType.PERFECT_SCORE, "01/01/2024")
        advanceUntilIdle()
        
        // Then
        val filteredStudents = viewModel.filteredStudents.value
        assertEquals(2, filteredStudents.size)
        assertTrue(filteredStudents.any { it.id == 1L })
        assertTrue(filteredStudents.any { it.id == 3L })
    }
    
    @Test
    fun `filterStudents with ALL filter shows all students`() = runTest {
        // Given
        val classEntity = ClassEntity(
            id = 1L,
            name = "Math 101",
            scheduleDaysOfWeek = "[2]",
            startTimeMinutes = 480,
            repeatInterval = 1,
            repeatUnit = "WEEK",
            createdAt = LocalDate.of(2024, 1, 1).atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
        )
        
        val students = listOf(
            StudentEntity(id = 1L, name = "Student 1", classId = 1L),
            StudentEntity(id = 2L, name = "Student 2", classId = 1L),
            StudentEntity(id = 3L, name = "Student 3", classId = 1L)
        )
        
        whenever(classRepository.getClassById(1L)).thenReturn(classEntity)
        whenever(studentRepository.getStudentsByClass(1L)).thenReturn(flowOf(students))
        whenever(dailyRecordRepository.getRecordsByClassAndDate(any(), any())).thenReturn(flowOf(emptyList()))
        
        viewModel.loadClassDetail(1L)
        advanceUntilIdle()
        
        // When
        viewModel.setFilter(com.studentmanagement.app.ui.model.FilterType.ALL, null)
        advanceUntilIdle()
        
        // Then
        val filteredStudents = viewModel.filteredStudents.value
        assertEquals(3, filteredStudents.size)
    }
    
    @Test
    fun `filterStudents with empty student list returns empty list`() = runTest {
        // Given
        val classEntity = ClassEntity(
            id = 1L,
            name = "Math 101",
            scheduleDaysOfWeek = "[2]",
            startTimeMinutes = 480,
            repeatInterval = 1,
            repeatUnit = "WEEK",
            createdAt = LocalDate.of(2024, 1, 1).atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
        )
        
        whenever(classRepository.getClassById(1L)).thenReturn(classEntity)
        whenever(studentRepository.getStudentsByClass(1L)).thenReturn(flowOf(emptyList()))
        whenever(dailyRecordRepository.getRecordsByClassAndDate(any(), any())).thenReturn(flowOf(emptyList()))
        
        viewModel.loadClassDetail(1L)
        advanceUntilIdle()
        
        // When
        viewModel.setFilter(com.studentmanagement.app.ui.model.FilterType.LOW_SCORE, "15/11/2025")
        advanceUntilIdle()
        
        // Then
        val filteredStudents = viewModel.filteredStudents.value
        assertTrue(filteredStudents.isEmpty())
    }
    
    @Test
    fun `filterStudents with no daily records returns empty list for date-specific filters`() = runTest {
        // Given
        val classEntity = ClassEntity(
            id = 1L,
            name = "Math 101",
            scheduleDaysOfWeek = "[2]",
            startTimeMinutes = 480,
            repeatInterval = 1,
            repeatUnit = "WEEK",
            createdAt = LocalDate.of(2024, 1, 1).atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
        )
        
        val students = listOf(
            StudentEntity(id = 1L, name = "Student 1", classId = 1L),
            StudentEntity(id = 2L, name = "Student 2", classId = 1L)
        )
        
        whenever(classRepository.getClassById(1L)).thenReturn(classEntity)
        whenever(studentRepository.getStudentsByClass(1L)).thenReturn(flowOf(students))
        whenever(dailyRecordRepository.getRecordsByClassAndDate(any(), any())).thenReturn(flowOf(emptyList()))
        
        viewModel.loadClassDetail(1L)
        advanceUntilIdle()
        
        // When - filter for LOW_SCORE but no records exist
        viewModel.setFilter(com.studentmanagement.app.ui.model.FilterType.LOW_SCORE, "15/11/2025")
        advanceUntilIdle()
        
        // Then
        val filteredStudents = viewModel.filteredStudents.value
        assertTrue(filteredStudents.isEmpty())
    }
    
    @Test
    fun `filterStudents with boundary score 7_0 is not included in LOW_SCORE filter`() = runTest {
        // Given - Use a date that matches the class schedule
        val testDate = LocalDate.of(2024, 1, 1) // Monday
        val classEntity = ClassEntity(
            id = 1L,
            name = "Math 101",
            scheduleDaysOfWeek = "[2]", // Monday
            startTimeMinutes = 480,
            repeatInterval = 1,
            repeatUnit = "WEEK",
            createdAt = testDate.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
        )
        
        val students = listOf(
            StudentEntity(id = 1L, name = "Student 1", classId = 1L),
            StudentEntity(id = 2L, name = "Student 2", classId = 1L),
            StudentEntity(id = 3L, name = "Student 3", classId = 1L)
        )
        
        // Mock daily records with boundary values
        val mockRecords = listOf(
            com.studentmanagement.app.data.entity.DailyRecordEntity(
                id = 1L, studentId = 1L, classId = 1L, date = "2024-01-01", score = 6.9f
            ),
            com.studentmanagement.app.data.entity.DailyRecordEntity(
                id = 2L, studentId = 2L, classId = 1L, date = "2024-01-01", score = 7.0f
            ),
            com.studentmanagement.app.data.entity.DailyRecordEntity(
                id = 3L, studentId = 3L, classId = 1L, date = "2024-01-01", score = 7.1f
            )
        )
        
        whenever(classRepository.getClassById(1L)).thenReturn(classEntity)
        whenever(studentRepository.getStudentsByClass(1L)).thenReturn(flowOf(students))
        whenever(dailyRecordRepository.getRecordsByClassAndDate(any(), any()))
            .thenReturn(flowOf(emptyList()))
        whenever(dailyRecordRepository.getRecordsByClassAndDate(1L, "2024-01-01"))
            .thenReturn(flowOf(mockRecords))
        
        viewModel.loadClassDetail(1L)
        advanceUntilIdle()
        
        // When
        viewModel.setFilter(com.studentmanagement.app.ui.model.FilterType.LOW_SCORE, "01/01/2024")
        advanceUntilIdle()
        
        // Then - only student with 6.9 should be included
        val filteredStudents = viewModel.filteredStudents.value
        assertEquals(1, filteredStudents.size)
        assertTrue(filteredStudents.any { it.id == 1L })
    }
    
    // Filter State Persistence Tests
    
    @Test
    fun `SavedStateHandle saves filter state when setFilter is called`() = runTest {
        // Given
        val savedStateHandle = SavedStateHandle()
        val viewModelWithSavedState = ClassDetailViewModel(
            classRepository = classRepository,
            studentRepository = studentRepository,
            dailyRecordRepository = dailyRecordRepository,
            savedStateHandle = savedStateHandle
        )
        
        val classEntity = ClassEntity(
            id = 1L,
            name = "Math 101",
            scheduleDaysOfWeek = "[2]",
            startTimeMinutes = 480,
            repeatInterval = 1,
            repeatUnit = "WEEK",
            createdAt = LocalDate.of(2024, 1, 1).atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
        )
        
        whenever(classRepository.getClassById(1L)).thenReturn(classEntity)
        whenever(studentRepository.getStudentsByClass(1L)).thenReturn(flowOf(emptyList()))
        whenever(dailyRecordRepository.getRecordsByClassAndDate(any(), any())).thenReturn(flowOf(emptyList()))
        
        viewModelWithSavedState.loadClassDetail(1L)
        advanceUntilIdle()
        
        // When
        viewModelWithSavedState.setFilter(com.studentmanagement.app.ui.model.FilterType.LOW_SCORE, "15/11/2025")
        advanceUntilIdle()
        
        // Then
        assertEquals("LOW_SCORE", savedStateHandle.get<String>("filter_type"))
        assertEquals("15/11/2025", savedStateHandle.get<String>("filter_date"))
    }
    
    @Test
    fun `SavedStateHandle restores filter state when ViewModel is recreated`() = runTest {
        // Given - Create a SavedStateHandle with saved filter state
        val savedStateHandle = SavedStateHandle()
        savedStateHandle["filter_type"] = "PERFECT_SCORE"
        savedStateHandle["filter_date"] = "20/11/2025"
        
        // When - Create a new ViewModel with the saved state
        val viewModelWithRestoredState = ClassDetailViewModel(
            classRepository = classRepository,
            studentRepository = studentRepository,
            dailyRecordRepository = dailyRecordRepository,
            savedStateHandle = savedStateHandle
        )
        
        // Then - Filter state should be restored
        val filterState = viewModelWithRestoredState.filterState.value
        assertEquals(com.studentmanagement.app.ui.model.FilterType.PERFECT_SCORE, filterState.type)
        assertEquals("20/11/2025", filterState.selectedDate)
        assertTrue(filterState.isActive())
    }
    
    @Test
    fun `SavedStateHandle restores default filter state when no saved state exists`() = runTest {
        // Given - Create a SavedStateHandle with no saved filter state
        val savedStateHandle = SavedStateHandle()
        
        // When - Create a new ViewModel
        val viewModelWithDefaultState = ClassDetailViewModel(
            classRepository = classRepository,
            studentRepository = studentRepository,
            dailyRecordRepository = dailyRecordRepository,
            savedStateHandle = savedStateHandle
        )
        
        // Then - Filter state should be default (ALL, no date)
        val filterState = viewModelWithDefaultState.filterState.value
        assertEquals(com.studentmanagement.app.ui.model.FilterType.ALL, filterState.type)
        assertNull(filterState.selectedDate)
        assertFalse(filterState.isActive())
    }
    
    @Test
    fun `SavedStateHandle handles invalid filter type gracefully`() = runTest {
        // Given - Create a SavedStateHandle with invalid filter type
        val savedStateHandle = SavedStateHandle()
        savedStateHandle["filter_type"] = "INVALID_TYPE"
        savedStateHandle["filter_date"] = "15/11/2025"
        
        // When - Create a new ViewModel
        val viewModelWithInvalidState = ClassDetailViewModel(
            classRepository = classRepository,
            studentRepository = studentRepository,
            dailyRecordRepository = dailyRecordRepository,
            savedStateHandle = savedStateHandle
        )
        
        // Then - Should default to ALL filter
        val filterState = viewModelWithInvalidState.filterState.value
        assertEquals(com.studentmanagement.app.ui.model.FilterType.ALL, filterState.type)
    }
    
    @Test
    fun `resetFilter clears saved state in SavedStateHandle`() = runTest {
        // Given
        val savedStateHandle = SavedStateHandle()
        val viewModelWithSavedState = ClassDetailViewModel(
            classRepository = classRepository,
            studentRepository = studentRepository,
            dailyRecordRepository = dailyRecordRepository,
            savedStateHandle = savedStateHandle
        )
        
        val classEntity = ClassEntity(
            id = 1L,
            name = "Math 101",
            scheduleDaysOfWeek = "[2]",
            startTimeMinutes = 480,
            repeatInterval = 1,
            repeatUnit = "WEEK",
            createdAt = LocalDate.of(2024, 1, 1).atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
        )
        
        whenever(classRepository.getClassById(1L)).thenReturn(classEntity)
        whenever(studentRepository.getStudentsByClass(1L)).thenReturn(flowOf(emptyList()))
        whenever(dailyRecordRepository.getRecordsByClassAndDate(any(), any())).thenReturn(flowOf(emptyList()))
        
        viewModelWithSavedState.loadClassDetail(1L)
        advanceUntilIdle()
        
        // Set a filter first
        viewModelWithSavedState.setFilter(com.studentmanagement.app.ui.model.FilterType.NO_SCORE, "18/11/2025")
        advanceUntilIdle()
        
        // When
        viewModelWithSavedState.resetFilter()
        advanceUntilIdle()
        
        // Then
        assertEquals("ALL", savedStateHandle.get<String>("filter_type"))
        assertNull(savedStateHandle.get<String>("filter_date"))
    }
    
    @Test
    fun `filter state persists across configuration changes simulation`() = runTest {
        // Given - First ViewModel instance with filter set
        val savedStateHandle = SavedStateHandle()
        val firstViewModel = ClassDetailViewModel(
            classRepository = classRepository,
            studentRepository = studentRepository,
            dailyRecordRepository = dailyRecordRepository,
            savedStateHandle = savedStateHandle
        )
        
        val classEntity = ClassEntity(
            id = 1L,
            name = "Math 101",
            scheduleDaysOfWeek = "[2]",
            startTimeMinutes = 480,
            repeatInterval = 1,
            repeatUnit = "WEEK",
            createdAt = LocalDate.of(2024, 1, 1).atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
        )
        
        whenever(classRepository.getClassById(1L)).thenReturn(classEntity)
        whenever(studentRepository.getStudentsByClass(1L)).thenReturn(flowOf(emptyList()))
        whenever(dailyRecordRepository.getRecordsByClassAndDate(any(), any())).thenReturn(flowOf(emptyList()))
        
        firstViewModel.loadClassDetail(1L)
        advanceUntilIdle()
        
        firstViewModel.setFilter(com.studentmanagement.app.ui.model.FilterType.LOW_SCORE, "22/11/2025")
        advanceUntilIdle()
        
        // When - Simulate configuration change by creating new ViewModel with same SavedStateHandle
        val secondViewModel = ClassDetailViewModel(
            classRepository = classRepository,
            studentRepository = studentRepository,
            dailyRecordRepository = dailyRecordRepository,
            savedStateHandle = savedStateHandle
        )
        
        // Then - Filter state should be preserved
        val filterState = secondViewModel.filterState.value
        assertEquals(com.studentmanagement.app.ui.model.FilterType.LOW_SCORE, filterState.type)
        assertEquals("22/11/2025", filterState.selectedDate)
        assertTrue(filterState.isActive())
    }
}
