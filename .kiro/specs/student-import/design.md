# Design Document - Student Import, Class Reminder & Calendar Features

## Overview

This document outlines the technical design for three interconnected features:
1. **Student Import**: Import student names from CSV/Excel files
2. **Class Reminder**: Notification system for upcoming classes
3. **Calendar with Badges**: Monthly calendar view showing class counts per day

## Architecture

### High-Level Components

```
┌─────────────────────────────────────────────────────────────┐
│                     Presentation Layer                       │
├──────────────────┬──────────────────┬──────────────────────┤
│  Import Screen   │  Calendar Screen │  Class Edit Screen   │
│  - File Picker   │  - Month View    │  - Reminder Toggle   │
│  - Preview List  │  - Badge Display │  - Lead Time Input   │
└────────┬─────────┴────────┬─────────┴──────────┬───────────┘
         │                  │                     │
┌────────▼──────────────────▼─────────────────────▼───────────┐
│                      ViewModel Layer                         │
├──────────────────┬──────────────────┬──────────────────────┤
│ ImportViewModel  │ CalendarViewModel│ ClassEditViewModel   │
└────────┬─────────┴────────┬─────────┴──────────┬───────────┘
         │                  │                     │
┌────────▼──────────────────▼─────────────────────▼───────────┐
│                      Service Layer                           │
├──────────────────┬──────────────────┬──────────────────────┤
│  FileParser      │  ReminderService │  ScheduleService     │
│  - CSV Parser    │  - AlarmManager  │  - Date Calculator   │
│  - Excel Parser  │  - Notification  │                      │
└────────┬─────────┴────────┬─────────┴──────────┬───────────┘
         │                  │                     │
┌────────▼──────────────────▼─────────────────────▼───────────┐
│                      Repository Layer                        │
├──────────────────┬──────────────────┬──────────────────────┤
│ StudentRepo      │  ClassRepo       │  ReminderRepo        │
└────────┬─────────┴────────┬─────────┴──────────┬───────────┘
         │                  │                     │
┌────────▼──────────────────▼─────────────────────▼───────────┐
│                      Database Layer                          │
│  - StudentEntity  - ClassEntity  - ReminderEntity           │
└─────────────────────────────────────────────────────────────┘
```

## Components and Interfaces

### 1. Student Import Feature

#### FileParser Service
```kotlin
interface FileParser {
    suspend fun parseFile(uri: Uri): ParseResult
    fun detectNameColumns(headers: List<String>): NameColumnInfo
}

data class ParseResult(
    val headers: List<String>,
    val rows: List<List<String>>,
    val detectedNameColumns: NameColumnInfo
)

data class NameColumnInfo(
    val firstNameColumn: Int?,
    val lastNameColumn: Int?,
    val fullNameColumn: Int?
)
```

#### CSVParser Implementation
- Uses `openInputStream()` to read CSV files
- Parses using comma/semicolon delimiters
- Handles quoted fields and escaped characters

#### ExcelParser Implementation  
- Uses Apache POI library for .xls/.xlsx files
- Reads first sheet by default
- Converts cells to strings

