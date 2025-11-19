package com.studentmanagement.app.ui.screen.`class`

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.studentmanagement.app.data.entity.StudentEntity
import com.studentmanagement.app.ui.component.FilterComboBox
import com.studentmanagement.app.ui.component.ResetFilterButton
import com.studentmanagement.app.ui.theme.Primary
import com.studentmanagement.app.ui.theme.PrimaryLight
import com.studentmanagement.app.ui.viewmodel.ClassDetailViewModel
import com.studentmanagement.app.ui.viewmodel.ClassDetailUiState
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

@Composable
fun ClassDetailScreen(
    navController: NavController,
    classId: Long,
    viewModel: ClassDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val dailyRecords by viewModel.dailyRecords.collectAsState()
    val filterState by viewModel.filterState.collectAsState()
    val focusManager = LocalFocusManager.current

    // Get reload trigger from savedStateHandle
    val reloadTrigger = navController.currentBackStackEntry
        ?.savedStateHandle
        ?.getStateFlow("reload_trigger", 0L)
        ?.collectAsState()
    
    // Load class detail when screen is first created OR when reload trigger changes
    LaunchedEffect(classId, reloadTrigger?.value) {
        android.util.Log.d("ClassDetailScreen", "LaunchedEffect triggered: classId=$classId, reloadTrigger=${reloadTrigger?.value}")
        viewModel.loadClassDetail(classId)
    }

    val className = when (val state = uiState) {
        is ClassDetailUiState.Success -> state.classEntity.name
        else -> "..."
    }
    
    val scheduledDates = when (val state = uiState) {
        is ClassDetailUiState.Success -> state.scheduledDates
        else -> emptyList()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Lớp $className",
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
                    IconButton(onClick = { viewModel.loadClassDetail(classId) }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Làm mới",
                            tint = Color.White
                        )
                    }
                    // Requirement 1.1: Import Students button
                    IconButton(onClick = { 
                        navController.navigate("class/$classId/import")
                    }) {
                        Icon(
                            imageVector = Icons.Default.FileUpload,
                            contentDescription = "Import Students",
                            tint = Color.White
                        )
                    }
                    FilterComboBox(
                        currentFilter = filterState,
                        scheduledDates = scheduledDates,
                        onFilterChange = { type, date ->
                            viewModel.setFilter(type, date)
                        }
                    )
                    ResetFilterButton(
                        isVisible = filterState.isActive(),
                        onReset = { viewModel.resetFilter() }
                    )
                    IconButton(onClick = { 
                        navController.navigate("class/$classId/edit")
                    }) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Chỉnh sửa",
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
            is ClassDetailUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            is ClassDetailUiState.Error -> {
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
            is ClassDetailUiState.Success -> {
                val filteredStudents by viewModel.filteredStudents.collectAsState()
                
                // Use derivedStateOf to compute the display list efficiently
                // This minimizes recompositions by only updating when dependencies change
                val displayStudents by remember(filterState, filteredStudents, state.students) {
                    androidx.compose.runtime.derivedStateOf {
                        if (filterState.isActive()) {
                            filteredStudents
                        } else {
                            state.students
                        }
                    }
                }
                
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .clickable(
                            indication = null,
                            interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
                        ) {
                            // Clear focus when clicking outside the grid
                            focusManager.clearFocus()
                        }
                ) {
                    StudentGridView(
                        classId = classId,
                        students = displayStudents,
                        scheduledDates = state.scheduledDates,
                        dailyRecords = dailyRecords,
                        filterState = filterState,
                        viewModel = viewModel,
                        onStudentClick = { studentId, dateString ->
                            navController.navigate("student/$studentId/daily/edit?classId=$classId&date=$dateString")
                        },
                        onStudentDetailClick = { studentId ->
                            navController.navigate("student/$studentId/history")
                        },
                        onRefresh = {
                            viewModel.refreshDailyRecords(classId)
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}

@Composable
fun StudentGridView(
    classId: Long,
    students: List<com.studentmanagement.app.data.entity.StudentEntity>,
    scheduledDates: List<LocalDate>,
    dailyRecords: Map<Pair<Long, String>, Float?>,
    filterState: com.studentmanagement.app.ui.model.FilterState,
    viewModel: ClassDetailViewModel,
    onStudentClick: (Long, String) -> Unit,
    onStudentDetailClick: (Long) -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Sort students by first name (Tên) - always sort when students list changes
    val sortedStudents = students.sortedBy { student ->
        // Get the last word as first name (Tên)
        student.name.split(" ").lastOrNull() ?: student.name
    }
    
    // Check for empty scheduled dates
    if (scheduledDates.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarToday,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Lớp học chưa có lịch học được cấu hình",
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Vui lòng chỉnh sửa lớp học để thêm lịch học",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
        return
    }
    
    // Check for empty filtered list when filter is active
    if (filterState.isActive() && sortedStudents.isEmpty()) {
        val emptyMessage = when (filterState.type) {
            com.studentmanagement.app.ui.model.FilterType.LOW_SCORE -> 
                "Không có học sinh nào có điểm dưới 7 trong ngày này"
            com.studentmanagement.app.ui.model.FilterType.NO_SCORE -> 
                "Tất cả học sinh đã có điểm trong ngày này"
            com.studentmanagement.app.ui.model.FilterType.PERFECT_SCORE -> 
                "Không có học sinh nào đạt điểm 10 trong ngày này"
            else -> ""
        }
        
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.FilterList,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = emptyMessage,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
        return
    }
    
    // Zoom state - only allow zoom out for date columns
    var zoomScale by remember { mutableStateOf(1f) }
    val minZoom = 0.3f  // Allow zoom out to see more columns
    val maxZoom = 1.0f  // No zoom in allowed
    
    // Base dimensions
    val baseDetailWidth = 40.dp
    val baseSttWidth = 50.dp
    val baseColumnWidth = 100.dp
    val baseRowHeight = 45.dp
    
    // Fixed columns (Detail, STT, Họ, Tên) keep original size - no zoom
    val detailWidth = baseDetailWidth
    val sttWidth = baseSttWidth
    val fixedColumnWidth = baseColumnWidth  // Họ and Tên keep 100dp
    
    // Only date columns zoom out
    val columnWidth = baseColumnWidth * zoomScale
    val rowHeight = baseRowHeight * zoomScale
    
    val leftScrollableWidth = detailWidth
    val fixedCenterWidth = sttWidth + (fixedColumnWidth * 2) // STT + Họ + Tên (no zoom)
    val rightScrollableWidth = columnWidth * scheduledDates.size
    val totalWidth = leftScrollableWidth + fixedCenterWidth + rightScrollableWidth
    
    // Shared scroll states
    val leftScrollState = rememberScrollState()
    val rightScrollState = rememberScrollState()
    
    // Get screen width
    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    
    // Only enable scroll if content is wider than screen
    val enableScroll = totalWidth > screenWidth
    
    // Tìm ngày gần nhất với hiện tại để scroll đến
    val today = LocalDate.now()
    val nearestDateIndex = remember(scheduledDates) {
        scheduledDates.indexOfFirst { it >= today }.takeIf { it >= 0 } ?: (scheduledDates.size - 1)
    }
    
    // Auto scroll to nearest date on first load
    LaunchedEffect(nearestDateIndex) {
        if (enableScroll && nearestDateIndex >= 0) {
            val scrollPosition = (nearestDateIndex * columnWidth.value).toInt()
            rightScrollState.scrollTo(scrollPosition)
        }
    }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Grid table with independent scroll for left and right sections
        Column(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTransformGestures { _, _, zoom, _ ->
                        val newScale = (zoomScale * zoom).coerceIn(minZoom, maxZoom)
                        zoomScale = newScale
                    }
                }
        ) {
            // Header row - scrollable left + fixed center + scrollable right
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(rowHeight)
            ) {
                // Scrollable left section (Icon only - no text)
                Row(
                    modifier = if (enableScroll) {
                        Modifier.horizontalScroll(leftScrollState)
                    } else {
                        Modifier
                    }
                ) {
                    GridHeaderCell("", width = detailWidth) // Empty text
                }
                
                // Fixed center section (STT + Họ + Tên) - no zoom
                GridHeaderCell("STT", width = sttWidth)
                GridHeaderCell("Họ", width = fixedColumnWidth)
                GridHeaderCell("Tên", width = fixedColumnWidth)
                
                // Scrollable right section (dates only)
                Row(
                    modifier = if (enableScroll) {
                        Modifier
                            .weight(1f)
                            .horizontalScroll(rightScrollState)
                    } else {
                        Modifier.weight(1f)
                    }
                ) {
                    // Dynamically generate date column headers
                    scheduledDates.forEach { date ->
                        val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
                        val dateString = date.format(dateFormatter)
                        GridHeaderCell(dateString, width = columnWidth)
                    }
                }
            }
            
            // Data rows - scrollable vertically with independent horizontal scroll
            // Use graphicsLayer for hardware acceleration on high refresh rate displays
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        // Enable hardware acceleration for smooth scrolling at 120Hz/144Hz
                    }
            ) {
                // Existing students with stable keys for optimal recomposition
                itemsIndexed(
                    items = sortedStudents,
                    key = { _, student -> student.id }
                ) { index, student ->
                    EditableStudentRow(
                        index = index + 1,
                        student = student,
                        scheduledDates = scheduledDates,
                        dailyRecords = dailyRecords,
                        viewModel = viewModel,
                        leftScrollState = leftScrollState,
                        rightScrollState = rightScrollState,
                        detailWidth = detailWidth,
                        sttWidth = sttWidth,
                        fixedColumnWidth = fixedColumnWidth,
                        columnWidth = columnWidth,
                        enableScroll = enableScroll,
                        onStudentClick = { dateString ->
                            onStudentClick(student.id, dateString)
                            // Refresh after returning from edit screen
                            onRefresh()
                        },
                        onStudentDetailClick = onStudentDetailClick
                    )
                }
                
                // Empty row for adding new student
                item {
                    NewStudentRow(
                        index = sortedStudents.size + 1,
                        classId = classId,
                        scheduledDates = scheduledDates,
                        viewModel = viewModel,
                        leftScrollState = leftScrollState,
                        rightScrollState = rightScrollState,
                        detailWidth = detailWidth,
                        sttWidth = sttWidth,
                        fixedColumnWidth = fixedColumnWidth,
                        columnWidth = columnWidth,
                        enableScroll = enableScroll
                    )
                }
            }
        }
    }
}

