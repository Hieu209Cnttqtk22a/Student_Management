package com.studentmanagement.app.ui.component

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import org.junit.Rule
import org.junit.Test

class ResetFilterButtonTest {
    
    @get:Rule
    val composeTestRule = createComposeRule()
    
    @Test
    fun buttonIsVisibleWhenFilterIsActive() {
        // Given - Filter is active
        composeTestRule.setContent {
            ResetFilterButton(
                isVisible = true,
                onReset = { }
            )
        }
        
        // Then - Button should be displayed
        composeTestRule.onNodeWithContentDescription("Xóa bộ lọc").assertIsDisplayed()
    }
    
    @Test
    fun buttonIsHiddenWhenFilterIsAll() {
        // Given - No filter is active (ALL filter)
        composeTestRule.setContent {
            ResetFilterButton(
                isVisible = false,
                onReset = { }
            )
        }
        
        // Then - Button should not exist in the composition
        composeTestRule.onNodeWithContentDescription("Xóa bộ lọc").assertDoesNotExist()
    }
    
    @Test
    fun clickingButtonCallsResetCallback() {
        // Given
        var resetCalled = false
        
        composeTestRule.setContent {
            ResetFilterButton(
                isVisible = true,
                onReset = { resetCalled = true }
            )
        }
        
        // When - Click the reset button
        composeTestRule.onNodeWithContentDescription("Xóa bộ lọc").performClick()
        
        // Then - Reset callback should be invoked
        assert(resetCalled)
    }
    
    @Test
    fun buttonTransitionsFromHiddenToVisible() {
        // Given - Start with button hidden
        var isVisible = false
        
        composeTestRule.setContent {
            ResetFilterButton(
                isVisible = isVisible,
                onReset = { }
            )
        }
        
        // Verify button is not visible
        composeTestRule.onNodeWithContentDescription("Xóa bộ lọc").assertDoesNotExist()
        
        // When - Change visibility to true
        composeTestRule.runOnUiThread {
            isVisible = true
        }
        
        composeTestRule.setContent {
            ResetFilterButton(
                isVisible = isVisible,
                onReset = { }
            )
        }
        
        // Then - Button should now be visible
        composeTestRule.onNodeWithContentDescription("Xóa bộ lọc").assertIsDisplayed()
    }
    
    @Test
    fun buttonTransitionsFromVisibleToHidden() {
        // Given - Start with button visible
        var isVisible = true
        
        composeTestRule.setContent {
            ResetFilterButton(
                isVisible = isVisible,
                onReset = { }
            )
        }
        
        // Verify button is visible
        composeTestRule.onNodeWithContentDescription("Xóa bộ lọc").assertIsDisplayed()
        
        // When - Change visibility to false
        composeTestRule.runOnUiThread {
            isVisible = false
        }
        
        composeTestRule.setContent {
            ResetFilterButton(
                isVisible = isVisible,
                onReset = { }
            )
        }
        
        // Then - Button should now be hidden
        composeTestRule.onNodeWithContentDescription("Xóa bộ lọc").assertDoesNotExist()
    }
}
