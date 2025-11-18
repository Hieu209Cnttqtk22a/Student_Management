package com.studentmanagement.app.ui.screen.`class`

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
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

@Composable
fun ClassCreateScreen(
    navController: NavController,
    onSave: (String, String, Int?, Int, String) -> Unit
) {
    val className = remember { mutableStateOf("") }
    val selectedDays = remember { mutableStateOf(setOf<Int>()) }
    val startHour = remember { mutableStateOf("") }
    val startMinute = remember { mutableStateOf("") }
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

            Text(
                "Lịch học",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            DayOfWeekSelector(
                selectedDays = selectedDays.value,
                onDayToggle = { day ->
                    selectedDays.value = if (selectedDays.value.contains(day)) {
                        selectedDays.value - day
                    } else {
                        selectedDays.value + day
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                "Thời gian học",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = startHour.value,
                    onValueChange = { 
                        if (it.isEmpty() || (it.toIntOrNull() in 0..23)) {
                            startHour.value = it
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
                    modifier = Modifier.align(Alignment.CenterVertically)
                )
                
                OutlinedTextField(
                    value = startMinute.value,
                    onValueChange = { 
                        if (it.isEmpty() || (it.toIntOrNull() in 0..59)) {
                            startMinute.value = it
                        }
                    },
                    label = { Text("Phút") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                "Tần suất lặp lại",
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
                Text("Lặp lại mỗi", fontSize = 14.sp)
                
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
                            val scheduleDaysJson = Json.encodeToString(selectedDays.value.toList())
                            val startTimeInMinutes = if (startHour.value.isNotBlank() && startMinute.value.isNotBlank()) {
                                (startHour.value.toIntOrNull() ?: 0) * 60 + (startMinute.value.toIntOrNull() ?: 0)
                            } else null
                            val interval = repeatInterval.value.toIntOrNull() ?: 1
                            
                            onSave(
                                className.value,
                                scheduleDaysJson,
                                startTimeInMinutes,
                                interval,
                                repeatUnit.value
                            )
                            navController.popBackStack()
                        }
                    },
                    modifier = Modifier.weight(1f),
                    enabled = className.value.isNotBlank()
                )
            }
        }
    }
}