@Composable
fun EditableStudentRow(
    index: Int,
    student: StudentEntity,
    scheduledDates: List<LocalDate>,
    dailyRecords: Map<Pair<Long, String>, Float?>,
    viewModel: ClassDetailViewModel,
    leftScrollState: ScrollState,
    rightScrollState: ScrollState,
    detailWidth: androidx.compose.ui.unit.Dp,
    sttWidth: androidx.compose.ui.unit.Dp,
    fixedColumnWidth: androidx.compose.ui.unit.Dp,
    columnWidth: androidx.compose.ui.unit.Dp,
    enableScroll: Boolean,
    onStudentClick: (String) -> Unit,
    onStudentDetailClick: (Long) -> Unit
) {
    val nameParts = student.name.split(" ")
    var lastName by remember(student.id, student.name) { 
        mutableStateOf(if (nameParts.size > 1) nameParts.dropLast(1).joinToString(" ") else "") 
    }
    var firstName by remember(student.id, student.name) { 
        mutableStateOf(nameParts.lastOrNull() ?: student.name) 
    }
    
    val focusManager = LocalFocusManager.current
    
    // Save when focus is lost (user finished editing)
    fun saveIfChanged() {
        if (lastName.isNotBlank() && firstName.isNotBlank()) {
            val newFullName = "$lastName $firstName".trim()
            if (newFullName != student.name) {
                viewModel.updateStudentName(student.id, newFullName)
            }
        }
    }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(45.dp)
    ) {
        // Scrollable left section (Chi tiết only)
        Row(
            modifier = if (enableScroll) {
                Modifier.horizontalScroll(leftScrollState)
            } else {
                Modifier
            }
        ) {
            // Detail icon
            Box(
                modifier = Modifier
                    .width(detailWidth)
                    .height(45.dp)
                    .border(
                        width = 0.5.dp,
                        color = MaterialTheme.colorScheme.outline
                    )
                    .clickable { onStudentDetailClick(student.id) }
                    .background(MaterialTheme.colorScheme.surface),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Visibility,
                    contentDescription = "Chi tiết",
                    modifier = Modifier.size(18.dp),
                    tint = Primary
                )
            }
        }
        
        // Fixed center section (STT + Họ + Tên) - no zoom
        GridCell(index.toString(), sttWidth) { focusManager.clearFocus() }
        
        EditableCellWithFocusLoss(
            value = lastName,
            onValueChange = { lastName = it },
            onFocusLost = { saveIfChanged() },
            width = fixedColumnWidth,
            placeholder = "Họ"
        )
        
        EditableCellWithFocusLoss(
            value = firstName,
            onValueChange = { firstName = it },
            onFocusLost = { saveIfChanged() },
            width = fixedColumnWidth,
            placeholder = "Tên"
        )
        
        // Scrollable right section (dates only)
        Row(
            modifier = if (enableScroll) {
                Modifier
                    .weight(1f)
                    .horizontalScroll(rightScrollState)
            } else {
                Modifier.weight(1f)
            }
        ) {
            // Date columns - show score if available for each date
            scheduledDates.forEach { date ->
                val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
                val dateString = date.format(dateFormatter)
                val score = dailyRecords[Pair(student.id, dateString)]
                val scoreText = score?.let { String.format("%.1f", it) } ?: ""
                GridCell(scoreText, columnWidth) { onStudentClick(dateString) }
            }
        }
    }
}

