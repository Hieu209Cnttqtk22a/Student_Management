package com.studentmanagement.app.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object DateUtils {
    private const val DATE_FORMAT = "yyyy-MM-dd"
    private const val DISPLAY_FORMAT = "dd/MM/yyyy"
    
    private val dateFormatter = SimpleDateFormat(DATE_FORMAT, Locale.getDefault())
    private val displayFormatter = SimpleDateFormat(DISPLAY_FORMAT, Locale.getDefault())

    fun getCurrentDate(): String {
        return dateFormatter.format(Date())
    }

    fun getCurrentDisplayDate(): String {
        return displayFormatter.format(Date())
    }

    fun formatDateForDisplay(date: String): String {
        return try {
            val parsedDate = dateFormatter.parse(date)
            parsedDate?.let { displayFormatter.format(it) } ?: date
        } catch (e: Exception) {
            date
        }
    }

    fun formatDateForDatabase(displayDate: String): String {
        return try {
            val parsedDate = displayFormatter.parse(displayDate)
            parsedDate?.let { dateFormatter.format(it) } ?: displayDate
        } catch (e: Exception) {
            displayDate
        }
    }

    fun getYesterday(): String {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -1)
        return dateFormatter.format(calendar.time)
    }

    fun getTomorrow(): String {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, 1)
        return dateFormatter.format(calendar.time)
    }

    fun getDayOfWeek(date: String): Int {
        return try {
            val parsedDate = dateFormatter.parse(date)
            val calendar = Calendar.getInstance()
            calendar.time = parsedDate ?: Date()
            calendar.get(Calendar.DAY_OF_WEEK)
        } catch (e: Exception) {
            Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
        }
    }

    fun addDays(date: String, days: Int): String {
        return try {
            val parsedDate = dateFormatter.parse(date)
            val calendar = Calendar.getInstance()
            calendar.time = parsedDate ?: Date()
            calendar.add(Calendar.DAY_OF_YEAR, days)
            dateFormatter.format(calendar.time)
        } catch (e: Exception) {
            date
        }
    }

    fun getMonthYear(date: String): String {
        return try {
            val parsedDate = dateFormatter.parse(date)
            val calendar = Calendar.getInstance()
            calendar.time = parsedDate ?: Date()
            val monthFormat = SimpleDateFormat("MMMM yyyy", Locale("vi"))
            monthFormat.format(calendar.time)
        } catch (e: Exception) {
            ""
        }
    }

    fun getDaysInMonth(year: Int, month: Int): Int {
        val calendar = Calendar.getInstance()
        calendar.set(year, month, 1)
        return calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
    }

    fun getFirstDayOfWeekInMonth(year: Int, month: Int): Int {
        val calendar = Calendar.getInstance()
        calendar.set(year, month, 1)
        return calendar.get(Calendar.DAY_OF_WEEK)
    }
}
