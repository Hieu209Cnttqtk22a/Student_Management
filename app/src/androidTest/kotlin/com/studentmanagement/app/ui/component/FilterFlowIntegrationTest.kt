package com.studentmanagement.app.ui.component

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.studentmanagement.app.ui.model.FilterState
import com.studentmanagement.app.ui.model.FilterType
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate

class FilterFlowIntegrationTest {
    
    @get:Rule
    val composeTestRule = createComposeRule()
    
    @Test
    fun completeFilterFlow_selectFilter_selectDate_viewFilteredList() {
        // Given - Setup a complete filter flow with state management
        var currentFilter by mutableStateOf(FilterState())
        val scheduledDates = listOf(
            LocalDate.of(2024, 11, 15),
            LocalDate.of(2024, 11, 18),
            LocalDate.of(2024, 11, 20)
        )
        
        composeTestRule.setContent {
            Column {
                FilterComboBox(
                    currentFilter = currentFilter,
                    scheduledDates = scheduledDates,
                    onFilterChange = { type, date ->
                        currentFilter = FilterState(type, date)
                    }
                )
                ResetFilterButton(
                    isVisible = currentFilter.isActive(),
                    onReset = {
                        currentFilter = FilterState()
                    }
                )
                // Display current filter state
                Text("Filter: ${currentFilter.getDisplayLabel()}")
            }
        }
        
        // When - Step 1: Open dropdown and select "Điểm kém (< 7)"
        composeTestRule.onNodeWithContentDescription("Tất cả").performClick()
        composeTestRule.onNodeWithText("Điểm kém (< 7)").performClick()
        
        // Then - Date dialog should appear
        composeTestRule.onNodeWithText("Chọn ngày").assertIsDisplayed()
        
        // When - Step 2: Select a date
        composeTestRule.onNodeWithText("15/11/2024").performClick()
        
        // Then - Filter should be applied with the selected date
        composeTestRule.onNodeWithText("Filter: Điểm kém - 15/11/2024").assertIsDisplayed()
        
        // And - Reset button should be visible
        composeTestRule.onNodeWithContentDescription("Xóa bộ lọc").assertIsDisplayed()
    }
    
    @Test
    fun resetButtonClearsFilter() {
        // Given - A filter is already applied
        var currentFilter by mutableStateOf(
            FilterState(type = FilterType.LOW_SCORE, selectedDate = "15/11/2024")
        )
        val scheduledDates = listOf(LocalDate.of(2024, 11, 15))
        
        composeTestRule.setContent {
            Column {
                FilterComboBox(
                    currentFilter = currentFilter,
                    scheduledDates = scheduledDates,
                    onFilterChange = { type, date ->
                        currentFilter = FilterState(type, date)
                    }
                )
                ResetFilterButton(
                    isVisible = currentFilter.isActive(),
                    onReset = {
                        currentFilter = FilterState()
                    }
                )
                Text("Filter: ${currentFilter.getDisplayLabel()}")
            }
        }
        
        // Verify filter is active
        composeTestRule.onNodeWithText("Filter: Điểm kém - 15/11/2024").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Xóa bộ lọc").assertIsDisplayed()
        
        // When - Click reset button
        composeTestRule.onNodeWithContentDescription("Xóa bộ lọc").performClick()
        
        // Then - Filter should be cleared
        composeTestRule.onNodeWithText("Filter: Tất cả").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Xóa bộ lọc").assertDoesNotExist()
    }
    
    @Test
    fun emptyStateDisplaysCorrectMessageForLowScoreFilter() {
        // Given - Empty filtered list with LOW_SCORE filter
        val filterState = FilterState(type = FilterType.LOW_SCORE, selectedDate = "15/11/2024")
        val emptyStudents = emptyList<String>()
        
        composeTestRule.setContent {
            Column {
                Text("Filter: ${filterState.getDisplayLabel()}")
                
                // Simulate empty state
                if (filterState.isActive() && emptyStudents.isEmpty()) {
                    val emptyMessage = when (filterState.type) {
                        FilterType.LOW_SCORE -> 
                            "Không có học sinh nào có điểm dưới 7 trong ngày này"
                        FilterType.NO_SCORE -> 
                            "Tất cả học sinh đã có điểm trong ngày này"
                        FilterType.PERFECT_SCORE -> 
                            "Không có học sinh nào đạt điểm 10 trong ngày này"
                        else -> ""
                    }
                    Text(emptyMessage)
                }
            }
        }
        
        // Then - Correct empty state message should be displayed
        composeTestRule.onNodeWithText("Không có học sinh nào có điểm dưới 7 trong ngày này")
            .assertIsDisplayed()
    }
    
