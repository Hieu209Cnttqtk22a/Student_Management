package com.studentmanagement.app.ui.component

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.studentmanagement.app.ui.model.FilterState
import com.studentmanagement.app.ui.model.FilterType
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate

class FilterComboBoxTest {
    
    @get:Rule
    val composeTestRule = createComposeRule()
    
    @Test
    fun dropdownOpensWhenClicked() {
        // Given
        val filterState = FilterState()
        val scheduledDates = listOf(
            LocalDate.of(2024, 11, 15),
            LocalDate.of(2024, 11, 18)
        )
        
        composeTestRule.setContent {
            FilterComboBox(
                currentFilter = filterState,
                scheduledDates = scheduledDates,
                onFilterChange = { _, _ -> }
            )
        }
        
        // When - Click the dropdown button
        composeTestRule.onNodeWithContentDescription("Tất cả").performClick()
        
        // Then - All filter options should be displayed
        composeTestRule.onNodeWithText("Tất cả").assertIsDisplayed()
        composeTestRule.onNodeWithText("Điểm kém (< 7)").assertIsDisplayed()
        composeTestRule.onNodeWithText("Chưa có điểm").assertIsDisplayed()
        composeTestRule.onNodeWithText("Điểm 10").assertIsDisplayed()
    }
    
    @Test
    fun allFilterOptionsAreDisplayed() {
        // Given
        val filterState = FilterState()
        val scheduledDates = listOf(LocalDate.of(2024, 11, 15))
        
        composeTestRule.setContent {
            FilterComboBox(
                currentFilter = filterState,
                scheduledDates = scheduledDates,
                onFilterChange = { _, _ -> }
            )
        }
        
        // When - Open dropdown
        composeTestRule.onNodeWithContentDescription("Tất cả").performClick()
        
        // Then - Verify all 4 filter options exist
        composeTestRule.onNodeWithText("Tất cả").assertExists()
        composeTestRule.onNodeWithText("Điểm kém (< 7)").assertExists()
        composeTestRule.onNodeWithText("Chưa có điểm").assertExists()
        composeTestRule.onNodeWithText("Điểm 10").assertExists()
    }
    
    @Test
    fun selectingAllFilterCallsCallback() {
        // Given
        var selectedType: FilterType? = null
        var selectedDate: String? = null
        val filterState = FilterState()
        val scheduledDates = listOf(LocalDate.of(2024, 11, 15))
        
        composeTestRule.setContent {
            FilterComboBox(
                currentFilter = filterState,
                scheduledDates = scheduledDates,
                onFilterChange = { type, date ->
                    selectedType = type
                    selectedDate = date
                }
            )
        }
        
        // When - Open dropdown and select "Tất cả"
        composeTestRule.onNodeWithContentDescription("Tất cả").performClick()
        composeTestRule.onNodeWithText("Tất cả").performClick()
        
        // Then
        assert(selectedType == FilterType.ALL)
        assert(selectedDate == null)
    }
    
    @Test
    fun selectingLowScoreFilterShowsDateDialog() {
        // Given
        val filterState = FilterState()
        val scheduledDates = listOf(
            LocalDate.of(2024, 11, 15),
            LocalDate.of(2024, 11, 18)
        )
        
        composeTestRule.setContent {
            FilterComboBox(
                currentFilter = filterState,
                scheduledDates = scheduledDates,
                onFilterChange = { _, _ -> }
            )
        }
        
        // When - Open dropdown and select "Điểm kém (< 7)"
        composeTestRule.onNodeWithContentDescription("Tất cả").performClick()
        composeTestRule.onNodeWithText("Điểm kém (< 7)").performClick()
        
        // Then - Date selection dialog should appear
        composeTestRule.onNodeWithText("Chọn ngày").assertIsDisplayed()
        composeTestRule.onNodeWithText("15/11/2024").assertIsDisplayed()
        composeTestRule.onNodeWithText("18/11/2024").assertIsDisplayed()
    }
    
    @Test
    fun selectingNoScoreFilterShowsDateDialog() {
        // Given
        val filterState = FilterState()
        val scheduledDates = listOf(LocalDate.of(2024, 11, 15))
        
        composeTestRule.setContent {
            FilterComboBox(
                currentFilter = filterState,
                scheduledDates = scheduledDates,
                onFilterChange = { _, _ -> }
            )
        }
        
        // When - Open dropdown and select "Chưa có điểm"
        composeTestRule.onNodeWithContentDescription("Tất cả").performClick()
        composeTestRule.onNodeWithText("Chưa có điểm").performClick()
        
        // Then - Date selection dialog should appear
        composeTestRule.onNodeWithText("Chọn ngày").assertIsDisplayed()
    }
    
    @Test
    fun selectingPerfectScoreFilterShowsDateDialog() {
        // Given
        val filterState = FilterState()
        val scheduledDates = listOf(LocalDate.of(2024, 11, 15))
        
        composeTestRule.setContent {
            FilterComboBox(
                currentFilter = filterState,
                scheduledDates = scheduledDates,
                onFilterChange = { _, _ -> }
            )
        }
        
        // When - Open dropdown and select "Điểm 10"
        composeTestRule.onNodeWithContentDescription("Tất cả").performClick()
        composeTestRule.onNodeWithText("Điểm 10").performClick()
        
        // Then - Date selection dialog should appear
        composeTestRule.onNodeWithText("Chọn ngày").assertIsDisplayed()
    }
    
    @Test
    fun currentFilterIsHighlighted() {
        // Given - Filter is set to LOW_SCORE
        val filterState = FilterState(type = FilterType.LOW_SCORE, selectedDate = "15/11/2024")
        val scheduledDates = listOf(LocalDate.of(2024, 11, 15))
        
        composeTestRule.setContent {
            FilterComboBox(
                currentFilter = filterState,
                scheduledDates = scheduledDates,
                onFilterChange = { _, _ -> }
            )
        }
        
        // When - Open dropdown
        composeTestRule.onNodeWithContentDescription("Điểm kém - 15/11/2024").performClick()
        
        // Then - Current filter should be visible (highlighted with primary color)
        composeTestRule.onNodeWithText("Điểm kém (< 7)").assertIsDisplayed()
    }
}
