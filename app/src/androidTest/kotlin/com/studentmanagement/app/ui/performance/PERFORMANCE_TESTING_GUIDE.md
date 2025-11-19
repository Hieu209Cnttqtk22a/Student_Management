# Performance Testing Guide for High Refresh Rate Displays

This guide explains how to verify the performance optimizations for the student filter feature on high refresh rate displays (120Hz/144Hz).

## Automated Performance Tests

The `FilterPerformanceTest.kt` file contains automated tests that measure:

1. **Frame rate performance during filtering** (Task 11.1)
   - `measureFilterApplicationPerformance_withLargeStudentList()` - Tests with 50 students
   - `measureFilterApplicationPerformance_with100Students()` - Tests with 100 students
   - `verifyNoFrameDropsDuringFiltering()` - Tests multiple filter operations

2. **Recomposition efficiency** (Task 11.2)
   - `verifyDerivedStateOfPreventsUnnecessaryRecompositions()` - Tests derivedStateOf optimization
   - `verifyLazyColumnKeyStability()` - Tests stable keys in LazyColumn
   - `verifyMinimalRecompositionsDuringFilterChanges()` - Tests minimal recompositions

## Running the Tests

### Run all performance tests:
```bash
./gradlew :app:connectedAndroidTest
```

### View test results:
Check the console output for performance metrics including:
- Filter application time (target: < 8.3ms for 120Hz, < 6.9ms for 144Hz)
- Recomposition counts
- Key stability metrics

## Manual Testing with Compose Layout Inspector

### Setup
1. Connect a device with 120Hz or 144Hz display
2. Open Android Studio
3. Run the app in debug mode
4. Open **Tools > Layout Inspector**

### Test Procedure

#### 1. Verify Minimal Recompositions (Requirement 8.3, 8.9)

1. In Layout Inspector, enable **Show Recomposition Counts**
2. Navigate to ClassDetailScreen
3. Apply a filter (e.g., "Điểm kém (< 7)")
4. Observe recomposition counts:
   - ✅ **PASS**: Only FilterComboBox and filtered list recompose
   - ❌ **FAIL**: Entire screen or unrelated components recompose

#### 2. Test LazyColumn Key Stability (Requirement 8.6)

1. In Layout Inspector, select the LazyColumn component
2. Apply different filters
3. Verify:
   - ✅ **PASS**: Items that remain visible don't recompose
   - ✅ **PASS**: Only new items entering the list compose
   - ❌ **FAIL**: All items recompose on filter change

#### 3. Verify derivedStateOf Optimization (Requirement 8.9)

1. In Layout Inspector, watch the filtered student list
2. Perform actions that don't affect the filter (e.g., scroll, change unrelated state)
3. Verify:
   - ✅ **PASS**: Filtered list doesn't recompute
   - ❌ **FAIL**: Filtered list recomputes on every state change

## Performance Targets

### Frame Rate Targets (Requirement 8.5)
- **120Hz displays**: Filter operations must complete in < 8.3ms
- **144Hz displays**: Filter operations must complete in < 6.9ms

### Recomposition Targets (Requirements 8.3, 8.4, 8.9)
- Filter change should trigger ≤ 3 recompositions total
- Unrelated state changes should NOT trigger filter recomputation
- LazyColumn items should recompose ≤ 2 times per filter change

## Implementation Optimizations

The following optimizations are implemented to achieve high refresh rate performance:

### 1. Background Processing (Requirement 8.7)
```kotlin
private fun applyFilter() {
    viewModelScope.launch(Dispatchers.Default) {
        val filtered = filterStudents(...)
        withContext(Dispatchers.Main) {
            _filteredStudents.value = filtered
        }
    }
}
```

### 2. Stable Keys (Requirement 8.6)
```kotlin
LazyColumn {
    items(
        items = filteredStudents,
        key = { student -> student.id }  // Stable key
    ) { student ->
        // Student row
    }
}
```

### 3. derivedStateOf (Requirement 8.9)
```kotlin
val filteredStudents by remember {
    derivedStateOf {
        filterStudents(students, dailyRecords, filterState)
    }
}
```

### 4. Fast Animations (Requirement 8.1, 8.8)
```kotlin
val animationSpec = spring<Float>(
    dampingRatio = Spring.DampingRatioNoBouncy,
    stiffness = Spring.StiffnessHigh
)
```

### 5. Hardware Acceleration (Requirement 8.2)
```kotlin
Modifier.graphicsLayer {
    // Hardware accelerated transformations
}
```

## Troubleshooting

### Issue: Filter operations take > 10ms
**Solution**: 
- Verify filtering runs on Dispatchers.Default
- Check for unnecessary data copying
- Ensure Map lookups use efficient keys

### Issue: Excessive recompositions
**Solution**:
- Verify derivedStateOf is used for computed values
- Check that FilterState is a data class (structural equality)
- Ensure StateFlow uses distinctUntilChanged()

### Issue: LazyColumn items recompose unnecessarily
**Solution**:
- Verify stable keys are used (student.id)
- Check that item content doesn't depend on unstable state
- Use remember for stable composable instances

## Requirements Coverage

This testing suite covers the following requirements:

- **8.1**: Optimized animations for 144Hz
- **8.2**: Hardware acceleration for scrolling
- **8.3**: Minimized recomposition overhead
- **8.4**: Efficient state management
- **8.5**: Frame rates matching device refresh rate (120-144 FPS)
- **8.6**: LazyColumn with proper key management
- **8.7**: Non-blocking filter operations
- **8.8**: Smooth animations optimized for high refresh rates
- **8.9**: derivedStateOf and remember to minimize recompositions
