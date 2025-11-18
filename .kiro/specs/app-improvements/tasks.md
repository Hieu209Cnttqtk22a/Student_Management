# Implementation Plan

- [x] 1. Remove demo data from database initialization





  - Modify `StudentManagementDatabase.kt` DatabaseCallback to only initialize tags
  - Remove any demo class, student, or daily record creation logic
  - Verify database starts empty on fresh install
  - _Requirements: 1.1, 1.2, 1.3, 1.4_

- [x] 2. Fix dark/light mode functionality





- [x] 2.1 Create SettingsViewModel


  - Create `SettingsViewModel.kt` in viewmodel package
  - Inject SettingsDataStore dependency
  - Expose StateFlows for darkMode, reminderEnabled, and reminderMinutes
  - Implement functions to update each setting
  - _Requirements: 3.1, 3.2, 3.3, 3.4_

- [x] 2.2 Update MainActivity to read theme preference


  - Inject SettingsDataStore in MainActivity
  - Collect darkModeFlow using collectAsState
  - Pass darkTheme parameter to StudentManagementTheme
  - _Requirements: 3.3, 3.4_

- [x] 2.3 Update SettingsScreen to use ViewModel


  - Replace local state with ViewModel state
  - Connect toggle switches to ViewModel functions
  - Use collectAsState for reactive UI updates
  - _Requirements: 3.1, 3.2_

- [x] 3. Simplify class input by removing subject and note fields





- [x] 3.1 Update ClassCreateScreen


  - Remove TextField for "Môn học" (subject)
  - Remove TextField for "Ghi chú" (note)
  - Set subject and note to null when creating class
  - _Requirements: 4.1, 4.3_

- [x] 3.2 Update ClassEditScreen


  - Remove TextField for "Môn học" (subject)
  - Remove TextField for "Ghi chú" (note)
  - Preserve existing values but don't allow editing
  - _Requirements: 4.2, 4.4_

- [x] 3.3 Update ClassDetailScreen


  - Remove display of subject field
  - Remove display of note field
  - Keep other class information intact
  - _Requirements: 4.1, 4.2_

- [x] 4. Add calendar period selector and enhance calendar view




- [x] 4.1 Create CalendarPeriod enum and selector component


  - Create `CalendarPeriod.kt` enum with WEEK, MONTH, YEAR values
  - Create `CalendarPeriodSelector.kt` composable component
  - Implement segmented control UI with three buttons
  - Add proper styling and animations
  - _Requirements: 5.1_

- [x] 4.2 Extend SettingsDataStore for calendar period


  - Add CALENDAR_PERIOD_KEY preference key
  - Add calendarPeriodFlow property
  - Add setCalendarPeriod suspend function
  - _Requirements: 5.5_

- [x] 4.3 Create CalendarViewModel


  - Create `CalendarViewModel.kt` with CalendarUiState
  - Inject SettingsDataStore and ClassRepository
  - Implement period selection logic
  - Implement date navigation functions
  - Implement getClassesForDate function
  - _Requirements: 5.1, 5.2, 5.3, 5.4, 5.5_

- [x] 4.4 Implement week view


  - Create WeekCalendarView composable
  - Display 7 days from Monday to Sunday
  - Add horizontal scroll for previous/next week
  - Highlight current date
  - Show scheduled classes for the week
  - _Requirements: 5.2_

- [x] 4.5 Enhance month view


  - Update existing month calendar implementation
  - Improve current date highlighting with border and background
  - Add indicators for days with scheduled classes
  - Ensure proper styling matches design
  - _Requirements: 5.3_

- [x] 4.6 Implement year view


  - Create YearCalendarView composable
  - Display 3x4 grid of 12 months
  - Show month name and class count for each month
  - Highlight current month
  - Add tap handler to navigate to month detail
  - _Requirements: 5.4_

- [x] 4.7 Update CalendarScreen to use ViewModel and new components


  - Integrate CalendarPeriodSelector at top of screen
  - Switch between week/month/year views based on selected period
  - Update "Buổi học hôm nay" section to show classes for current date
  - Connect to ViewModel for data and state management
  - _Requirements: 5.1, 5.2, 5.3, 5.4_

- [x] 4.8 Implement current date specific features


  - Ensure current date is highlighted by default on calendar open
  - Load and display scheduled classes for current date
  - Show empty state message when no classes scheduled for today
  - Add navigation to ClassDetailScreen when tapping a class
  - _Requirements: 2.1, 2.2, 2.3, 2.4_
