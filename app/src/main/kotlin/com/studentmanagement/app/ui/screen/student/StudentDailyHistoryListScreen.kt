package com.studentmanagement.app.ui.screen.student

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.studentmanagement.app.ui.component.DailyRecordCard
import com.studentmanagement.app.ui.theme.Primary
import com.studentmanagement.app.ui.viewmodel.StudentHistoryViewModel
import com.studentmanagement.app.ui.viewmodel.StudentHistoryUiState

@Composable
fun StudentDailyHistoryListScreen(
    navController: NavController,
    studentId: Long,
    studentName: String = "",
    viewModel: StudentHistoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val currentPage by viewModel.currentPage.collectAsState()
    val pageSize by viewModel.pageSize.collectAsState()

    LaunchedEffect(studentId) {
        viewModel.loadStudentHistory(studentId)
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Lịch sử học tập",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            studentName,
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
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
                    IconButton(onClick = { /* Open filter */ }) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = "Lọc",
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
            is StudentHistoryUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            is StudentHistoryUiState.Error -> {
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
            is StudentHistoryUiState.Empty -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Chưa có dữ liệu học tập",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            is StudentHistoryUiState.Success -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                        .padding(innerPadding)
                ) {
                    // Filter section
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Date range dropdown
                        var dateRangeExpanded by remember { mutableStateOf(false) }
                        var selectedDateRange by remember { mutableStateOf("Khoảng thời gian") }
                        val dateRangeOptions = listOf("Khoảng thời gian", "7 ngày qua", "30 ngày qua", "Tất cả")
                        
                        androidx.compose.material3.ExposedDropdownMenuBox(
                            expanded = dateRangeExpanded,
                            onExpandedChange = { dateRangeExpanded = it },
                            modifier = Modifier.weight(1f)
                        ) {
                            OutlinedTextField(
                                value = selectedDateRange,
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = {
                                    androidx.compose.material3.ExposedDropdownMenuDefaults.TrailingIcon(expanded = dateRangeExpanded)
                                },
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth(),
                                textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp),
                                colors = androidx.compose.material3.ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                            )
                            DropdownMenu(
                                expanded = dateRangeExpanded,
                                onDismissRequest = { dateRangeExpanded = false }
                            ) {
                                dateRangeOptions.forEach { option ->
                                    DropdownMenuItem(
                                        text = { Text(option, fontSize = 12.sp) },
                                        onClick = {
                                            selectedDateRange = option
                                            dateRangeExpanded = false
                                            // TODO: Apply date range filter
                                        }
                                    )
                                }
                            }
                        }
                        
                        // Page size dropdown
                        var pageSizeExpanded by remember { mutableStateOf(false) }
                        val pageSizeOptions = listOf(10, 20, 50)
                        
                        androidx.compose.material3.ExposedDropdownMenuBox(
                            expanded = pageSizeExpanded,
                            onExpandedChange = { pageSizeExpanded = it },
                            modifier = Modifier.weight(1f)
                        ) {
                            OutlinedTextField(
                                value = "$pageSize bản ghi",
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = {
                                    androidx.compose.material3.ExposedDropdownMenuDefaults.TrailingIcon(expanded = pageSizeExpanded)
                                },
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth(),
                                textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp),
                                colors = androidx.compose.material3.ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                            )
                            DropdownMenu(
                                expanded = pageSizeExpanded,
                                onDismissRequest = { pageSizeExpanded = false }
                            ) {
                                pageSizeOptions.forEach { size ->
                                    DropdownMenuItem(
                                        text = { Text("$size bản ghi", fontSize = 12.sp) },
                                        onClick = {
                                            viewModel.setPageSize(size)
                                            pageSizeExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Stats summary
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "Tổng: ${state.allRecords.size} buổi học",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (state.averageScore > 0) {
                            Text(
                                "Điểm TB: ${String.format("%.1f", state.averageScore)}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Primary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Records list
                    val currentPageRecords = if (currentPage < state.pagedRecords.size) {
                        state.pagedRecords[currentPage]
                    } else {
                        emptyList()
                    }

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(currentPageRecords) { record ->
                            DailyRecordCard(
                                date = record.date,
                                score = record.score,
                                tags = record.tags,
                                imageCount = record.imageUrls.size,
                                onClick = { 
                                    navController.navigate("record/${record.id}/detail")
                                }
                            )
                        }

                        if (state.totalPages > 1) {
                            item {
                                // Pagination
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 16.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    OutlinedButton(
                                        onClick = { viewModel.previousPage() },
                                        enabled = currentPage > 0,
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("Trang trước", fontSize = 12.sp)
                                    }
                                    Text(
                                        "${currentPage + 1} / ${state.totalPages}",
                                        fontSize = 12.sp,
                                        modifier = Modifier.padding(horizontal = 16.dp),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    OutlinedButton(
                                        onClick = { viewModel.nextPage() },
                                        enabled = currentPage < state.totalPages - 1,
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("Trang sau", fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
