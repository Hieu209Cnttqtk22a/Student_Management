package com.studentmanagement.app.ui.viewmodel

import android.util.Log
import com.studentmanagement.app.data.entity.ClassEntity
import com.studentmanagement.app.data.repository.ClassRepository
import com.studentmanagement.app.data.repository.StudentRepository
import com.studentmanagement.app.service.ScheduleService
import io.mockk.every
import io.mockk.mockkStatic
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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

@OptIn(ExperimentalCoroutinesApi::class)
class ClassListViewModelTest {
    
    private lateinit var viewModel: ClassListViewModel
    private lateinit var classRepository: ClassRepository
    private lateinit var scheduleService: ScheduleService
    private lateinit var studentRepository: StudentRepository
    
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
        scheduleService = mock()
        studentRepository = mock()
        
        // Mock the initial loadClasses() call in init block
        whenever(classRepository.getAllClasses()).thenReturn(kotlinx.coroutines.flow.flowOf(emptyList()))
        
        viewModel = ClassListViewModel(
            classRepository = classRepository,
            scheduleService = scheduleService,
            studentRepository = studentRepository
        )
        
        // Advance past the init block's loadClasses() call
        testDispatcher.scheduler.advanceUntilIdle()
    }
    
    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }
    
    @Test
    fun `createClass with valid ID calls scheduleService with correct ID`() = runTest {
        // Given: A class entity to create
        val classEntity = ClassEntity(
            id = 0L,
            name = "Math 101",
            scheduleDaysOfWeek = "[2,4,6]",
            startTimeMinutes = 480,
            repeatInterval = 1,
            repeatUnit = "WEEK"
        )
        
        val expectedNewId = 123L
        whenever(classRepository.createClass(classEntity)).thenReturn(expectedNewId)
        
        // When: Creating the class
        var completed = false
        viewModel.createClass(classEntity) { completed = true }
        
        // Give time for coroutine to complete
        advanceUntilIdle()
        
        // Then: callback should be called
        assertTrue(completed)
        
        // And: scheduleService should be called with the class entity containing the new ID
        verify(scheduleService).generateScheduleForClass(argThat { classArg ->
            classArg.id == expectedNewId &&
            classArg.name == classEntity.name &&
            classArg.scheduleDaysOfWeek == classEntity.scheduleDaysOfWeek
        })
    }
    
    @Test
    fun `createClass when repository fails does not call scheduleService`() = runTest {
        // Given: A class entity and repository that throws exception
        val classEntity = ClassEntity(
            id = 0L,
            name = "Math 101",
            scheduleDaysOfWeek = "[2,4,6]",
            startTimeMinutes = 480,
            repeatInterval = 1,
            repeatUnit = "WEEK"
        )
        
        val exception = RuntimeException("Database error")
        whenever(classRepository.createClass(classEntity)).thenThrow(exception)
        
        // When: Creating the class
        var completed = false
        viewModel.createClass(classEntity) { completed = true }
        
        // Give time for coroutine to complete
        advanceUntilIdle()
        
        // Then: scheduleService should not be called
        verify(scheduleService, never()).generateScheduleForClass(any())
        
        // And: UI state should be Error
        val uiState = viewModel.uiState.value
        assertTrue(uiState is ClassListUiState.Error)
        assertEquals("Database error", (uiState as ClassListUiState.Error).message)
    }
}