#### ImportViewModel
```kotlin
class ImportViewModel @Inject constructor(
    private val fileParser: FileParser,
    private val studentRepository: StudentRepository
) : ViewModel() {
    private val _parseResult = MutableStateFlow<ParseResult?>(null)
    val parseResult: StateFlow<ParseResult?> = _parseResult.asStateFlow()
    
    private val _importProgress = MutableStateFlow(ImportProgress(0, 0, false))
    val importProgress: StateFlow<ImportProgress> = _importProgress.asStateFlow()
    
    private val _importSummary = MutableStateFlow<ImportSummary?>(null)
    val importSummary: StateFlow<ImportSummary?> = _importSummary.asStateFlow()
    
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()
    
    fun selectFile(uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val result = fileParser.parseFile(uri)
                _parseResult.value = result
            } catch (e: Exception) {
                _errorMessage.value = "Failed to read file: ${e.message}"
            }
        }
    }
    
    fun confirmImport(classId: Long, nameColumnIndex: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val result = _parseResult.value ?: return@launch
            
            _importProgress.value = ImportProgress(0, result.rows.size, true)
            
            var added = 0
            var skipped = 0
            val errors = mutableListOf<String>()
            
            result.rows.forEachIndexed { index, row ->
                try {
                    val name = row.getOrNull(nameColumnIndex)?.trim()
                    if (!name.isNullOrEmpty()) {
                        val exists = studentRepository.studentExistsInClass(classId, name)
                        if (!exists) {
                            studentRepository.insertStudent(
                                StudentEntity(
                                    name = name,
                                    classId = classId
                                )
                            )
                            added++
                        } else {
                            skipped++
                        }
                    } else {
                        skipped++
                    }
                } catch (e: Exception) {
                    errors.add("Row ${index + 1}: ${e.message}")
                }
                
                _importProgress.value = ImportProgress(index + 1, result.rows.size, true)
            }
            
            _importProgress.value = ImportProgress(result.rows.size, result.rows.size, false)
            _importSummary.value = ImportSummary(added, skipped, errors)
        }
    }
    
    fun cancelImport() {
        _parseResult.value = null
        _importProgress.value = ImportProgress(0, 0, false)
        _importSummary.value = null
        _errorMessage.value = null
    }
}

data class ImportProgress(
    val current: Int,
    val total: Int,
    val isImporting: Boolean
)

data class ImportSummary(
    val added: Int,
    val skipped: Int,
    val errors: List<String>
)
```

#### ImportScreen Composable
```kotlin
@Composable
fun ImportScreen(
    classId: Long,
    navController: NavController,
    viewModel: ImportViewModel = hiltViewModel()
) {
    val parseResult by viewModel.parseResult.collectAsState()
    val importProgress by viewModel.importProgress.collectAsState()
    val importSummary by viewModel.importSummary.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.selectFile(it) }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Import Students") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            // File selection button (Requirement 1)
            if (parseResult == null && importSummary == null) {
                Button(
                    onClick = { 
                        filePickerLauncher.launch("*/*") // CSV, XLS, XLSX
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Upload, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Select File (CSV, Excel)")
                }
                
                Text(
                    text = "Supported formats: .csv, .xls, .xlsx",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            
            // Error message (Requirement 4)
            errorMessage?.let { error ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = error,
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
            
            // Preview and column selection (Requirements 2, 5)
            parseResult?.let { result ->
                NameColumnSelector(
                    parseResult = result,
                    onConfirm = { columnIndex ->
                        viewModel.confirmImport(classId, columnIndex)
                    },
                    onCancel = { viewModel.cancelImport() }
                )
            }
            
            // Import progress (Requirement 5)
            if (importProgress.isImporting) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Importing: ${importProgress.current} / ${importProgress.total}")
                }
            }
            
            // Import summary (Requirement 3)
            importSummary?.let { summary ->
                ImportSummaryCard(
                    summary = summary,
                    onDone = { navController.popBackStack() }
                )
            }
        }
    }
}

@Composable
fun NameColumnSelector(
    parseResult: ParseResult,
    onConfirm: (Int) -> Unit,
    onCancel: () -> Unit
) {
    var selectedColumn by remember { 
        mutableStateOf(
            parseResult.detectedNameColumns.fullNameColumn 
                ?: parseResult.detectedNameColumns.firstNameColumn 
                ?: 0
        )
    }
    
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Select Name Column",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Column selector dropdown
        parseResult.headers.forEachIndexed { index, header ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { selectedColumn = index }
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = selectedColumn == index,
                    onClick = { selectedColumn = index }
                )
                Text(text = header)
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Preview (Requirement 2.5)
        Text(
            text = "Preview (first 5 rows):",
            style = MaterialTheme.typography.titleSmall
        )
        
        parseResult.rows.take(5).forEach { row ->
            Text(
                text = row.getOrNull(selectedColumn) ?: "",
                modifier = Modifier.padding(vertical = 4.dp),
                style = MaterialTheme.typography.bodyMedium
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Action buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier.weight(1f)
            ) {
                Text("Cancel")
            }
            
            Button(
                onClick = { onConfirm(selectedColumn) },
                modifier = Modifier.weight(1f)
            ) {
                Text("Import")
            }
        }
    }
}

@Composable
fun ImportSummaryCard(
    summary: ImportSummary,
    onDone: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Import Complete",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text("Students added: ${summary.added}")
            Text("Students skipped: ${summary.skipped}")
            
            if (summary.errors.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Errors:",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error
                )
                summary.errors.forEach { error ->
                    Text(
                        text = "• $error",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = onDone,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Done")
            }
        }
    }
}
```

