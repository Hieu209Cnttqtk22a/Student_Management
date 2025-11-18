# Design Document

## Overview

This feature transforms the ClassDetailScreen attendance grid from a single-date view to a multi-date view. The grid will dynamically generate columns for all scheduled class dates based on the class configuration (scheduleDaysOfWeek, createdAt, repeatInterval, repeatUnit). The implementation focuses on efficient date calculation, data loading, and UI rendering with horizontal scrolling support.

## Architecture

### Component Structure

```
ClassDetailScreen (UI)
    ↓
ClassDetailViewModel (Business Logic)
    ↓
ClassRepository + StudentDailyRepository (Data Layer)
```

### Key Changes

1. **ClassDetailViewModel**: Add date calculation logic and multi-date data loading
2. **ClassDetailScreen**: Modify grid to render multiple date columns with horizontal scrolling
3. **Data Loading**: Batch load daily records for all scheduled dates

## Components and Interfaces

### 1. Date Calculation Service

**Location**: `ClassDetailViewModel` (as private functions)

**Functions**:

```kotlin
private fun calculateScheduledDates(
    classEntity: ClassEntity
): List<LocalDate> {
    // Parse scheduled days from JSON
    // Calculate start and end dates
    // Generate list of dates matching scheduled weekdays
    // Return sorted list
}

private fun parseScheduledDays(scheduleDaysJson: String): List<Int>

private fun getEndDate(
    startDate: LocalDate,
    repeatInterval: Int,
    repeatUnit: String
): LocalDate
```

**Algorithm**:
1. Parse `scheduleDaysOfWeek` JSON to get list of weekday numbers (1-7)
2. Convert `createdAt` timestamp to `LocalDate` as start date
3. Calculate end date: `startDate + repeatInterval * repeatUnit`
4. Iterate through each date from start to end
5. For each date, check if its day of week matches any scheduled day
6. Add matching dates to result list
7. Return sorted list of dates

**Day of Week Mapping**:
- UI format: 1=Sunday, 2=Monday, 3=Tuesday, 4=Wednesday, 5=Thursday, 6=Friday, 7=Saturday
- LocalDate format: 1=Monday, 2=Tuesday, ..., 7=Sunday
- Conversion needed: UI day → LocalDate day

### 2. Multi-Date Data Loading

**Location**: `ClassDetailViewModel`

**New State**:
```kotlin
data class ClassDetailUiState(
    // ... existing fields
    val scheduledDates: List<LocalDate> = emptyList()
)

// Change from Map<Long, Float?> to Map<Pair<Long, String>, Float?>
// Key: Pair(studentId, dateString)
val dailyRecords: StateFlow<Map<Pair<Long, String>, Float?>>
```

**Functions**:
```kotlin
fun loadClassDetail(classId: Long) {
    // Load class entity
    // Calculate scheduled dates
    // Load students
    // Load daily records for all dates
    // Update UI state
}

private suspend fun loadDailyRecordsForDates(
    classId: Long,
    dates: List<LocalDate>
): Map<Pair<Long, String>, Float?> {
    // For each date, load daily records
    // Build map with (studentId, dateString) as key
}
```

### 3. Grid UI Modifications

**Location**: `ClassDetailScreen.kt`

**Changes to StudentGridView**:

```kotlin
@Composable
fun StudentGridView(
    classId: Long,
    students: List<StudentEntity>,
    scheduledDates: List<LocalDate>, // NEW
    dailyRecords: Map<Pair<Long, String>, Float?>, // MODIFIED
    viewModel: ClassDetailViewModel,
    onStudentClick: (Long, String) -> Unit, // MODIFIED - add date parameter
    onStudentDetailClick: (Long) -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
)
```

**Grid Structure**:
- Fixed columns: Chi tiết (70dp) | STT (50dp) | Họ (120dp) | Tên (100dp)
- Dynamic columns: Date1 (100dp) | Date2 (100dp) | ... | DateN (100dp)
- Total width: 340dp + (100dp × number of dates)

**Scrolling Strategy**:
- Use `horizontalScroll()` modifier with shared `ScrollState`
- Apply to both header row and all data rows
- Fixed columns remain visible, date columns scroll

