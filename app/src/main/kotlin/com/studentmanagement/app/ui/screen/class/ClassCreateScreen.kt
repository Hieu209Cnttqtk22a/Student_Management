package com.studentmanagement.app.ui.screen.`class`

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.studentmanagement.app.ui.component.PrimaryButton
import com.studentmanagement.app.ui.component.SecondaryButton
import com.studentmanagement.app.ui.theme.Primary
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

data class ScheduleItem(
    val dayOfWeek: Int = 2, // Default Monday
    val hour: String = "",
    val minute: String = ""
)

@kotlinx.serialization.Serializable
data class ScheduleDay(
    val day: Int,
    val startTime: Int? = null // in minutes
)

@Composable
fun ClassCreateScreen(
    navController: NavController,
    onSave: (String, String, Int?, Int, String) -> Unit
) {
    val className = remember { mutableStateOf("") }
    val scheduleItems = remember { mutableStateListOf(ScheduleItem()) }
    val repeatInterval = remember { mutableStateOf("1") }
    val repeatUnit = remember { mutableStateOf("WEEK") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Tạo lớp học mới",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Quay lại",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Primary,
                    titleContentColor = Color.White
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                "Thông tin lớp học",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = className.value,
                onValueChange = { className.value = it },
                label = { Text("Tên lớp *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Giới hạn thời gian
            Text(
                "Giới hạn thời gian",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Làm trong", fontSize = 14.sp)
                
                OutlinedTextField(
                    value = repeatInterval.value,
                    onValueChange = { 
                        if (it.isEmpty() || (it.toIntOrNull() ?: 0) > 0) {
                            repeatInterval.value = it
                        }
                    },
                    modifier = Modifier.weight(0.3f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                
                RepeatUnitDropdown(
                    selectedUnit = repeatUnit.value,
                    onUnitSelected = { repeatUnit.value = it },
                    modifier = Modifier.weight(0.5f)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Lịch học
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Lịch học",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                // Nút thêm lịch
                IconButton(
                    onClick = { scheduleItems.add(ScheduleItem()) },
                    modifier = Modifier
                        .size(36.dp)
                        .background(Primary, RoundedCornerShape(8.dp))
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Thêm lịch",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Danh sách lịch học
            scheduleItems.forEachIndexed { index, item ->
                ScheduleItemCard(
                    item = item,
                    onItemChange = { newItem ->
                        scheduleItems[index] = newItem
                    },
                    onDelete = {
                        if (scheduleItems.size > 1) {
                            scheduleItems.removeAt(index)
                        }
                    },
                    canDelete = scheduleItems.size > 1
                )
                
                Spacer(modifier = Modifier.height(12.dp))
            }

            Spacer(modifier = Modifier.height(32.dp))

            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                SecondaryButton(
                    text = "Huỷ",
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.padding(8.dp))
                PrimaryButton(
                    text = "Lưu",
                    onClick = {
                        if (className.value.isNotBlank()) {
                            // Create ScheduleDay objects with time for each day
                            val scheduleDays = scheduleItems.map { item ->
                                val startTime = if (item.hour.isNotBlank() && item.minute.isNotBlank()) {
                                    (item.hour.toIntOrNull() ?: 0) * 60 + (item.minute.toIntOrNull() ?: 0)
                                } else null
                                ScheduleDay(day = item.dayOfWeek, startTime = startTime)
                            }
                            val scheduleDaysJson = Json.encodeToString(scheduleDays)
                            
                            // Use first item's time as default startTime for backward compatibility
                            val firstItem = scheduleItems.firstOrNull()
                            val startTimeInMinutes = if (firstItem != null && 
                                firstItem.hour.isNotBlank() && 
                                firstItem.minute.isNotBlank()) {
                                (firstItem.hour.toIntOrNull() ?: 0) * 60 + 
                                (firstItem.minute.toIntOrNull() ?: 0)
                            } else null
                            
                            val interval = repeatInterval.value.toIntOrNull() ?: 1
                            
                            onSave(
                                className.value,
                                scheduleDaysJson,
                                startTimeInMinutes,
                                interval,
                                repeatUnit.value
                            )
                            // Navigation is handled in AppNavigation.kt
                        }
                    },
                    modifier = Modifier.weight(1f),
                    enabled = className.value.isNotBlank()
                )
            }
        }
    }
}

@Composable
fun ScheduleItemCard(
    item: ScheduleItem,
    onItemChange: (ScheduleItem) -> Unit,
    onDelete: () -> Unit,
    canDelete: Boolean
) {
    var isExpanded by remember { mutableStateOf(true) }
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { isExpanded = !isExpanded },
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Buổi học",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Primary
            )
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (canDelete && isExpanded) {
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Xóa",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                
                Icon(
                    imageVector = if (isExpanded) 
                        Icons.Default.KeyboardArrowUp 
                    else 
                        Icons.Default.KeyboardArrowDown,
                    contentDescription = if (isExpanded) "Thu gọn" else "Mở rộng",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (isExpanded) {
            Spacer(modifier = Modifier.height(12.dp))

        // Chọn thứ
        Text(
            "Thứ trong tuần",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        DayOfWeekDropdown(
            selectedDay = item.dayOfWeek,
            onDaySelected = { onItemChange(item.copy(dayOfWeek = it)) }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Chọn giờ
        Text(
            "Thời gian bắt đầu",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = item.hour,
                onValueChange = { 
                    if (it.isEmpty() || (it.toIntOrNull() in 0..23)) {
                        onItemChange(item.copy(hour = it))
                    }
                },
                label = { Text("Giờ") },
                modifier = Modifier.weight(1f),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            
            Text(
                ":",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            
            OutlinedTextField(
                value = item.minute,
                onValueChange = { 
                    if (it.isEmpty() || (it.toIntOrNull() in 0..59)) {
                        onItemChange(item.copy(minute = it))
                    }
                },
                label = { Text("Phút") },
                modifier = Modifier.weight(1f),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
        }
        }
    }
}

@Composable
fun DayOfWeekDropdown(
    selectedDay: Int,
    onDaySelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val daysOfWeek = mapOf(
        2 to "Thứ 2",
        3 to "Thứ 3",
        4 to "Thứ 4",
        5 to "Thứ 5",
        6 to "Thứ 6",
        7 to "Thứ 7",
        1 to "Chủ nhật"
    )

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = daysOfWeek[selectedDay] ?: "Thứ 2",
            onValueChange = {},
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            singleLine = true
        )
        
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            daysOfWeek.forEach { (key, label) ->
                DropdownMenuItem(
                    text = { Text(label) },
                    onClick = {
                        onDaySelected(key)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun RepeatUnitDropdown(
    selectedUnit: String,
    onUnitSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val units = mapOf(
        "WEEK" to "Tuần",
        "MONTH" to "Tháng",
        "YEAR" to "Năm"
    )

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = units[selectedUnit] ?: "Tuần",
            onValueChange = {},
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            singleLine = true
        )
        
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            units.forEach { (key, label) ->
                DropdownMenuItem(
                    text = { Text(label) },
                    onClick = {
                        onUnitSelected(key)
                        expanded = false
                    }
                )
            }
        }
    }
}