### 2. Class Reminder Feature

#### ReminderEntity
```kotlin
@Entity(tableName = "reminders")
data class ReminderEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val classId: Long,
    val scheduledTime: Long, // Timestamp when reminder should trigger
    val leadTimeMinutes: Int, // How many minutes before class
    val isDelivered: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
```

#### Update ClassEntity
```kotlin
@Entity(tableName = "classes")
data class ClassEntity(
    // ... existing fields ...
    val reminderEnabled: Boolean = false,
    val reminderLeadTimeMinutes: Int = 30 // Default 30 minutes
)
```

#### ReminderService
```kotlin
class ReminderService @Inject constructor(
    private val alarmManager: AlarmManager,
    private val context: Context,
    private val reminderRepository: ReminderRepository,
    private val scheduleService: ScheduleService
) {
    // Requirement 7: Schedule reminders for all upcoming class sessions
    suspend fun scheduleRemindersForClass(classEntity: ClassEntity) {
        if (!classEntity.reminderEnabled) return
        
        // Cancel existing reminders first
        cancelRemindersForClass(classEntity.id)
        
        // Calculate upcoming class sessions (next 30 days)
        val upcomingSessions = scheduleService.getUpcomingClassSessions(
            classEntity = classEntity,
            daysAhead = 30
        )
        
        upcomingSessions.forEach { sessionDateTime ->
            // Calculate reminder time based on lead time
            val reminderTime = sessionDateTime.minusMinutes(
                classEntity.reminderLeadTimeMinutes.toLong()
            )
            
            // Only schedule if reminder time is in the future
            if (reminderTime.isAfter(LocalDateTime.now())) {
                val reminderEntity = ReminderEntity(
                    classId = classEntity.id,
                    scheduledTime = reminderTime.toEpochSecond(ZoneOffset.UTC) * 1000,
                    leadTimeMinutes = classEntity.reminderLeadTimeMinutes
                )
                
                val reminderId = reminderRepository.insertReminder(reminderEntity)
                scheduleAlarm(reminderId, reminderTime, classEntity)
            }
        }
    }
    
    // Requirement 7.4: Cancel reminders for deleted classes
    suspend fun cancelRemindersForClass(classId: Long) {
        val reminders = reminderRepository.getRemindersForClass(classId)
        reminders.forEach { reminder ->
            cancelAlarm(reminder.id)
        }
        reminderRepository.deleteRemindersForClass(classId)
    }
    
    // Requirement 7.5: Reschedule when lead time changes
    suspend fun rescheduleReminders(classId: Long) {
        val classEntity = classRepository.getClassById(classId) ?: return
        scheduleRemindersForClass(classEntity)
    }
    
    private fun scheduleAlarm(
        reminderId: Long,
        reminderTime: LocalDateTime,
        classEntity: ClassEntity
    ) {
        val intent = Intent(context, ReminderBroadcastReceiver::class.java).apply {
            action = "com.studentmanagement.app.CLASS_REMINDER"
            putExtra("reminder_id", reminderId)
            putExtra("class_id", classEntity.id)
            putExtra("class_name", classEntity.name)
            putExtra("class_time", classEntity.startTime)
        }
        
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reminderId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val triggerTime = reminderTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        
        // Use exact alarm for precise timing
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
            } else {
                // Fallback to inexact alarm
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
            }
        } else {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            )
        }
    }
    
    private fun cancelAlarm(reminderId: Long) {
        val intent = Intent(context, ReminderBroadcastReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reminderId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }
    
    // Requirement 9: Check and request notification permission
    fun checkNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true // No permission needed for older versions
        }
    }
    
    fun requestNotificationPermission(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                REQUEST_NOTIFICATION_PERMISSION
            )
        }
    }
    
    companion object {
        const val REQUEST_NOTIFICATION_PERMISSION = 1001
    }
}
```

