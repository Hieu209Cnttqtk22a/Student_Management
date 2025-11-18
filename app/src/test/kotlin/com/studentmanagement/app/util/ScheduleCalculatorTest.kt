package com.studentmanagement.app.util

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.Calendar

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class ScheduleCalculatorTest {
    
    private lateinit var scheduleCalculator: ScheduleCalculator
    
    @Before
    fun setup() {
        scheduleCalculator = ScheduleCalculator()
    }
    
    @Test
    fun `calculateScheduleDates with WEEK repeat returns dates for specified days`() {
        // Given: T2, T4, T6 (Monday=2, Wednesday=4, Friday=6)
        val scheduleDaysOfWeek = "[2,4,6]"
        val repeatInterval = 1
        val repeatUnit = "WEEK"
        
        val startDate = Calendar.getInstance().apply {
            set(2024, Calendar.JANUARY, 1, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }
        
        // When
        val result = scheduleCalculator.calculateScheduleDates(
            scheduleDaysOfWeek = scheduleDaysOfWeek,
            repeatInterval = repeatInterval,
            repeatUnit = repeatUnit,
            startDate = startDate,
            monthsAhead = 1
        )
        
        // Then: Should have dates for Mon, Wed, Fri
        assertTrue("Result should not be empty", result.isNotEmpty())
        assertTrue("Should contain Monday dates", result.any { it.contains("2024-01") })
    }
    
    @Test
    fun `calculateScheduleDates with WEEK repeat interval 2 skips alternate weeks`() {
        // Given: T2 (Monday) every 2 weeks
        val scheduleDaysOfWeek = "[2]"
        val repeatInterval = 2
        val repeatUnit = "WEEK"
        
        val startDate = Calendar.getInstance().apply {
            set(2024, Calendar.JANUARY, 1, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }
        
        // When
        val result = scheduleCalculator.calculateScheduleDates(
            scheduleDaysOfWeek = scheduleDaysOfWeek,
            repeatInterval = repeatInterval,
            repeatUnit = repeatUnit,
            startDate = startDate,
            monthsAhead = 2
        )
        
        // Then: Should have some Mondays but not all
        assertTrue("Result should not be empty", result.isNotEmpty())
        // With interval 2, should have fewer dates than interval 1
        val allMondays = scheduleCalculator.calculateScheduleDates(
            scheduleDaysOfWeek = scheduleDaysOfWeek,
            repeatInterval = 1,
            repeatUnit = repeatUnit,
            startDate = startDate,
            monthsAhead = 2
        )
        assertTrue("Interval 2 should have fewer dates", result.size < allMondays.size)
    }
    
    @Test
    fun `calculateScheduleDates with MONTH repeat returns all matching days in month`() {
        // Given: T3 (Tuesday) every month
        val scheduleDaysOfWeek = "[3]"
        val repeatInterval = 1
        val repeatUnit = "MONTH"
        
        val startDate = Calendar.getInstance().apply {
            set(2024, Calendar.JANUARY, 1, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }
        
        // When
        val result = scheduleCalculator.calculateScheduleDates(
            scheduleDaysOfWeek = scheduleDaysOfWeek,
            repeatInterval = repeatInterval,
            repeatUnit = repeatUnit,
            startDate = startDate,
            monthsAhead = 2
        )
        
        // Then: Should have Tuesdays from January and February
        assertTrue("Result should not be empty", result.isNotEmpty())
        assertTrue("Should have January dates", result.any { it.startsWith("2024-01") })
        assertTrue("Should have February dates", result.any { it.startsWith("2024-02") })
    }
    
    @Test
    fun `calculateScheduleDates with YEAR repeat returns all matching days in period`() {
        // Given: T2 (Monday) every year
        val scheduleDaysOfWeek = "[2]"
        val repeatInterval = 1
        val repeatUnit = "YEAR"
        
        val startDate = Calendar.getInstance().apply {
            set(2024, Calendar.JANUARY, 1, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }
        
        // When
        val result = scheduleCalculator.calculateScheduleDates(
            scheduleDaysOfWeek = scheduleDaysOfWeek,
            repeatInterval = repeatInterval,
            repeatUnit = repeatUnit,
            startDate = startDate,
            monthsAhead = 2
        )
        
        // Then: Should have all Mondays in the 2-month period
        assertTrue("Result should not be empty", result.isNotEmpty())
        val januaryMondays = result.count { it.startsWith("2024-01") }
        val februaryMondays = result.count { it.startsWith("2024-02") }
        assertTrue("Should have January Mondays", januaryMondays > 0)
        assertTrue("Should have February Mondays", februaryMondays > 0)
    }
    
    @Test
    fun `calculateScheduleDates with multiple days returns all specified days`() {
        // Given: T2, T5, T7 (Monday, Thursday, Saturday)
        val scheduleDaysOfWeek = "[2,5,7]"
        val repeatInterval = 1
        val repeatUnit = "WEEK"
        
        val startDate = Calendar.getInstance().apply {
            set(2024, Calendar.JANUARY, 1, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }
        
        // When
        val result = scheduleCalculator.calculateScheduleDates(
            scheduleDaysOfWeek = scheduleDaysOfWeek,
            repeatInterval = repeatInterval,
            repeatUnit = repeatUnit,
            startDate = startDate,
            monthsAhead = 1
        )
        
        // Then: Should have multiple days per week
        assertTrue("Result should not be empty", result.isNotEmpty())
        // Should have more dates than single day per week
        val singleDayResult = scheduleCalculator.calculateScheduleDates(
            scheduleDaysOfWeek = "[2]",
            repeatInterval = repeatInterval,
            repeatUnit = repeatUnit,
            startDate = startDate,
            monthsAhead = 1
        )
        assertTrue("Multiple days should have more dates", result.size > singleDayResult.size)
    }
    
    @Test
    fun `calculateScheduleDates with empty scheduleDaysOfWeek returns empty list`() {
        // Given: Empty schedule
        val scheduleDaysOfWeek = "[]"
        val repeatInterval = 1
        val repeatUnit = "WEEK"
        
        val startDate = Calendar.getInstance()
        
        // When
        val result = scheduleCalculator.calculateScheduleDates(
            scheduleDaysOfWeek = scheduleDaysOfWeek,
            repeatInterval = repeatInterval,
            repeatUnit = repeatUnit,
            startDate = startDate,
            monthsAhead = 1
        )
        
        // Then
        assertTrue("Empty schedule should return empty list", result.isEmpty())
    }
    
    @Test
    fun `calculateScheduleDates with invalid repeatUnit throws exception`() {
        // Given: Invalid repeat unit
        val scheduleDaysOfWeek = "[2]"
        val repeatInterval = 1
        val repeatUnit = "INVALID"
        
        val startDate = Calendar.getInstance()
        
        // When/Then
        assertThrows(IllegalArgumentException::class.java) {
            scheduleCalculator.calculateScheduleDates(
                scheduleDaysOfWeek = scheduleDaysOfWeek,
                repeatInterval = repeatInterval,
                repeatUnit = repeatUnit,
                startDate = startDate,
                monthsAhead = 1
            )
        }
    }
    
    @Test
    fun `calculateScheduleDates returns sorted dates`() {
        // Given: Multiple days
        val scheduleDaysOfWeek = "[7,2,5]" // Saturday, Monday, Thursday (unsorted)
        val repeatInterval = 1
        val repeatUnit = "WEEK"
        
        val startDate = Calendar.getInstance().apply {
            set(2024, Calendar.JANUARY, 1, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }
        
        // When
        val result = scheduleCalculator.calculateScheduleDates(
            scheduleDaysOfWeek = scheduleDaysOfWeek,
            repeatInterval = repeatInterval,
            repeatUnit = repeatUnit,
            startDate = startDate,
            monthsAhead = 1
        )
        
        // Then: Should be sorted
        if (result.isNotEmpty()) {
            val sortedResult = result.sorted()
            assertEquals("Dates should be sorted", sortedResult, result)
        }
    }
}
