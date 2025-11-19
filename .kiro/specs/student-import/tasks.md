# Implementation Plan - Student Import, Class Reminder & Calendar Features

- [x] 1. Setup dependencies and permissions





  - Add Apache POI dependencies to build.gradle
  - Add required permissions to AndroidManifest.xml (READ_EXTERNAL_STORAGE, POST_NOTIFICATIONS, SCHEDULE_EXACT_ALARM)
  - Register ReminderBroadcastReceiver in AndroidManifest.xml
  - _Requirements: 1.1, 6.1, 9.1_

- [x] 2. Create data models and database schema





  - [x] 2.1 Create ReminderEntity with Room annotations


    - Define table structure with classId, scheduledTime, leadTimeMinutes, isDelivered fields
    - Add foreign key relationship to ClassEntity
    - _Requirements: 7.1, 7.4_
  
  - [x] 2.2 Update ClassEntity with reminder fields


    - Add reminderEnabled and reminderLeadTimeMinutes fields
    - Set default values (false, 30 minutes)
    - _Requirements: 6.3, 6.5_
  
  - [x] 2.3 Create ReminderDao interface


    - Define insert, update, delete, and query methods
    - Add method to get pending reminders for a class
    - Add method to mark reminder as delivered
    - _Requirements: 7.2, 10.4_
  
  - [x] 2.4 Update database version and create migration


    - Increment database version number
    - Create migration to add reminders table and update classes table
    - _Requirements: 6.3_

- [x] 3. Implement file parsing service




  - [x] 3.1 Create FileParser interface and data models


    - Define ParseResult, NameColumnInfo data classes
    - Create FileParser interface with parseFile and detectNameColumns methods
    - _Requirements: 1.3, 2.1_
  
  - [x] 3.2 Implement CSVParser


    - Read CSV file from Uri using InputStream
    - Parse rows with comma/semicolon delimiter support
    - Handle quoted fields and escape characters
    - _Requirements: 1.2, 1.3_
  
  - [x] 3.3 Implement ExcelParser using Apache POI


    - Read .xls and .xlsx files
    - Extract first sheet data
    - Convert cells to string values
    - _Requirements: 1.2, 1.3_
  
  - [x] 3.4 Implement name column detection logic


    - Scan headers for keywords: "tên", "họ", "name", "student", "học sinh" (case-insensitive)
    - Identify first name, last name, and full name columns
    - Combine first and last name if both are present
    - _Requirements: 2.1, 2.2, 2.3_

- [x] 4. Create student import UI and ViewModel





  - [x] 4.1 Create ImportViewModel


    - Implement file selection and parsing logic
    - Track import progress (current/total)
    - Generate import summary (added/skipped/errors)
    - _Requirements: 3.1, 5.1, 5.2_
  

  - [x] 4.2 Create StudentImportScreen composable

    - Add file picker button
    - Display preview table with detected names
    - Show column selection dropdown if auto-detection fails
    - Add confirm and cancel buttons
    - _Requirements: 1.1, 2.4, 2.5_
  
  - [x] 4.3 Implement import confirmation dialog

    - Show preview of students to be imported
    - Display import progress indicator
    - Show import summary after completion
    - _Requirements: 2.5, 5.1, 5.3_
  
  - [x] 4.4 Add import button to ClassDetailScreen


    - Add "Import Students" button to class detail toolbar or FAB
    - Navigate to StudentImportScreen with classId parameter
    - _Requirements: 1.1_

- [x] 5. Implement student import logic











  - [x] 5.1 Create bulk student import method in StudentRepository


    - Accept list of student names and classId
    - Check for duplicate names in the class
    - Batch insert students using transaction
    - Return import summary
    - _Requirements: 3.1, 3.3, 3.4_
  
  - [x] 5.2 Implement duplicate detection

    - Query existing students in the class
    - Compare names (case-insensitive, trimmed)
    - Skip duplicates and track in summary
    - _Requirements: 3.4_
  
  - [x] 5.3 Add error handling for import process











    - Catch file read errors
    - Handle parse errors with line numbers
    - Rollback transaction on failure
    - _Requirements: 4.1, 4.3_

- [ ] 6. Implement reminder service and scheduling
  - [ ] 6.1 Create ReminderRepository
    - Implement CRUD operations for ReminderEntity
    - Add methods to query pending reminders
    - Add method to mark reminders as delivered
    - _Requirements: 7.1, 10.4_
  
  - [ ] 6.2 Create ReminderService
    - Implement scheduleRemindersForClass method using AlarmManager
    - Calculate reminder times based on class schedule and lead time
    - Create PendingIntent for each reminder
    - _Requirements: 7.1, 7.2, 8.5_
  
  - [ ] 6.3 Implement reminder cancellation logic
    - Cancel all pending alarms for a class
    - Delete reminder records from database
    - Handle class deletion and schedule changes
    - _Requirements: 7.3, 7.4_
  
  - [ ] 6.4 Create ReminderBroadcastReceiver
    - Handle reminder broadcast intent
    - Extract class information from intent extras
    - Trigger notification display
    - Mark reminder as delivered in database
    - _Requirements: 8.1, 10.4_

