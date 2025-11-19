package com.studentmanagement.app.service

import android.util.Log
import com.studentmanagement.app.data.entity.ClassEntity
import com.studentmanagement.app.data.entity.DailyRecordEntity
import com.studentmanagement.app.data.repository.DailyRecordRepository
import com.studentmanagement.app.data.repository.StudentRepository
import com.studentmanagement.app.util.ScheduleCalculator
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ScheduleService @Inject constructor(
    private val studentRepository: StudentRepository,
    private val dailyRecordRepository: DailyRecordRepository,
    private val scheduleCalculator: ScheduleCalculator
) {
    
    companion object {
        private const val TAG = "ScheduleService"
    }
    
    /**
     * Tạo lịch học cho lớp mới
     * Tạo DailyRecord cho tất cả học sinh trong lớp theo lịch đã thiết lập
     * 
     * @param classEntity Lớp học cần tạo lịch
     * @throws IllegalArgumentException if class ID is invalid (id <= 0)
     */
    suspend fun generateScheduleForClass(classEntity: ClassEntity) {
        // Validate class ID
        if (classEntity.id <= 0) {
            Log.e(TAG, "Invalid class ID: ${classEntity.id}")
            throw IllegalArgumentException("Invalid class ID: ${classEntity.id}. Class must be saved to database before generating schedule.")
        }
        
        Log.d(TAG, "Generating schedule for class: ${classEntity.name} (ID: ${classEntity.id})")
        
        // Lấy danh sách học sinh trong lớp
        val students = studentRepository.getStudentsByClass(classEntity.id).first()
        
        // Nếu lớp chưa có học sinh, không tạo records
        if (students.isEmpty()) {
            Log.d(TAG, "No students in class ${classEntity.id}, skipping schedule generation")
            return
        }
        
        Log.d(TAG, "Found ${students.size} students in class ${classEntity.id}")
        
        // Tính toán các ngày học dựa trên lịch
        val scheduleDates = try {
            scheduleCalculator.calculateScheduleDates(
                scheduleDaysOfWeek = classEntity.scheduleDaysOfWeek,
                repeatInterval = classEntity.repeatInterval,
                repeatUnit = classEntity.repeatUnit
            )
        } catch (e: Exception) {
            throw IllegalArgumentException("Invalid schedule configuration: ${e.message}", e)
        }
        
        // Tạo DailyRecord cho mỗi học sinh và mỗi ngày học
        val recordsToCreate = mutableListOf<DailyRecordEntity>()
        val currentTime = System.currentTimeMillis()
        
        for (student in students) {
            for (date in scheduleDates) {
                // Kiểm tra xem record đã tồn tại chưa để tránh duplicate
                if (!dailyRecordRepository.recordExists(student.id, classEntity.id, date)) {
                    recordsToCreate.add(
                        DailyRecordEntity(
                            studentId = student.id,
                            classId = classEntity.id,
                            date = date,
                            score = null,
                            note = null,
                            createdAt = currentTime,
                            updatedAt = currentTime
                        )
                    )
                }
            }
        }
        
        // Bulk insert tất cả records
        if (recordsToCreate.isNotEmpty()) {
            Log.d(TAG, "Creating ${recordsToCreate.size} daily records for class ${classEntity.id}")
            dailyRecordRepository.createBulkRecords(recordsToCreate)
            Log.d(TAG, "Successfully created schedule for class ${classEntity.id}")
        } else {
            Log.d(TAG, "No new records to create for class ${classEntity.id}")
        }
    }
    
    /**
     * Tạo lại lịch học cho lớp đã chỉnh sửa
     * Xóa các DailyRecord chưa có dữ liệu và tạo lại lịch mới
     * 
     * @param classEntity Lớp học cần tạo lại lịch
     */
    suspend fun regenerateScheduleForClass(classEntity: ClassEntity) {
        // Xóa các DailyRecord chưa có dữ liệu (score = null, note = null, không có tags)
        dailyRecordRepository.deleteEmptyRecordsByClass(classEntity.id)
        
        // Tạo lại lịch mới
        generateScheduleForClass(classEntity)
    }
    
    /**
     * Check if a class is scheduled on a given date
     * Requirements: 11.4, 11.5
     * 
     * @param classEntity The class to check
     * @param date The date to check
     * @return true if the class is scheduled on the given date
     */
    fun isClassScheduledOnDate(classEntity: ClassEntity, date: LocalDate): Boolean {
        // Convert LocalDate to Calendar for comparison
        val calendar = Calendar.getInstance().apply {
            set(Calendar.YEAR, date.year)
            set(Calendar.MONTH, date.monthValue - 1) // Calendar months are 0-based
            set(Calendar.DAY_OF_MONTH, date.dayOfMonth)
        }
        
        // Calculate schedule dates for a range that includes the target date
        val startCalendar = Calendar.getInstance().apply {
            time = calendar.time
            add(Calendar.MONTH, -1) // Start 1 month before
        }
        
        val scheduleDates = scheduleCalculator.calculateScheduleDates(
            scheduleDaysOfWeek = classEntity.scheduleDaysOfWeek,
            repeatInterval = classEntity.repeatInterval,
            repeatUnit = classEntity.repeatUnit,
            startDate = startCalendar,
            monthsAhead = 2 // Cover 2 months total
        )
        
        // Format the target date to match the schedule date format
        val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val targetDateString = dateFormatter.format(calendar.time)
        
        return scheduleDates.contains(targetDateString)
    }
    
    /**
     * Get the count of classes scheduled for a specific date
     * Requirements: 11.4, 11.5, 12.5
     * 
     * @param classes List of all classes
     * @param date The date to check
     * @return Number of classes scheduled on the given date
     */
    fun getClassCountForDate(classes: List<ClassEntity>, date: LocalDate): Int {
        return classes.count { classEntity ->
            isClassScheduledOnDate(classEntity, date)
        }
    }
    
    /**
     * Get class counts for all days in a month
     * Requirements: 11.4, 11.5, 12.5
     * 
     * @param classes List of all classes
     * @param yearMonth The month to calculate counts for
     * @return Map of LocalDate to class count
     */
    fun getClassCountsForMonth(classes: List<ClassEntity>, yearMonth: YearMonth): Map<LocalDate, Int> {
        val counts = mutableMapOf<LocalDate, Int>()
        
        // Iterate through all days in the month
        val daysInMonth = yearMonth.lengthOfMonth()
        for (day in 1..daysInMonth) {
            val date = yearMonth.atDay(day)
            val count = getClassCountForDate(classes, date)
            if (count > 0) {
                counts[date] = count
            }
        }
        
        return counts
    }
    
    /**
     * Get list of classes scheduled on a specific date
     * Requirements: 13.1, 13.2
     * 
     * @param classes List of all classes
     * @param date The date to check
     * @return List of classes scheduled on the given date
     */
    fun getClassesForDate(classes: List<ClassEntity>, date: LocalDate): List<ClassEntity> {
        return classes.filter { classEntity ->
            isClassScheduledOnDate(classEntity, date)
        }
    }
}