#### ReminderBroadcastReceiver
```kotlin
class ReminderBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != "com.studentmanagement.app.CLASS_REMINDER") return
        
        // Extract reminder data
        val reminderId = intent.getLongExtra("reminder_id", -1)
        val classId = intent.getLongExtra("class_id", -1)
        val className = intent.getStringExtra("class_name") ?: ""
        val classTime = intent.getStringExtra("class_time") ?: ""
        
        // Show notification with sound (Requirement 8)
        NotificationHelper.showClassReminder(
            context = context,
            classId = classId,
            className = className,
            classTime = classTime
        )
        
        // Mark reminder as delivered (Requirement 10.4)
        CoroutineScope(Dispatchers.IO).launch {
            val reminderRepository = (context.applicationContext as MyApplication)
                .appContainer.reminderRepository
            reminderRepository.markReminderAsDelivered(reminderId)
        }
    }
}
```

#### NotificationHelper
```kotlin
object NotificationHelper {
    private const val CHANNEL_ID = "class_reminders"
    private const val CHANNEL_NAME = "Class Reminders"
    
    // Requirement 8: Display notification with sound
    fun showClassReminder(
        context: Context,
        classId: Long,
        className: String,
        classTime: String
    ) {
        createNotificationChannel(context)
        
        // Create intent to open class detail when notification is tapped (Requirement 8.4)
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("navigate_to", "class_detail")
            putExtra("class_id", classId)
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            classId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // Build notification (Requirements 8.1, 8.2, 8.3)
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Class Reminder: $className")
            .setContentText("Class starts at $classTime")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
            .setVibrate(longArrayOf(0, 500, 200, 500))
            .build()
        
        val notificationManager = NotificationManagerCompat.from(context)
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            notificationManager.notify(classId.toInt(), notification)
        }
    }
    
    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for upcoming class sessions"
                enableSound(true)
                enableVibration(true)
                setSound(
                    RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION),
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
            }
            
            val notificationManager = context.getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }
}
```

#### ClassEditScreen Enhancement (Requirement 6)
```kotlin
@Composable
fun ReminderSettingsSection(
    reminderEnabled: Boolean,
    reminderLeadTimeMinutes: Int,
    onReminderEnabledChange: (Boolean) -> Unit,
    onLeadTimeChange: (Int) -> Unit,
    onRequestPermission: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Class Reminders",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Enable/disable toggle (Requirement 6.1, 6.4)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Enable reminders")
            Switch(
                checked = reminderEnabled,
                onCheckedChange = { enabled ->
                    if (enabled) {
                        onRequestPermission()
                    }
                    onReminderEnabledChange(enabled)
                }
            )
        }
        
        // Lead time selection (Requirement 6.2, 6.5)
        if (reminderEnabled) {
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Remind me before class:",
                style = MaterialTheme.typography.bodyMedium
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Quick options
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(15, 30, 60).forEach { minutes ->
                    FilterChip(
                        selected = reminderLeadTimeMinutes == minutes,
                        onClick = { onLeadTimeChange(minutes) },
                        label = { Text("$minutes min") }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Custom input
            OutlinedTextField(
                value = reminderLeadTimeMinutes.toString(),
                onValueChange = { value ->
                    value.toIntOrNull()?.let { onLeadTimeChange(it) }
                },
                label = { Text("Custom (minutes)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
```

