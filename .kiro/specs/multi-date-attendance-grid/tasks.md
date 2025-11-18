# Implementation Plan

- [x] 1. Add date calculation logic to ClassDetailViewModel





  - Add `calculateScheduledDates()` function that takes ClassEntity and returns List<LocalDate>
  - Add `parseScheduledDays()` function to parse scheduleDaysOfWeek JSON
  - Add `getEndDate()` function to calculate end date based on repeatInterval and repeatUnit
  - Implement day-of-week conversion between UI format (1=Sunday) and LocalDate format (1=Monday)
  - Add `scheduledDates` field to ClassDetailUiState
  - _Requirements: 2.1, 2.2, 2.3, 2.4, 2.5, 2.6, 2.7_

- [x] 2. Modify data loading to support multiple dates





  - Change `dailyRecords` StateFlow type from `Map<Long, Float?>` to `Map<Pair<Long, String>, Float?>`
  - Update `loadClassDetail()` to calculate scheduled dates and store in UI state
  - Create `loadDailyRecordsForDates()` function to batch load records for all scheduled dates
  - Update the map building logic to use (studentId, dateString) as key
  - _Requirements: 3.1, 3.2, 3.3, 3.4_

- [x] 3. Update ClassDetailScreen grid to display multiple date columns





  - Modify `StudentGridView` signature to accept `scheduledDates: List<LocalDate>` parameter
  - Update `dailyRecords` parameter type to `Map<Pair<Long, String>, Float?>`
  - Remove the single `selectedDate` parameter and header display
  - Update header row to dynamically generate date column headers using `scheduledDates.forEach`
  - Format date headers as "dd/MM/yyyy"
  - _Requirements: 1.1, 1.2, 1.4, 6.1, 6.2, 6.3, 6.4_

- [x] 4. Update data rows to display scores for all dates





  - Modify `EditableStudentRow` to iterate through `scheduledDates` and create a GridCell for each date
  - Update score lookup to use `Pair(student.id, dateString)` as key
  - Format scores with one decimal place using `String.format("%.1f", score)`
  - Display empty cell when no record exists for a date
  - Update `NewStudentRow` to create empty cells for all scheduled dates
  - _Requirements: 3.2, 3.3, 3.4_

- [x] 5. Implement horizontal scrolling for date columns





  - Calculate total width of all columns (340dp fixed + 100dp per date)
  - Add logic to enable horizontal scrolling only when total width exceeds screen width
  - Apply shared `horizontalScrollState` to both header row and all data rows
  - Ensure fixed columns (Chi tiết, STT, Họ, Tên) remain visible during scrolling
  - _Requirements: 4.1, 4.2, 4.3, 4.4_

- [x] 6. Update click handlers to pass selected date





  - Modify `onStudentClick` callback signature to accept both studentId and dateString
  - Update GridCell onClick to pass the specific date being clicked
  - Update navigation call in ClassDetailScreen to include the selected date parameter
  - Ensure navigation to edit screen works with the correct date
  - _Requirements: 5.1, 5.2, 5.3_

- [x] 7. Handle edge cases and empty states





  - Add check for empty scheduledDates list and display appropriate message
  - Handle classes with no scheduled days configured
  - Ensure dates before class creation date are not displayed
  - Ensure dates after class end date are not displayed
  - _Requirements: 1.3, 1.5, 2.1_

- [x] 8. Add unit tests for date calculation




  - Write test for single weekday schedule
  - Write test for multiple weekdays schedule
  - Write test for different repeatUnits (WEEK, MONTH, YEAR)
  - Write test for edge cases (start date, end date boundaries)
  - _Requirements: 2.1, 2.2, 2.3, 2.4, 2.5, 2.6, 2.7_
