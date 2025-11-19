package com.studentmanagement.app.ui.component

import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import com.studentmanagement.app.ui.model.FilterState
import com.studentmanagement.app.ui.model.FilterType
import java.time.LocalDate

/**
 * FilterComboBox component that displays a dropdown menu for selecting student score filters.
 * 
 * @param currentFilter The current filter state
 * @param scheduledDates List of scheduled dates available for date-specific filters
 * @param onFilterChange Callback invoked when filter changes with FilterType and optional date
 * @param modifier Modifier for the component
 */
@Composable
fun FilterComboBox(
    currentFilter: FilterState,
    scheduledDates: List<LocalDate>,
    onFilterChange: (FilterType, String?) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    var showDateDialog by remember { mutableStateOf(false) }
    var pendingFilterType by remember { mutableStateOf<FilterType?>(null) }
    
    // Use remember for stable display label to minimize recompositions
    val displayLabel = remember(currentFilter) { currentFilter.getDisplayLabel() }
    
    Box(modifier = modifier) {
        // Dropdown button showing current filter label
        IconButton(onClick = { expanded = true }) {
            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = displayLabel,
                tint = Color.White
            )
        }
        
        // Dropdown menu with filter options
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            // All filter option
            DropdownMenuItem(
                text = {
                    Text(
                        text = "Tất cả",
                        fontSize = 14.sp,
                        color = if (currentFilter.type == FilterType.ALL) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        }
                    )
                },
                onClick = {
                    expanded = false
                    onFilterChange(FilterType.ALL, null)
                }
            )
            
            // Low score filter option
            DropdownMenuItem(
                text = {
                    Text(
                        text = "Điểm kém (< 7)",
                        fontSize = 14.sp,
                        color = if (currentFilter.type == FilterType.LOW_SCORE) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        }
                    )
                },
                onClick = {
                    expanded = false
                    pendingFilterType = FilterType.LOW_SCORE
                    showDateDialog = true
                }
            )
            
            // No score filter option
            DropdownMenuItem(
                text = {
                    Text(
                        text = "Chưa có điểm",
                        fontSize = 14.sp,
                        color = if (currentFilter.type == FilterType.NO_SCORE) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        }
                    )
                },
                onClick = {
                    expanded = false
                    pendingFilterType = FilterType.NO_SCORE
                    showDateDialog = true
                }
            )
            
            // Perfect score filter option
            DropdownMenuItem(
                text = {
                    Text(
                        text = "Điểm 10",
                        fontSize = 14.sp,
                        color = if (currentFilter.type == FilterType.PERFECT_SCORE) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        }
                    )
                },
                onClick = {
                    expanded = false
                    pendingFilterType = FilterType.PERFECT_SCORE
                    showDateDialog = true
                }
            )
        }
    }
    
    // Date selection dialog for date-specific filters
    if (showDateDialog && pendingFilterType != null) {
        DateSelectionDialog(
            scheduledDates = scheduledDates,
            onDateSelected = { selectedDate ->
                onFilterChange(pendingFilterType!!, selectedDate)
                showDateDialog = false
                pendingFilterType = null
            },
            onDismiss = {
                showDateDialog = false
                pendingFilterType = null
            }
        )
    }
}