- [ ] 7. Implement notification system
  - [ ] 7.1 Create NotificationHelper
    - Create notification channel for class reminders
    - Build notification with class name and time
    - Set notification sound to system default
    - Create PendingIntent to open class detail screen
    - _Requirements: 8.1, 8.2, 8.3, 8.4_
  
  - [ ] 7.2 Implement notification permission handling
    - Check for POST_NOTIFICATIONS permission (Android 13+)
    - Request permission when enabling reminders
    - Show explanation dialog if permission denied
    - Provide link to app settings
    - _Requirements: 9.1, 9.2, 9.3_
  
  - [ ] 7.3 Initialize notification channel on app startup
    - Create notification channel in Application class or MainActivity
    - Set channel importance to HIGH for sound
    - _Requirements: 8.3_

- [ ] 8. Add reminder UI to class screens
  - [ ] 8.1 Update ClassEditScreen with reminder settings
    - Add reminder enable/disable toggle
    - Add lead time input field with preset options (15, 30, 60 minutes)
    - Allow custom lead time input
    - _Requirements: 6.1, 6.2, 6.5_
  
  - [ ] 8.2 Update ClassEditViewModel
    - Add reminder state fields
    - Save reminder settings with class
    - Trigger reminder scheduling when class is saved
    - _Requirements: 6.3, 7.2_
  
  - [ ] 8.3 Add reminder indicator to ClassDetailScreen
    - Display reminder status (enabled/disabled)
    - Show configured lead time
    - Add quick toggle for enable/disable
    - _Requirements: 10.1, 10.2_
  
  - [ ] 8.4 Add reminder indicator to ClassListScreen
    - Show small icon if reminders are enabled for a class
    - Allow quick enable/disable from list
    - _Requirements: 10.3_

- [ ] 9. Implement calendar view with badges
  - [ ] 9.1 Create CalendarViewModel
    - Implement month navigation (previous/next)
    - Load all classes and calculate counts per date
    - Handle date selection
    - Load classes for selected date
    - _Requirements: 11.1, 11.4, 12.1, 12.2, 13.1_
  
  - [ ] 9.2 Create CalendarScreen composable
    - Build monthly calendar grid (7 columns x 5-6 rows)
    - Display month/year header with navigation buttons
    - Highlight current day and selected day
    - _Requirements: 11.1, 12.3, 12.4, 13.4_
  
  - [ ] 9.3 Implement CalendarDayCell with badge
    - Display date number
    - Show badge with class count in top-right corner
    - Handle click to select date
    - Apply styling for today, selected, and normal states
    - _Requirements: 11.2, 11.3, 14.1, 14.2, 14.3_
  
  - [ ] 9.4 Implement badge component
    - Create reusable Badge composable
    - Support dynamic sizing based on number length
    - Use contrasting background color
    - Ensure readability with small font
    - _Requirements: 14.4, 14.5, 14.6_
  
  - [ ] 9.5 Create ClassListForDate component
    - Display list of classes for selected date
    - Show class name, time, and student count
    - Handle click to navigate to class detail
    - Show "no classes" message when empty
    - _Requirements: 13.2, 13.3, 13.5_

- [ ] 10. Enhance ScheduleService for calendar
  - [ ] 10.1 Add getClassCountForDate method
    - Check if class is scheduled on given date
    - Match against scheduleDaysOfWeek
    - Verify date is within class period
    - _Requirements: 11.4, 11.5_
  
  - [ ] 10.2 Add getClassCountsForMonth method
    - Iterate through all days in month
    - Calculate class count for each day
    - Return map of date to count
    - Optimize by caching results
    - _Requirements: 11.4, 11.5, 12.5_
  
  - [ ] 10.3 Add getClassesForDate method
    - Return list of classes scheduled on specific date
    - Include class details needed for display
    - _Requirements: 13.1, 13.2_

- [ ] 11. Add navigation and integrate features
  - [ ] 11.1 Add CalendarScreen to navigation graph
    - Create route for calendar screen
    - Add navigation from home screen or bottom nav
    - _Requirements: 11.1_
  
  - [ ] 11.2 Add StudentImportScreen to navigation graph
    - Create route with classId parameter
    - Add navigation from ClassDetailScreen
    - _Requirements: 1.1_
  
  - [ ] 11.3 Wire up reminder scheduling on class save
    - Call ReminderService.scheduleRemindersForClass after class creation/update
    - Cancel old reminders before rescheduling
    - _Requirements: 7.2, 7.3_
  
  - [ ] 11.4 Update calendar when class schedule changes
    - Refresh calendar view after class edit
    - Update badge counts automatically
    - _Requirements: 11.6, 12.5_

- [ ] 12. Handle edge cases and polish
  - [ ] 12.1 Handle file size limits
    - Check file size before parsing (max 10MB)
    - Show error if file is too large
    - _Requirements: 4.1_
  
  - [ ] 12.2 Handle timezone for reminders
    - Use system timezone for all time calculations
    - Handle daylight saving time changes
    - _Requirements: 7.1_
  
  - [ ] 12.3 Handle app updates and reminder persistence
    - Reschedule reminders after app update
    - Use WorkManager for reliability across reboots
    - _Requirements: 8.5_
  
  - [ ] 12.4 Add loading states and error messages
    - Show loading indicator during file parsing
    - Display user-friendly error messages
    - Provide retry options
    - _Requirements: 4.1, 4.2, 5.1_