@Composable
fun NewStudentRow(
    index: Int,
    classId: Long,
    scheduledDates: List<LocalDate>,
    viewModel: ClassDetailViewModel,
    leftScrollState: ScrollState,
    rightScrollState: ScrollState,
    detailWidth: androidx.compose.ui.unit.Dp,
    sttWidth: androidx.compose.ui.unit.Dp,
    fixedColumnWidth: androidx.compose.ui.unit.Dp,
    columnWidth: androidx.compose.ui.unit.Dp,
    enableScroll: Boolean
) {
    var lastName by remember { mutableStateOf("") }
    var firstName by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current
    
    // Save when focus is lost
    fun saveIfFilled() {
        if (lastName.isNotBlank() && firstName.isNotBlank()) {
            val fullName = "$lastName $firstName".trim()
            viewModel.createStudent(classId, fullName)
            // Reset fields
            lastName = ""
            firstName = ""
        }
    }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(45.dp)
    ) {
        // Scrollable left section (Chi tiết only)
        Row(
            modifier = if (enableScroll) {
                Modifier.horizontalScroll(leftScrollState)
            } else {
                Modifier
            }
        ) {
            // Empty detail cell
            Box(
                modifier = Modifier
                    .width(detailWidth)
                    .height(45.dp)
                    .border(
                        width = 0.5.dp,
                        color = MaterialTheme.colorScheme.outline
                    )
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                // Empty
            }
        }
        
        // Fixed center section (STT + Họ + Tên) - no zoom
        GridCell(index.toString(), sttWidth) { focusManager.clearFocus() }
        
        EditableCellWithFocusLoss(
            value = lastName,
            onValueChange = { lastName = it },
            onFocusLost = { saveIfFilled() },
            width = fixedColumnWidth,
            placeholder = "Họ"
        )
        
        EditableCellWithFocusLoss(
            value = firstName,
            onValueChange = { firstName = it },
            onFocusLost = { saveIfFilled() },
            width = fixedColumnWidth,
            placeholder = "Tên"
        )
        
        // Scrollable right section (dates only)
        Row(
            modifier = if (enableScroll) {
                Modifier
                    .weight(1f)
                    .horizontalScroll(rightScrollState)
            } else {
                Modifier.weight(1f)
            }
        ) {
            // Empty date cells for all scheduled dates
            scheduledDates.forEach { _ ->
                Box(
                    modifier = Modifier
                        .width(columnWidth)
                        .height(45.dp)
                        .border(
                            width = 0.5.dp,
                            color = MaterialTheme.colorScheme.outline
                        )
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                )
            }
        }
    }
}