**Header Row**:
```kotlin
Row(modifier = Modifier.horizontalScroll(scrollState)) {
    GridHeaderCell("Chi tiết", 70.dp)
    GridHeaderCell("STT", 50.dp)
    GridHeaderCell("Họ", 120.dp)
    GridHeaderCell("Tên", 100.dp)
    scheduledDates.forEach { date ->
        GridHeaderCell(date.format("dd/MM/yyyy"), 100.dp)
    }
}
```

**Data Row**:
```kotlin
Row(modifier = Modifier.horizontalScroll(scrollState)) {
    // ... fixed columns (detail, STT, lastName, firstName)
    scheduledDates.forEach { date ->
        val dateString = date.format("dd/MM/yyyy")
        val score = dailyRecords[Pair(student.id, dateString)]
        GridCell(
            content = score?.let { "%.1f".format(it) } ?: "",
            width = 100.dp,
            onClick = { onStudentClick(student.id, dateString) }
        )
    }
}
```

### 4. Navigation Updates

**Modification**: Update click handlers to pass selected date

```kotlin
onClick = { studentId, dateString ->
    navController.navigate("student/$studentId/daily/edit?classId=$classId&date=$dateString")
}
```

## Data Models

### Existing Models (No Changes)

- `ClassEntity`: Already contains all needed fields
- `StudentEntity`: No changes needed
- `StudentDailyEntity`: No changes needed

### ViewModel State Changes

```kotlin
// Before
val dailyRecords: StateFlow<Map<Long, Float?>>

// After
val dailyRecords: StateFlow<Map<Pair<Long, String>, Float?>>
```

## Error Handling

### Invalid Schedule Configuration

**Scenario**: scheduleDaysOfWeek is empty or invalid JSON

**Handling**: 
- Return empty list from `calculateScheduledDates()`
- Display message: "Lớp học chưa có lịch học được cấu hình"

### Date Calculation Errors

**Scenario**: Invalid repeatUnit or repeatInterval

**Handling**:
- Default to 1 week if invalid
- Log warning

### Data Loading Errors

**Scenario**: Failed to load daily records for some dates

**Handling**:
- Show empty cells for failed dates
- Continue displaying other data
- Log error

## Testing Strategy

### Unit Tests

1. **Date Calculation Tests**:
   - Test with single weekday
   - Test with multiple weekdays
   - Test with different repeatUnits (WEEK, MONTH, YEAR)
   - Test with different repeatIntervals
   - Test edge cases (start date, end date)

2. **Data Loading Tests**:
   - Test loading records for multiple dates
   - Test with missing records
   - Test with partial data

### Integration Tests

1. **Grid Rendering**:
   - Test with 1 scheduled date
   - Test with 10+ scheduled dates
   - Test horizontal scrolling
   - Test with no scheduled dates

2. **User Interactions**:
   - Test clicking on date cells
   - Test navigation to edit screen
   - Test refresh after editing

### Manual Testing

1. Create class with single weekday (e.g., Monday only)
2. Create class with multiple weekdays (e.g., Monday, Wednesday, Friday)
3. Create class with different durations (1 week, 4 weeks, 3 months)
4. Verify all scheduled dates appear in grid
5. Verify horizontal scrolling works
6. Add attendance records and verify they appear in correct cells
7. Edit attendance and verify grid updates

## Performance Considerations

### Date Calculation

- Calculate dates once when loading class detail
- Cache in ViewModel state
- Typical class: ~12-50 dates (e.g., 3 times/week for 4 weeks = 12 dates)

### Data Loading

- Batch load all daily records in single query or parallel queries
- Use Flow to reactively update UI
- Estimated data: 30 students × 12 dates = 360 records (lightweight)

### UI Rendering

- LazyColumn for vertical scrolling (already implemented)
- Horizontal scroll for date columns
- Each row renders dynamically based on scheduledDates list
- Estimated columns: 4 fixed + 12 date = 16 columns (manageable)

## Migration Notes

### Backward Compatibility

- Existing classes without proper schedule configuration will show empty grid
- Existing daily records will continue to work
- No database migration needed

### Data Integrity

- No changes to database schema
- All existing data remains valid
- New feature only changes how data is displayed

## Future Enhancements

1. **Date Range Selector**: Allow teachers to view specific date ranges
2. **Week/Month View Toggle**: Switch between showing all dates vs. current week/month
3. **Export to Excel**: Export attendance grid with all dates
4. **Attendance Statistics**: Show attendance percentage per student across all dates
5. **Bulk Edit**: Edit attendance for multiple students/dates at once
