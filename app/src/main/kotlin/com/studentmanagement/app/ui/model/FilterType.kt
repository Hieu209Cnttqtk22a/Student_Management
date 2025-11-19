package com.studentmanagement.app.ui.model

/**
 * Enum defining the types of filters available for student score filtering.
 */
enum class FilterType {
    /** Show all students regardless of score */
    ALL,
    
    /** Show only students with scores less than 7.0 */
    LOW_SCORE,
    
    /** Show only students with no score recorded */
    NO_SCORE,
    
    /** Show only students with perfect score (10.0) */
    PERFECT_SCORE
}
