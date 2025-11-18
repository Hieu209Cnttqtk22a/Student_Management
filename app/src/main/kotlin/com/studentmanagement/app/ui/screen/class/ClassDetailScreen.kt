package com.studentmanagement.app.ui.screen.`class`

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.studentmanagement.app.data.entity.StudentEntity
import com.studentmanagement.app.ui.theme.Primary
import com.studentmanagement.app.ui.theme.PrimaryLight
import com.studentmanagement.app.ui.viewmodel.ClassDetailViewModel
import com.studentmanagement.app.ui.viewmodel.ClassDetailUiState
import java.text.SimpleDateFormat
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
    val selectedDate = remember { mutableStateOf(SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())) }
    val focusManager = LocalFocusManager.current

    LaunchedEffect(classId, selectedDate.value) {
        viewModel.loadClassDetail(classId, selectedDate.value)
    }

    val className = when (val state = uiState) {
        is ClassDetailUiState.Success -> state.classEntity.name
        else -> "..."
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
                    IconButton(onClick = { /* TODO: Filter */ }) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = "Lọc",
                            tint = Color.White
                        )
                    }
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
                        students = state.students,
                        selectedDate = selectedDate.value,
                        dailyRecords = dailyRecords,
                        viewModel = viewModel,
                        onStudentClick = { studentId ->
                            navController.navigate("student/$studentId/daily/edit?classId=$classId&date=${selectedDate.value}")
                        },
                        onStudentDetailClick = { studentId ->
                            navController.navigate("student/$studentId/history")
                        },
                        onRefresh = {
                            viewModel.refreshDailyRecords(classId, selectedDate.value)
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
    selectedDate: String,
    dailyRecords: Map<Long, Float?>,
    viewModel: ClassDetailViewModel,
    onStudentClick: (Long) -> Unit,
    onStudentDetailClick: (Long) -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Sort students by name
    val sortedStudents = remember(students) {
        students.sortedBy { it.name }
    }
    
    // Calculate total width of all columns
    val totalWidth = 70.dp + 50.dp + 120.dp + 100.dp + 100.dp // Chi tiết + STT + Họ + Tên + Date
    
    // Shared scroll state for synchronized horizontal scrolling
    val horizontalScrollState = rememberScrollState()
    
    // Get screen width
    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    
    // Only enable scroll if content is wider than screen
    val enableScroll = totalWidth > screenWidth
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(Primary, PrimaryLight)
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Buổi học ngày",
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.8f)
                )
                Text(
                    selectedDate,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }

        // Grid table with synchronized horizontal scroll
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Header row - scrollable horizontally only if needed
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(
                        if (enableScroll) {
                            Modifier.horizontalScroll(horizontalScrollState)
                        } else {
                            Modifier
                        }
                    )
            ) {
                GridHeaderCell("Chi tiết", width = 70.dp)
                GridHeaderCell("STT", width = 50.dp)
                GridHeaderCell("Họ", width = 120.dp)
                GridHeaderCell("Tên", width = 100.dp)
                GridHeaderCell(selectedDate, width = 100.dp)
            }
            
            // Data rows - scrollable vertically and horizontally (synchronized)
            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                // Existing students
                itemsIndexed(sortedStudents) { index, student ->
                    EditableStudentRow(
                        index = index + 1,
                        student = student,
                        score = dailyRecords[student.id],
                        viewModel = viewModel,
                        horizontalScrollState = horizontalScrollState,
                        enableScroll = enableScroll,
                        onStudentClick = { 
                            onStudentClick(student.id)
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
                        viewModel = viewModel,
                        horizontalScrollState = horizontalScrollState,
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
    score: Float?,
    viewModel: ClassDetailViewModel,
    horizontalScrollState: ScrollState,
    enableScroll: Boolean,
    onStudentClick: () -> Unit,
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
            .then(
                if (enableScroll) {
                    Modifier.horizontalScroll(horizontalScrollState)
                } else {
                    Modifier
                }
            )
    ) {
        // Detail icon
        Box(
            modifier = Modifier
                .width(70.dp)
                .height(45.dp)
                .border(
                    width = 0.5.dp,
                    color = Color.Gray
                )
                .clickable { onStudentDetailClick(student.id) }
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Visibility,
                contentDescription = "Chi tiết",
                modifier = Modifier.size(18.dp),
                tint = Primary
            )
        }
        
        // STT
        GridCell(index.toString(), 50.dp) { focusManager.clearFocus() }
        
        // Last name - editable
        EditableCellWithFocusLoss(
            value = lastName,
            onValueChange = { lastName = it },
            onFocusLost = { saveIfChanged() },
            width = 120.dp,
            placeholder = "Họ"
        )
        
        // First name - editable
        EditableCellWithFocusLoss(
            value = firstName,
            onValueChange = { firstName = it },
            onFocusLost = { saveIfChanged() },
            width = 100.dp,
            placeholder = "Tên"
        )
        
        // Date column - show score if available
        val scoreText = score?.let { String.format("%.1f", it) } ?: ""
        GridCell(scoreText, 100.dp) { onStudentClick() }
    }
}

@Composable
fun NewStudentRow(
    index: Int,
    classId: Long,
    viewModel: ClassDetailViewModel,
    horizontalScrollState: ScrollState,
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
            .then(
                if (enableScroll) {
                    Modifier.horizontalScroll(horizontalScrollState)
                } else {
                    Modifier
                }
            )
    ) {
        // Empty detail cell
        Box(
            modifier = Modifier
                .width(70.dp)
                .height(45.dp)
                .border(
                    width = 0.5.dp,
                    color = Color.Gray
                )
                .background(Color(0xFFF5F5F5)),
            contentAlignment = Alignment.Center
        ) {
            // Empty
        }
        
        // STT
        GridCell(index.toString(), 50.dp) { focusManager.clearFocus() }
        
        // Last name input
        EditableCellWithFocusLoss(
            value = lastName,
            onValueChange = { lastName = it },
            onFocusLost = { saveIfFilled() },
            width = 120.dp,
            placeholder = "Họ"
        )
        
        // First name input
        EditableCellWithFocusLoss(
            value = firstName,
            onValueChange = { firstName = it },
            onFocusLost = { saveIfFilled() },
            width = 100.dp,
            placeholder = "Tên"
        )
        
        // Empty date cell
        Box(
            modifier = Modifier
                .width(100.dp)
                .height(45.dp)
                .border(
                    width = 0.5.dp,
                    color = Color.Gray
                )
                .background(Color(0xFFF5F5F5))
        )
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
                color = Color.Gray
            )
            .background(Color.White)
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
                color = Color.Black
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
fun GridHeaderCell(text: String, width: androidx.compose.ui.unit.Dp) {
    Box(
        modifier = Modifier
            .width(width)
            .height(45.dp)
            .border(
                width = 0.5.dp,
                color = Color.Gray
            )
            .background(Primary.copy(alpha = 0.15f)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            maxLines = 2
        )
    }
}

@Composable
fun GridCell(
    content: String,
    width: androidx.compose.ui.unit.Dp,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .width(width)
            .height(45.dp)
            .border(
                width = 0.5.dp,
                color = Color.Gray
            )
            .background(Color.White)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (content.isNotEmpty()) {
            Text(
                text = content,
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
        }
    }
}