### 3. Calendar with Badges Feature

#### CalendarViewModel
```kotlin
class CalendarViewModel @Inject constructor(
    private val classRepository: ClassRepository,
    private val scheduleService: ScheduleService
) : ViewModel() {
    private val _currentMonth = MutableStateFlow(YearMonth.now())
    val currentMonth: StateFlow<YearMonth> = _currentMonth.asStateFlow()
    
    private val _classCountByDate = MutableStateFlow<Map<LocalDate, Int>>(emptyMap())
    val classCountByDate: StateFlow<Map<LocalDate, Int>> = _classCountByDate.asStateFlow()
    
    private val _selectedDate = MutableStateFlow<LocalDate?>(null)
    val selectedDate: StateFlow<LocalDate?> = _selectedDate.asStateFlow()
    
    private val _classesForSelectedDate = MutableStateFlow<List<ClassEntity>>(emptyList())
    val classesForSelectedDate: StateFlow<List<ClassEntity>> = _classesForSelectedDate.asStateFlow()
    
    private var allClasses: List<ClassEntity> = emptyList()
    
    init {
        loadClasses()
    }
    
    private fun loadClasses() {
        viewModelScope.launch {
            classRepository.getAllClasses().collect { classes ->
                allClasses = classes
                loadClassCounts(_currentMonth.value)
                
                // Update selected date classes if a date is selected
                _selectedDate.value?.let { date ->
                    updateClassesForDate(date)
                }
            }
        }
    }
    
    // Requirement 12.2: Navigate to next month
    fun navigateToNextMonth() {
        _currentMonth.value = _currentMonth.value.plusMonths(1)
        loadClassCounts(_currentMonth.value)
    }
    
    // Requirement 12.1: Navigate to previous month
    fun navigateToPreviousMonth() {
        _currentMonth.value = _currentMonth.value.minusMonths(1)
        loadClassCounts(_currentMonth.value)
    }
    
    // Requirement 13.1: Select date and show classes
    fun selectDate(date: LocalDate) {
        _selectedDate.value = date
        updateClassesForDate(date)
    }
    
    // Requirement 11, 12.5: Load class counts for month
    fun loadClassCounts(month: YearMonth) {
        viewModelScope.launch(Dispatchers.Default) {
            val counts = scheduleService.getClassCountsForMonth(month, allClasses)
            _classCountByDate.value = counts
        }
    }
    
    // Requirement 13: Get classes for selected date
    private fun updateClassesForDate(date: LocalDate) {
        viewModelScope.launch(Dispatchers.Default) {
            val classes = scheduleService.getClassesForDate(date, allClasses)
            _classesForSelectedDate.value = classes
        }
    }
}
```

