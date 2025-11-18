package com.studentmanagement.app.service

import com.studentmanagement.app.data.entity.ClassEntity
import com.studentmanagement.app.data.entity.StudentEntity
import com.studentmanagement.app.data.repository.DailyRecordRepository
import com.studentmanagement.app.data.repository.StudentRepository
import com.studentmanagement.app.util.ScheduleCalculator
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.*
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class ScheduleServiceTest {
    
    private lateinit var scheduleService: ScheduleService
    private lateinit var studentRepository: StudentRepository
    private lateinit var dailyRecordRepository: DailyRecordRepository
    private lateinit var scheduleCalculator: ScheduleCalculator
    
    @Before
    fun setup() {
        studentRepository = mock()
        dailyRecordRepository = mock()
        scheduleCalculator = ScheduleCalculator() // Use real calculator with Robolectric
        
        scheduleService = ScheduleService(
            studentRepository = studentRepository,
            dailyRecordRepository = dailyRecordRepository,
            scheduleCalculator = scheduleCalculator
        )
    }
    
    @Test
    fun `generateScheduleForClass with students creates daily records`() = runTest {
        // Given: A class with students
        val classEntity = ClassEntity(
            id = 1L,
            name = "Math 101",
            scheduleDaysOfWeek = "[2]", // Monday
            startTimeMinutes = 480,
            repeatInterval = 1,
            repeatUnit = "WEEK"
        )
        
        val students = listOf(
            StudentEntity(id = 1L, name = "Student 1", classId = 1L),
            StudentEntity(id = 2L, name = "Student 2", classId = 1L)
        )
        
        whenever(studentRepository.getStudentsByClass(1L)).thenReturn(flowOf(students))
        whenever(dailyRecordRepository.recordExists(any(), any(), any())).thenReturn(false)
        
        // When
        scheduleService.generateScheduleForClass(classEntity)
        
        // Then: Should create records for each student
        verify(dailyRecordRepository).createBulkRecords(argThat { records ->
            records.isNotEmpty() &&
            records.all { it.classId == 1L } &&
            records.any { it.studentId == 1L } &&
            records.any { it.studentId == 2L }
        })
    }
    
    @Test
    fun `generateScheduleForClass without students does not create records`() = runTest {
        // Given: A class without students
        val classEntity = ClassEntity(
            id = 1L,
            name = "Math 101",
            scheduleDaysOfWeek = "[2,4,6]",
            startTimeMinutes = 480,
            repeatInterval = 1,
            repeatUnit = "WEEK"
        )
        
        whenever(studentRepository.getStudentsByClass(1L)).thenReturn(flowOf(emptyList()))
        
        // When
        scheduleService.generateScheduleForClass(classEntity)
        
        // Then: Should not create any records
        verify(dailyRecordRepository, never()).createBulkRecords(any())
    }
    
    @Test
    fun `generateScheduleForClass skips existing records`() = runTest {
        // Given: A class with students and some existing records
        val classEntity = ClassEntity(
            id = 1L,
            name = "Math 101",
            scheduleDaysOfWeek = "[2]",
            startTimeMinutes = 480,
            repeatInterval = 1,
            repeatUnit = "WEEK"
        )
        
        val students = listOf(
            StudentEntity(id = 1L, name = "Student 1", classId = 1L)
        )
        
        whenever(studentRepository.getStudentsByClass(1L)).thenReturn(flowOf(students))
        
        // Mock that some records exist
        whenever(dailyRecordRepository.recordExists(any(), any(), any())).thenAnswer { invocation ->
            val date = invocation.getArgument<String>(2)
            // First date exists, others don't
            date == "2024-01-01"
        }
        
        // When
        scheduleService.generateScheduleForClass(classEntity)
        
        // Then: Should create some records (but not for existing dates)
        verify(dailyRecordRepository).createBulkRecords(argThat { records ->
            records.all { it.date != "2024-01-01" }
        })
    }
    
    @Test
    fun `regenerateScheduleForClass deletes empty records and creates new ones`() = runTest {
        // Given: A class with students
        val classEntity = ClassEntity(
            id = 1L,
            name = "Math 101",
            scheduleDaysOfWeek = "[2]",
            startTimeMinutes = 480,
            repeatInterval = 1,
            repeatUnit = "WEEK"
        )
        
        val students = listOf(
            StudentEntity(id = 1L, name = "Student 1", classId = 1L)
        )
        
        whenever(studentRepository.getStudentsByClass(1L)).thenReturn(flowOf(students))
        whenever(dailyRecordRepository.recordExists(any(), any(), any())).thenReturn(false)
        
        // When
        scheduleService.regenerateScheduleForClass(classEntity)
        
        // Then: Should delete empty records first, then create new ones
        val inOrder = inOrder(dailyRecordRepository)
        inOrder.verify(dailyRecordRepository).deleteEmptyRecordsByClass(1L)
        inOrder.verify(dailyRecordRepository).createBulkRecords(any())
    }
    
    @Test
    fun `generateScheduleForClass creates records with correct data`() = runTest {
        // Given
        val classEntity = ClassEntity(
            id = 1L,
            name = "Math 101",
            scheduleDaysOfWeek = "[2]",
            startTimeMinutes = 480,
            repeatInterval = 1,
            repeatUnit = "WEEK"
        )
        
        val students = listOf(
            StudentEntity(id = 1L, name = "Student 1", classId = 1L)
        )
        
        whenever(studentRepository.getStudentsByClass(1L)).thenReturn(flowOf(students))
        whenever(dailyRecordRepository.recordExists(any(), any(), any())).thenReturn(false)
        
        // When
        scheduleService.generateScheduleForClass(classEntity)
        
        // Then: Verify record structure
        verify(dailyRecordRepository).createBulkRecords(argThat { records ->
            records.isNotEmpty() &&
            records.all { 
                it.studentId == 1L &&
                it.classId == 1L &&
                it.score == null &&
                it.note == null
            }
        })
    }
}
