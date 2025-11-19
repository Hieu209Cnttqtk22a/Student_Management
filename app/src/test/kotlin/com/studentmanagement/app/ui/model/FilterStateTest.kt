package com.studentmanagement.app.ui.model

import org.junit.Assert.*
import org.junit.Test

class FilterStateTest {
    
    @Test
    fun `isActive returns false when filter type is ALL`() {
        val filterState = FilterState(type = FilterType.ALL)
        assertFalse(filterState.isActive())
    }
    
    @Test
    fun `isActive returns true when filter type is LOW_SCORE`() {
        val filterState = FilterState(type = FilterType.LOW_SCORE, selectedDate = "15/11/2025")
        assertTrue(filterState.isActive())
    }
    
    @Test
    fun `isActive returns true when filter type is NO_SCORE`() {
        val filterState = FilterState(type = FilterType.NO_SCORE, selectedDate = "15/11/2025")
        assertTrue(filterState.isActive())
    }
    
    @Test
    fun `isActive returns true when filter type is PERFECT_SCORE`() {
        val filterState = FilterState(type = FilterType.PERFECT_SCORE, selectedDate = "15/11/2025")
        assertTrue(filterState.isActive())
    }
    
    @Test
    fun `getDisplayLabel returns correct label for ALL filter`() {
        val filterState = FilterState(type = FilterType.ALL)
        assertEquals("Tất cả", filterState.getDisplayLabel())
    }
    
    @Test
    fun `getDisplayLabel returns correct label for LOW_SCORE filter without date`() {
        val filterState = FilterState(type = FilterType.LOW_SCORE, selectedDate = null)
        assertEquals("Điểm kém", filterState.getDisplayLabel())
    }
    
    @Test
    fun `getDisplayLabel returns correct label for LOW_SCORE filter with date`() {
        val filterState = FilterState(type = FilterType.LOW_SCORE, selectedDate = "15/11/2025")
        assertEquals("Điểm kém - 15/11/2025", filterState.getDisplayLabel())
    }
    
    @Test
    fun `getDisplayLabel returns correct label for NO_SCORE filter without date`() {
        val filterState = FilterState(type = FilterType.NO_SCORE, selectedDate = null)
        assertEquals("Chưa có điểm", filterState.getDisplayLabel())
    }
    
    @Test
    fun `getDisplayLabel returns correct label for NO_SCORE filter with date`() {
        val filterState = FilterState(type = FilterType.NO_SCORE, selectedDate = "20/11/2025")
        assertEquals("Chưa có điểm - 20/11/2025", filterState.getDisplayLabel())
    }
    
    @Test
    fun `getDisplayLabel returns correct label for PERFECT_SCORE filter without date`() {
        val filterState = FilterState(type = FilterType.PERFECT_SCORE, selectedDate = null)
        assertEquals("Điểm 10", filterState.getDisplayLabel())
    }
    
    @Test
    fun `getDisplayLabel returns correct label for PERFECT_SCORE filter with date`() {
        val filterState = FilterState(type = FilterType.PERFECT_SCORE, selectedDate = "22/11/2025")
        assertEquals("Điểm 10 - 22/11/2025", filterState.getDisplayLabel())
    }
    
    @Test
    fun `FilterType enum has all expected values`() {
        val values = FilterType.values()
        assertEquals(4, values.size)
        assertTrue(values.contains(FilterType.ALL))
        assertTrue(values.contains(FilterType.LOW_SCORE))
        assertTrue(values.contains(FilterType.NO_SCORE))
        assertTrue(values.contains(FilterType.PERFECT_SCORE))
    }
    
    @Test
    fun `FilterState default constructor creates ALL filter with no date`() {
        val filterState = FilterState()
        assertEquals(FilterType.ALL, filterState.type)
        assertNull(filterState.selectedDate)
        assertFalse(filterState.isActive())
    }
    
    @Test
    fun `FilterState data class equality works correctly`() {
        val filterState1 = FilterState(type = FilterType.LOW_SCORE, selectedDate = "15/11/2025")
        val filterState2 = FilterState(type = FilterType.LOW_SCORE, selectedDate = "15/11/2025")
        val filterState3 = FilterState(type = FilterType.LOW_SCORE, selectedDate = "16/11/2025")
        
        assertEquals(filterState1, filterState2)
        assertNotEquals(filterState1, filterState3)
    }
}