@Composable
fun EditableCell(
    value: String,
    onValueChange: (String) -> Unit,
    width: androidx.compose.ui.unit.Dp,
    placeholder: String
) {
    Box(
        modifier = Modifier
            .width(width)
            .height(45.dp)
            .border(0.5.dp, Color.Gray)
            .background(Color.White)
            .padding(horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            textStyle = androidx.compose.ui.text.TextStyle(
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                color = Color.Black
            ),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            decorationBox = { innerTextField ->
                if (value.isEmpty()) {
                    Text(
                        text = placeholder,
                        fontSize = 13.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                innerTextField()
            }
        )
    }
}

@Composable
fun EditableCellWithFocusLoss(
    value: String,
    onValueChange: (String) -> Unit,
    onFocusLost: () -> Unit,
    width: androidx.compose.ui.unit.Dp,
    placeholder: String
) {
    Box(
        modifier = Modifier
            .width(width)
            .height(45.dp)
            .border(
                width = 0.5.dp,
                color = MaterialTheme.colorScheme.outline
            )
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 8.dp)
            .clickable { 
                // Prevent click from propagating to parent
            },
        contentAlignment = Alignment.Center
    ) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            textStyle = androidx.compose.ui.text.TextStyle(
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface
            ),
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { focusState ->
                    if (!focusState.isFocused) {
                        onFocusLost()
                    }
                },
            decorationBox = { innerTextField ->
                if (value.isEmpty()) {
                    Text(
                        text = placeholder,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                innerTextField()
            }
        )
    }
}

@Composable
fun GridHeaderCell(text: String, width: androidx.compose.ui.unit.Dp, height: androidx.compose.ui.unit.Dp = 45.dp) {
    Box(
        modifier = Modifier
            .width(width)
            .height(height)
            .border(
                width = 0.5.dp,
                color = MaterialTheme.colorScheme.outline
            )
            .background(MaterialTheme.colorScheme.primaryContainer)
            .padding(horizontal = 2.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = (12.sp.value * (height.value / 45f)).sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

@Composable
fun GridCell(
    content: String,
    width: androidx.compose.ui.unit.Dp,
    height: androidx.compose.ui.unit.Dp = 45.dp,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .width(width)
            .height(height)
            .border(
                width = 0.5.dp,
                color = MaterialTheme.colorScheme.outline
            )
            .background(MaterialTheme.colorScheme.surface)
            .clickable { onClick() }
            .padding(horizontal = 2.dp),
        contentAlignment = Alignment.Center
    ) {
        if (content.isNotEmpty()) {
            Text(
                text = content,
                fontSize = (13.sp.value * (height.value / 45f)).sp,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
