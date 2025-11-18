package com.studentmanagement.app.util

import org.json.JSONArray
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ScheduleCalculator @Inject constructor() {
    
    private val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    
    /**
     * Tính toán các ngày học dựa trên lịch học của lớp
     * 
     * @param scheduleDaysOfWeek JSON string chứa các ngày trong tuần (1=CN, 2=T2, ..., 7=T7)
     * @param repeatInterval Số lần lặp (1, 2, 3...)
     * @param repeatUnit Đơn vị lặp (WEEK, MONTH, YEAR)
     * @param startDate Ngày bắt đầu tính toán (mặc định là hôm nay)
     * @param monthsAhead Số tháng tính toán trước (mặc định là 3 tháng)
     * @return Danh sách các ngày học theo format yyyy-MM-dd
     */
    fun calculateScheduleDates(
        scheduleDaysOfWeek: String,
        repeatInterval: Int,
        repeatUnit: String,
        startDate: Calendar = Calendar.getInstance(),
        monthsAhead: Int = 3
    ): List<String> {
        // Parse scheduleDaysOfWeek từ JSON
        val daysOfWeek = parseScheduleDaysOfWeek(scheduleDaysOfWeek)
        
        if (daysOfWeek.isEmpty()) {
            return emptyList()
        }
        
        // Tính ngày kết thúc
        val endDate = Calendar.getInstance().apply {
            time = startDate.time
            add(Calendar.MONTH, monthsAhead)
        }
        
        val scheduleDates = mutableListOf<String>()
        
        when (repeatUnit.uppercase()) {
            "WEEK" -> {
                scheduleDates.addAll(calculateWeeklySchedule(daysOfWeek, repeatInterval, startDate, endDate))
            }
            "MONTH" -> {
                scheduleDates.addAll(calculateMonthlySchedule(daysOfWeek, repeatInterval, startDate, endDate))
            }
            "YEAR" -> {
                scheduleDates.addAll(calculateYearlySchedule(daysOfWeek, repeatInterval, startDate, endDate))
            }
            else -> {
                throw IllegalArgumentException("Invalid repeatUnit: $repeatUnit. Must be WEEK, MONTH, or YEAR")
            }
        }
        
        return scheduleDates.sorted()
    }
    
    /**
     * Parse JSON scheduleDaysOfWeek thành List<Int>
     * Format: "[2,4,6]" -> [2, 4, 6]
     */
    private fun parseScheduleDaysOfWeek(scheduleDaysOfWeek: String): List<Int> {
        if (scheduleDaysOfWeek.isBlank()) {
            return emptyList()
        }
        
        return try {
            val jsonArray = JSONArray(scheduleDaysOfWeek)
            val result = mutableListOf<Int>()
            for (i in 0 until jsonArray.length()) {
                result.add(jsonArray.getInt(i))
            }
            result
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    /**
     * Tính toán lịch học theo tuần
     */
    private fun calculateWeeklySchedule(
        daysOfWeek: List<Int>,
        repeatInterval: Int,
        startDate: Calendar,
        endDate: Calendar
    ): List<String> {
        val dates = mutableListOf<String>()
        val currentDate = Calendar.getInstance().apply { time = startDate.time }
        
        // Tìm ngày đầu tiên trong tuần hiện tại khớp với scheduleDaysOfWeek
        var weekCounter = 0
        
        while (currentDate.before(endDate) || currentDate == endDate) {
            val dayOfWeek = currentDate.get(Calendar.DAY_OF_WEEK)
            
            // Kiểm tra xem ngày hiện tại có trong scheduleDaysOfWeek không
            if (daysOfWeek.contains(dayOfWeek) && weekCounter % repeatInterval == 0) {
                dates.add(dateFormatter.format(currentDate.time))
            }
            
            // Chuyển sang ngày tiếp theo
            currentDate.add(Calendar.DAY_OF_YEAR, 1)
            
            // Nếu sang tuần mới (Chủ nhật -> Thứ 2), tăng weekCounter
            if (currentDate.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY && currentDate.after(startDate)) {
                weekCounter++
            }
        }
        
        return dates
    }
    
    /**
     * Tính toán lịch học theo tháng
     */
    private fun calculateMonthlySchedule(
        daysOfWeek: List<Int>,
        repeatInterval: Int,
        startDate: Calendar,
        endDate: Calendar
    ): List<String> {
        val dates = mutableListOf<String>()
        val currentMonth = Calendar.getInstance().apply { 
            time = startDate.time
            set(Calendar.DAY_OF_MONTH, 1)
        }
        
        var monthCounter = 0
        
        while (currentMonth.before(endDate) || currentMonth == endDate) {
            if (monthCounter % repeatInterval == 0) {
                // Tìm tất cả các ngày trong tháng khớp với scheduleDaysOfWeek
                val daysInMonth = currentMonth.getActualMaximum(Calendar.DAY_OF_MONTH)
                
                for (day in 1..daysInMonth) {
                    val dateInMonth = Calendar.getInstance().apply {
                        time = currentMonth.time
                        set(Calendar.DAY_OF_MONTH, day)
                    }
                    
                    // Chỉ thêm nếu ngày >= startDate và <= endDate
                    if ((dateInMonth.after(startDate) || dateInMonth == startDate) && 
                        (dateInMonth.before(endDate) || dateInMonth == endDate)) {
                        
                        val dayOfWeek = dateInMonth.get(Calendar.DAY_OF_WEEK)
                        if (daysOfWeek.contains(dayOfWeek)) {
                            dates.add(dateFormatter.format(dateInMonth.time))
                        }
                    }
                }
            }
            
            currentMonth.add(Calendar.MONTH, 1)
            monthCounter++
        }
        
        return dates
    }
    
    /**
     * Tính toán lịch học theo năm
     */
    private fun calculateYearlySchedule(
        daysOfWeek: List<Int>,
        repeatInterval: Int,
        startDate: Calendar,
        endDate: Calendar
    ): List<String> {
        val dates = mutableListOf<String>()
        val currentYear = Calendar.getInstance().apply { 
            time = startDate.time
            set(Calendar.DAY_OF_YEAR, 1)
        }
        
        var yearCounter = 0
        
        while (currentYear.before(endDate) || currentYear == endDate) {
            if (yearCounter % repeatInterval == 0) {
                // Tìm tất cả các ngày trong năm khớp với scheduleDaysOfWeek
                val daysInYear = currentYear.getActualMaximum(Calendar.DAY_OF_YEAR)
                
                for (day in 1..daysInYear) {
                    val dateInYear = Calendar.getInstance().apply {
                        time = currentYear.time
                        set(Calendar.DAY_OF_YEAR, day)
                    }
                    
                    // Chỉ thêm nếu ngày >= startDate và <= endDate
                    if ((dateInYear.after(startDate) || dateInYear == startDate) && 
                        (dateInYear.before(endDate) || dateInYear == endDate)) {
                        
                        val dayOfWeek = dateInYear.get(Calendar.DAY_OF_WEEK)
                        if (daysOfWeek.contains(dayOfWeek)) {
                            dates.add(dateFormatter.format(dateInYear.time))
                        }
                    }
                }
            }
            
            currentYear.add(Calendar.YEAR, 1)
            yearCounter++
        }
        
        return dates
    }
}