#### ScheduleService Enhancement
```kotlin
class ScheduleService {
    // ... existing methods ...
    
    // Requirement 11.4: Calculate class count based on schedule configuration
    fun getClassCountForDate(date: LocalDate, classes: List<ClassEntity>): Int {
        return classes.count { classEntity ->
            isClassScheduledOnDate(classEntity, date)
        }
    }
    
    // Requirement 11: Get class counts for entire month
    fun getClassCountsForMonth(
        month: YearMonth,
        classes: List<ClassEntity>
    ): Map<LocalDate, Int> {
        val counts = mutableMapOf<LocalDate, Int>()
        val startDate = month.atDay(1)
        val endDate = month.atEndOfMonth()
        
        var currentDate = startDate
        while (!currentDate.isAfter(endDate)) {
            val count = getClassCountForDate(currentDate, classes)
            if (count > 0) {
                counts[currentDate] = count
            }
            currentDate = currentDate.plusDays(1)
        }
        
        return counts
    }
    
    // Requirement 11.4: Check if class is scheduled on specific date
    private fun isClassScheduledOnDate(
        classEntity: ClassEntity,
        date: LocalDate
    ): Boolean {
        // Check if date is within class period
        val startDate = LocalDate.parse(classEntity.startDate)
        val endDate = classEntity.endDate?.let { LocalDate.parse(it) }
        
        if (date.isBefore(startDate)) return false
        if (endDate != null && date.isAfter(endDate)) return false
        
        // Check if day of week matches
        val dayOfWeek = date.dayOfWeek.value // 1=Monday, 7=Sunday
        if (!classEntity.scheduleDaysOfWeek.contains(dayOfWeek)) return false
        
        // Check repeat interval
        if (classEntity.repeatInterval > 0) {
            val daysSinceStart = ChronoUnit.DAYS.between(startDate, date)
            
            val intervalDays = when (classEntity.repeatUnit) {
                "day" -> classEntity.repeatInterval
                "week" -> classEntity.repeatInterval * 7
                "month" -> classEntity.repeatInterval * 30 // Approximate
                else -> 1
            }
            
            if (daysSinceStart % intervalDays != 0L) return false
        }
        
        return true
    }
    
    // Get classes scheduled for specific date (Requirement 13)
    fun getClassesForDate(
        date: LocalDate,
        classes: List<ClassEntity>
    ): List<ClassEntity> {
        return classes.filter { classEntity ->
            isClassScheduledOnDate(classEntity, date)
        }.sortedBy { it.startTime }
    }
    
    // Requirement 7: Get upcoming class sessions for reminder scheduling
    fun getUpcomingClassSessions(
        classEntity: ClassEntity,
        daysAhead: Int = 30
    ): List<LocalDateTime> {
        val sessions = mutableListOf<LocalDateTime>()
        val today = LocalDate.now()
        val endDate = today.plusDays(daysAhead.toLong())
        
        var currentDate = today
        while (!currentDate.isAfter(endDate)) {
            if (isClassScheduledOnDate(classEntity, currentDate)) {
                // Parse start time and create LocalDateTime
                val timeParts = classEntity.startTime.split(":")
                if (timeParts.size == 2) {
                    val hour = timeParts[0].toIntOrNull() ?: 0
                    val minute = timeParts[1].toIntOrNull() ?: 0
                    
                    val sessionDateTime = LocalDateTime.of(
                        currentDate.year,
                        currentDate.month,
                        currentDate.dayOfMonth,
                        hour,
                        minute
                    )
                    
                    // Only add if in the future
                    if (sessionDateTime.isAfter(LocalDateTime.now())) {
                        sessions.add(sessionDateTime)
                    }
                }
            }
            currentDate = currentDate.plusDays(1)
        }
        
        return sessions
    }
}
```

