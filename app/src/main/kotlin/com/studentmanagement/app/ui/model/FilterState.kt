package com.studentmanagement.app.ui.model

/**
 * Data class representing the current filter state for student score filtering.
 *
 * @property type The type of filter being applied
 * @property selectedDate The selected date for date-specific filters (format: dd/MM/yyyy), null if not applicable
 */
data class FilterState(
    val type: FilterType = FilterType.ALL,
    val selectedDate: String? = null
) {
    /**
     * Checks if a filter is currently active (not showing all students).
     *
     * @return true if a filter other than ALL is active, false otherwise
     */
    fun isActive(): Boolean = type != FilterType.ALL
    
    /**
     * Gets the display label for the current filter state.
     * Includes the selected date if applicable.
     *
     * @return A formatted string representing the current filter
     */
    fun getDisplayLabel(): String {
        return when (type) {
            FilterType.ALL -> "Tất cả"
            FilterType.LOW_SCORE -> "Điểm kém${selectedDate?.let { " - $it" } ?: ""}"
            FilterType.NO_SCORE -> "Chưa có điểm${selectedDate?.let { " - $it" } ?: ""}"
            FilterType.PERFECT_SCORE -> "Điểm 10${selectedDate?.let { " - $it" } ?: ""}"
        }
    }
}
