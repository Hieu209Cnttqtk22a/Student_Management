package com.studentmanagement.app.ui.screen.`class`

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.runtime.mutableStateListOf
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
    val scheduleItems = remember { mutableStateListOf<ScheduleItem>() }
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
            
            // Parse schedule days and create schedule items
            try {
                val days = Json.decodeFromString<List<Int>>(classEntity.scheduleDaysOfWeek)
                if (days.isNotEmpty() && scheduleItems.isEmpty()) {
                    // Parse start time
                    val hour = classEntity.startTimeMinutes?.let { (it / 60).toString() } ?: ""
                    val minute = classEntity.startTimeMinutes?.let { (it % 60).toString() } ?: ""
                    
                    // Create schedule items for each day
                    scheduleItems.clear()
                    days.forEach { day ->
                        scheduleItems.add(ScheduleItem(day, hour, minute))
                    }
                }
            } catch (e: Exception) {
                if (scheduleItems.isEmpty()) {
                    scheduleItems.add(ScheduleItem())
                }
            }
            
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
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
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

                    // Tần suất lặp lại
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
                                    val selectedDays = scheduleItems.map { it.dayOfWeek }.toSet()
                                    val scheduleDaysJson = Json.encodeToString(selectedDays.toList())
                                    
                                    val firstItem = scheduleItems.firstOrNull()
                                    val startTimeInMinutes = if (firstItem != null && 
                                        firstItem.hour.isNotBlank() && 
                                        firstItem.minute.isNotBlank()) {
                                        (firstItem.hour.toIntOrNull() ?: 0) * 60 + 
                                        (firstItem.minute.toIntOrNull() ?: 0)
                                    } else null
                                    
                                    val interval = repeatInterval.value.toIntOrNull() ?: 1
                                    
                                    viewModel.updateClass(
                                        classId = classId,
                                        name = className.value,
                                        scheduleDaysOfWeek = scheduleDaysJson,
                                        startTimeMinutes = startTimeInMinutes,
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
            is ClassEditUiState.Error -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Lỗi: ${state.message}",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }

    // Delete confirmation dialog
    if (showDeleteDialog.value) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog.value = false },
            title = { Text("Xác nhận xóa") },
            text = { Text("Bạn có chắc chắn muốn xóa lớp học này?") },
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