#### CalendarScreen Composable
```kotlin
@Composable
fun CalendarScreen(
    viewModel: CalendarViewModel = hiltViewModel()
) {
    val currentMonth by viewModel.currentMonth.collectAsState()
    val classCountByDate by viewModel.classCountByDate.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    val classesForSelectedDate by viewModel.classesForSelectedDate.collectAsState()
    
    Column(modifier = Modifier.fillMaxSize()) {
        MonthHeader(
            month = currentMonth,
            onPreviousMonth = { viewModel.navigateToPreviousMonth() },
            onNextMonth = { viewModel.navigateToNextMonth() }
        )
        
        CalendarGrid(
            month = currentMonth,
            classCountByDate = classCountByDate,
            selectedDate = selectedDate,
            onDateClick = { date -> viewModel.selectDate(date) }
        )
        
        selectedDate?.let { date ->
            if (classesForSelectedDate.isEmpty()) {
                EmptyClassMessage(date = date)
            } else {
                ClassListForDate(
                    date = date,
                    classes = classesForSelectedDate,
                    onClassClick = { classId -> 
                        // Navigate to class detail screen
                    }
                )
            }
        }
    }
}

@Composable
fun MonthHeader(
    month: YearMonth,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onPreviousMonth) {
            Icon(Icons.Default.ChevronLeft, contentDescription = "Previous month")
        }
        
        Text(
            text = "${month.month.getDisplayName(TextStyle.FULL, Locale.getDefault())} ${month.year}",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        
        IconButton(onClick = onNextMonth) {
            Icon(Icons.Default.ChevronRight, contentDescription = "Next month")
        }
    }
}

@Composable
fun CalendarGrid(
    month: YearMonth,
    classCountByDate: Map<LocalDate, Int>,
    selectedDate: LocalDate?,
    onDateClick: (LocalDate) -> Unit
) {
    val today = LocalDate.now()
    val firstDayOfMonth = month.atDay(1)
    val lastDayOfMonth = month.atEndOfMonth()
    val firstDayOfWeek = firstDayOfMonth.dayOfWeek.value % 7 // 0 = Sunday
    
    Column(modifier = Modifier.fillMaxWidth()) {
        // Day of week headers
        Row(modifier = Modifier.fillMaxWidth()) {
            listOf("CN", "T2", "T3", "T4", "T5", "T6", "T7").forEach { day ->
                Text(
                    text = day,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        
        // Calendar grid
        var currentDate = firstDayOfMonth.minusDays(firstDayOfWeek.toLong())
        repeat(6) { // Max 6 weeks
            Row(modifier = Modifier.fillMaxWidth()) {
                repeat(7) { // 7 days per week
                    val date = currentDate
                    val isInCurrentMonth = date.month == month.month
                    val classCount = classCountByDate[date] ?: 0
                    
                    CalendarDayCell(
                        date = date,
                        classCount = classCount,
                        isSelected = date == selectedDate,
                        isToday = date == today,
                        isInCurrentMonth = isInCurrentMonth,
                        onClick = { if (isInCurrentMonth) onDateClick(date) }
                    )
                    
                    currentDate = currentDate.plusDays(1)
                }
            }
        }
    }
}

@Composable
fun CalendarDayCell(
    date: LocalDate,
    classCount: Int,
    isSelected: Boolean,
    isToday: Boolean,
    isInCurrentMonth: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clickable(enabled = isInCurrentMonth, onClick = onClick)
            .background(
                when {
                    isSelected -> MaterialTheme.colorScheme.primaryContainer
                    isToday -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                    else -> Color.Transparent
                }
            )
            .padding(4.dp)
    ) {
        Text(
            text = date.dayOfMonth.toString(),
            modifier = Modifier.align(Alignment.Center),
            style = MaterialTheme.typography.bodyMedium,
            color = when {
                !isInCurrentMonth -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                isSelected -> MaterialTheme.colorScheme.onPrimaryContainer
                isToday -> MaterialTheme.colorScheme.primary
                else -> MaterialTheme.colorScheme.onSurface
            }
        )
        
        // Badge for class count (Requirements 11, 14)
        if (classCount > 0 && isInCurrentMonth) {
            Badge(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(2.dp),
                containerColor = MaterialTheme.colorScheme.error,
                contentColor = MaterialTheme.colorScheme.onError
            ) {
                Text(
                    text = classCount.toString(),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                )
            }
        }
    }
}

@Composable
fun ClassListForDate(
    date: LocalDate,
    classes: List<ClassEntity>,
    onClassClick: (Long) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "Classes on ${date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))}",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        classes.forEach { classEntity ->
            ClassItemCard(
                classEntity = classEntity,
                onClick = { onClassClick(classEntity.id) }
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
fun ClassItemCard(
    classEntity: ClassEntity,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = classEntity.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${classEntity.startTime} - ${classEntity.endTime}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Student count badge
            Badge {
                Text(text = "${classEntity.studentCount} students")
            }
        }
    }
}

@Composable
fun EmptyClassMessage(date: LocalDate) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "No classes scheduled for ${date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}
```