    @Test
    fun emptyStateDisplaysCorrectMessageForNoScoreFilter() {
        // Given - Empty filtered list with NO_SCORE filter
        val filterState = FilterState(type = FilterType.NO_SCORE, selectedDate = "15/11/2024")
        val emptyStudents = emptyList<String>()
        
        composeTestRule.setContent {
            Column {
                Text("Filter: ${filterState.getDisplayLabel()}")
                
                // Simulate empty state
                if (filterState.isActive() && emptyStudents.isEmpty()) {
                    val emptyMessage = when (filterState.type) {
                        FilterType.LOW_SCORE -> 
                            "Không có học sinh nào có điểm dưới 7 trong ngày này"
                        FilterType.NO_SCORE -> 
                            "Tất cả học sinh đã có điểm trong ngày này"
                        FilterType.PERFECT_SCORE -> 
                            "Không có học sinh nào đạt điểm 10 trong ngày này"
                        else -> ""
                    }
                    Text(emptyMessage)
                }
            }
        }
        
        // Then - Correct empty state message should be displayed
        composeTestRule.onNodeWithText("Tất cả học sinh đã có điểm trong ngày này")
            .assertIsDisplayed()
    }
    
    @Test
    fun emptyStateDisplaysCorrectMessageForPerfectScoreFilter() {
        // Given - Empty filtered list with PERFECT_SCORE filter
        val filterState = FilterState(type = FilterType.PERFECT_SCORE, selectedDate = "15/11/2024")
        val emptyStudents = emptyList<String>()
        
        composeTestRule.setContent {
            Column {
                Text("Filter: ${filterState.getDisplayLabel()}")
                
                // Simulate empty state
                if (filterState.isActive() && emptyStudents.isEmpty()) {
                    val emptyMessage = when (filterState.type) {
                        FilterType.LOW_SCORE -> 
                            "Không có học sinh nào có điểm dưới 7 trong ngày này"
                        FilterType.NO_SCORE -> 
                            "Tất cả học sinh đã có điểm trong ngày này"
                        FilterType.PERFECT_SCORE -> 
                            "Không có học sinh nào đạt điểm 10 trong ngày này"
                        else -> ""
                    }
                    Text(emptyMessage)
                }
            }
        }
        
        // Then - Correct empty state message should be displayed
        composeTestRule.onNodeWithText("Không có học sinh nào đạt điểm 10 trong ngày này")
            .assertIsDisplayed()
    }
    
    @Test
    fun switchingBetweenDifferentFilters() {
        // Given - Start with no filter
        var currentFilter by mutableStateOf(FilterState())
        val scheduledDates = listOf(LocalDate.of(2024, 11, 15))
        
        composeTestRule.setContent {
            Column {
                FilterComboBox(
                    currentFilter = currentFilter,
                    scheduledDates = scheduledDates,
                    onFilterChange = { type, date ->
                        currentFilter = FilterState(type, date)
                    }
                )
                Text("Filter: ${currentFilter.getDisplayLabel()}")
            }
        }
        
        // When - Apply LOW_SCORE filter
        composeTestRule.onNodeWithContentDescription("Tất cả").performClick()
        composeTestRule.onNodeWithText("Điểm kém (< 7)").performClick()
        composeTestRule.onNodeWithText("15/11/2024").performClick()
        
        // Then - LOW_SCORE filter should be active
        composeTestRule.onNodeWithText("Filter: Điểm kém - 15/11/2024").assertIsDisplayed()
        
        // When - Switch to NO_SCORE filter
        composeTestRule.onNodeWithContentDescription("Điểm kém - 15/11/2024").performClick()
        composeTestRule.onNodeWithText("Chưa có điểm").performClick()
        composeTestRule.onNodeWithText("15/11/2024").performClick()
        
        // Then - NO_SCORE filter should be active
        composeTestRule.onNodeWithText("Filter: Chưa có điểm - 15/11/2024").assertIsDisplayed()
        
        // When - Switch to PERFECT_SCORE filter
        composeTestRule.onNodeWithContentDescription("Chưa có điểm - 15/11/2024").performClick()
        composeTestRule.onNodeWithText("Điểm 10").performClick()
        composeTestRule.onNodeWithText("15/11/2024").performClick()
        
        // Then - PERFECT_SCORE filter should be active
        composeTestRule.onNodeWithText("Filter: Điểm 10 - 15/11/2024").assertIsDisplayed()
    }
    
    @Test
    fun selectingAllFilterClearsDateAndShowsAllStudents() {
        // Given - A filter is already applied
        var currentFilter by mutableStateOf(
            FilterState(type = FilterType.LOW_SCORE, selectedDate = "15/11/2024")
        )
        val scheduledDates = listOf(LocalDate.of(2024, 11, 15))
        
        composeTestRule.setContent {
            Column {
                FilterComboBox(
                    currentFilter = currentFilter,
                    scheduledDates = scheduledDates,
                    onFilterChange = { type, date ->
                        currentFilter = FilterState(type, date)
                    }
                )
                Text("Filter: ${currentFilter.getDisplayLabel()}")
                Text("Date: ${currentFilter.selectedDate ?: "none"}")
            }
        }
        
        // Verify filter is active
        composeTestRule.onNodeWithText("Filter: Điểm kém - 15/11/2024").assertIsDisplayed()
        
        // When - Select "Tất cả" filter
        composeTestRule.onNodeWithContentDescription("Điểm kém - 15/11/2024").performClick()
        composeTestRule.onNodeWithText("Tất cả").performClick()
        
        // Then - Filter should be cleared and date should be null
        composeTestRule.onNodeWithText("Filter: Tất cả").assertIsDisplayed()
        composeTestRule.onNodeWithText("Date: none").assertIsDisplayed()
    }
}
