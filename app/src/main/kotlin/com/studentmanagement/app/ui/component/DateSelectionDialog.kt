package com.studentmanagement.app.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * DateSelectionDialog component that displays a dialog for selecting a date from scheduled dates.
 * 
 * @param scheduledDates List of scheduled dates to choose from
 * @param onDateSelected Callback invoked when a date is selected, receives date in dd/MM/yyyy format
 * @param onDismiss Callback invoked when dialog is dismissed
 */
@Composable
fun DateSelectionDialog(
    scheduledDates: List<LocalDate>,
    onDateSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val today = LocalDate.now()
    val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Chọn ngày",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            LazyColumn {
                items(
                    items = scheduledDates,
                    key = { date -> date.toString() }
                ) { date ->
                    val dateString = date.format(dateFormatter)
                    val isToday = date == today
                    
                    Text(
                        text = if (isToday) "$dateString (Hôm nay)" else dateString,
                        fontSize = 14.sp,
                        color = if (isToday) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        },
                        fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onDateSelected(dateString) }
                            .padding(vertical = 12.dp, horizontal = 16.dp)
                    )
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Hủy")
            }
        }
    )
}