## Data Models

### Database Schema Updates

#### New Table: reminders
```sql
CREATE TABLE reminders (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    classId INTEGER NOT NULL,
    scheduledTime INTEGER NOT NULL,
    leadTimeMinutes INTEGER NOT NULL,
    isDelivered INTEGER NOT NULL DEFAULT 0,
    createdAt INTEGER NOT NULL,
    FOREIGN KEY (classId) REFERENCES classes(id) ON DELETE CASCADE
)
```

#### Update Table: classes
```sql
ALTER TABLE classes ADD COLUMN reminderEnabled INTEGER NOT NULL DEFAULT 0;
ALTER TABLE classes ADD COLUMN reminderLeadTimeMinutes INTEGER NOT NULL DEFAULT 30;
```

## Error Handling

### File Import Errors
- **File not found**: Display error message, allow user to select again
- **Invalid format**: Show format requirements, suggest correct format
- **No name columns detected**: Allow manual column selection
- **Parse error**: Log error, show user-friendly message with line number

### Reminder Errors
- **Permission denied**: Show explanation dialog with settings link
- **AlarmManager unavailable**: Fallback to WorkManager for scheduling
- **Notification failed**: Log error, retry on next app launch

### Calendar Errors
- **Failed to load classes**: Show error message, provide retry button
- **Date calculation error**: Use fallback to simple date matching

## Testing Strategy

### Unit Tests
- FileParser: Test CSV and Excel parsing with various formats
- NameDetector: Test column detection with different header variations
- ScheduleService: Test date calculations and class count logic
- ReminderService: Test reminder scheduling and cancellation

### Integration Tests
- Import flow: Test end-to-end import from file selection to student creation
- Reminder flow: Test reminder scheduling and notification delivery
- Calendar sync: Test that calendar badges update when class schedule changes

### UI Tests
- Import screen: Test file picker, preview, and import confirmation
- Calendar screen: Test month navigation, date selection, badge display
- Reminder settings: Test toggle, lead time input, permission request

## Dependencies

### New Libraries
```gradle
// Apache POI for Excel parsing
implementation 'org.apache.poi:poi:5.2.3'
implementation 'org.apache.poi:poi-ooxml:5.2.3'

// CSV parsing (if needed, or use built-in)
// implementation 'com.opencsv:opencsv:5.7.1'
```

### Android Permissions
```xml
<!-- For file access -->
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />

<!-- For notifications -->
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />

<!-- For exact alarms (Android 12+) -->
<uses-permission android:name="android.permission.SCHEDULE_EXACT_ALARM" />
<uses-permission android:name="android.permission.USE_EXACT_ALARM" />
```

### Broadcast Receiver Registration
```xml
<receiver
    android:name=".service.ReminderBroadcastReceiver"
    android:enabled="true"
    android:exported="false">
    <intent-filter>
        <action android:name="com.studentmanagement.app.CLASS_REMINDER" />
    </intent-filter>
</receiver>
```

## Performance Considerations

### File Import
- Parse files on background thread (IO dispatcher)
- Show progress for large files (>100 rows)
- Batch insert students (use transaction)

### Reminder Scheduling
- Schedule reminders in batches
- Limit to next 30 days of reminders (reschedule monthly)
- Use WorkManager as fallback for reliability

### Calendar Rendering
- Cache class counts for current month
- Lazy load adjacent months
- Debounce month navigation to prevent rapid API calls

## Security Considerations

- Validate file size before parsing (max 10MB)
- Sanitize student names (trim, remove special characters)
- Validate reminder times (must be in future)
- Check notification permission before scheduling
