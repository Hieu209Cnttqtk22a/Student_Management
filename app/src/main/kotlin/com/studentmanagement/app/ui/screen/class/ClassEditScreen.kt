package com.studentmanagement.app.ui.screen.`class`

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.studentmanagement.app.ui.component.PrimaryButton
import com.studentmanagement.app.ui.component.SecondaryButton
import com.studentmanagement.app.ui.theme.Primary
import com.studentmanagement.app.ui.viewmodel.ClassEditViewModel
import com.studentmanagement.app.ui.viewmodel.ClassEditUiState
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Composable
fun ClassEditScreen(
    navController: NavController,
    classId: Long,
    viewModel: ClassEditViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val className = remember { mutableStateOf("") }
    val selectedDays = remember { mutableStateOf(setOf<Int>()) }
    val startHour = remember { mutableStateOf("") }
    val startMinute = remember { mutableStateOf("") }
    val repeatInterval = remember { mutableStateOf("1") }
    val repeatUnit = remember { mutableStateOf("WEEK") }
    val showDeleteDialog = remember { mutableStateOf(false) }

    LaunchedEffect(classId) {
        viewModel.loadClass(classId)
    }

    LaunchedEffect(uiState) {
        if (uiState is ClassEditUiState.Success) {
            val classEntity = (uiState as ClassEditUiState.Success).classEntity
            className.value = classEntity.name
            
            // Parse schedule days
            try {
                val days = kotlinx.serialization.json.Json.decodeFromString<List<Int>>(classEntity.scheduleDaysOfWeek)
                selectedDays.value = days.toSet()
            } catch (e: Exception) {
                selectedDays.value = emptySet()
            }
            
            // Parse start time
            classEntity.startTimeMinutes?.let { minutes ->
                startHour.value = (minutes / 60).toString()
                startMinute.value = (minutes % 60).toString()
            }
            
            // Parse repeat settings
            repeatInterval.value = classEntity.repeatInterval.toString()
            repeatUnit.value = classEntity.repeatUnit
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Chỉnh sửa lớp học",
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
                actions = {
                    IconButton(onClick = { showDeleteDialog.value = true }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Xóa lớp",
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
        when (val state = uiState) {
            is ClassEditUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            is ClassEditUiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Lỗi: ${state.message}",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
            is ClassEditUiState.Success -> {
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
                                    
                                    viewModel.updateClass(
                                        classId = classId,
                                        name = className.value,
                                        scheduleDays = scheduleDaysJson,
                                        startTime = startTimeInMinutes,
                                        repeatInterval = interval,
                                        repeatUnit = repeatUnit.value
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

        // Delete confirmation dialog
        if (showDeleteDialog.value) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog.value = false },
                title = { Text("Xóa lớp học") },
                text = { Text("Bạn có chắc chắn muốn xóa lớp học này? Hành động này không thể hoàn tác.") },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.deleteClass(classId)
                            showDeleteDialog.value = false
                            navController.popBackStack()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("Xóa")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog.value = false }) {
                        Text("Hủy")
                    }
                }
            )
        }
    }
}


@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DayOfWeekSelector(
    selectedDays: Set<Int>,
    onDayToggle: (Int) -> Unit
) {
    val daysOfWeek = listOf(
        2 to "T2",
        3 to "T3",
        4 to "T4",
        5 to "T5",
        6 to "T6",
        7 to "T7",
        1 to "CN"
    )

    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        daysOfWeek.forEach { (dayValue, dayLabel) ->
            FilterChip(
                selected = selectedDays.contains(dayValue),
                onClick = { onDayToggle(dayValue) },
                label = { Text(dayLabel) }
            )
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
