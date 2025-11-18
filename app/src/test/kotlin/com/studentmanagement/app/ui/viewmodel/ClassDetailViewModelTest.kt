package com.studentmanagement.app.ui.viewmodel

import android.util.Log
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
            dailyRecordRepository = dailyRecordRepository
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
}
