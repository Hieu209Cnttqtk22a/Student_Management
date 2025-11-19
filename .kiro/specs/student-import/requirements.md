# Requirements Document - Student Import & Class Reminder Features

## Introduction

This document covers two main features:
1. Student Import: Allows teachers to import student names from external files (CSV or Excel) into a class
2. Class Reminder: Provides notification reminders before scheduled class sessions

## Glossary

- **Import System**: The component responsible for reading and parsing external files
- **File Parser**: Component that reads CSV and Excel files
- **Name Detector**: Component that identifies name columns in the file
- **Student Importer**: Component that creates student records in the database
- **Reminder System**: The component responsible for scheduling and triggering class notifications
- **Notification Manager**: Component that displays notifications with sound
- **Reminder Scheduler**: Component that schedules reminders based on user-defined lead time

## Requirements

### Requirement 1: File Selection

**User Story:** As a teacher, I want to select a CSV or Excel file from my device, so that I can import student names into my class.

#### Acceptance Criteria

1. WHEN the teacher taps the import button, THE Import System SHALL display a file picker dialog
2. THE Import System SHALL accept files with extensions .csv, .xls, and .xlsx
3. WHEN a file is selected, THE Import System SHALL validate the file format
4. IF the file format is invalid, THEN THE Import System SHALL display an error message to the teacher

### Requirement 2: Name Column Detection

**User Story:** As a teacher, I want the system to automatically detect name columns, so that I don't have to manually specify which column contains student names.

#### Acceptance Criteria

1. WHEN a file is loaded, THE Name Detector SHALL scan all column headers
2. THE Name Detector SHALL identify columns with headers containing "tên", "họ", "name", "student", "học sinh" (case-insensitive)
3. IF multiple name columns are found, THE Name Detector SHALL combine them (e.g., "Họ" + "Tên")
4. IF no name column is detected, THE Name Detector SHALL allow the teacher to manually select the column
5. THE Name Detector SHALL display a preview of detected names before import

### Requirement 3: Student Import

**User Story:** As a teacher, I want to import all detected student names into my class, so that I can quickly populate my class roster.

#### Acceptance Criteria

1. WHEN the teacher confirms the import, THE Student Importer SHALL create a student record for each detected name
2. THE Student Importer SHALL skip empty rows or rows without names
3. THE Student Importer SHALL trim whitespace from student names
4. IF a student name already exists in the class, THE Student Importer SHALL skip that student
5. WHEN import is complete, THE Student Importer SHALL display a summary showing the number of students added and skipped

### Requirement 4: Error Handling

**User Story:** As a teacher, I want to see clear error messages if the import fails, so that I can fix the issue and try again.

#### Acceptance Criteria

1. IF the file cannot be read, THE Import System SHALL display an error message with the reason
2. IF no names are detected, THE Import System SHALL inform the teacher and suggest manual column selection
3. IF the import is interrupted, THE Import System SHALL rollback any partial imports
4. THE Import System SHALL log all import errors for debugging purposes

### Requirement 5: Import Progress

**User Story:** As a teacher, I want to see the progress of the import, so that I know the system is working.

#### Acceptance Criteria

1. WHILE importing students, THE Import System SHALL display a progress indicator
2. THE Import System SHALL show the current number of students processed
3. WHEN import is complete, THE Import System SHALL automatically refresh the student list


### Requirement 6: Class Reminder Configuration

**User Story:** As a teacher, I want to set up reminders for my classes, so that I don't forget about upcoming lessons.

#### Acceptance Criteria

1. WHEN creating or editing a class, THE Reminder System SHALL allow the teacher to enable reminders
2. THE Reminder System SHALL allow the teacher to specify reminder lead time in minutes (e.g., 15, 30, 60 minutes before class)
3. THE Reminder System SHALL save the reminder configuration with the class settings
4. THE Reminder System SHALL allow the teacher to disable reminders for a class
5. THE Reminder System SHALL provide default reminder options (15, 30, 60 minutes) and allow custom input

### Requirement 7: Reminder Scheduling

**User Story:** As a teacher, I want the system to automatically schedule reminders for all my class sessions, so that I receive timely notifications.

#### Acceptance Criteria

1. WHEN a class with reminders enabled is saved, THE Reminder Scheduler SHALL calculate all upcoming class sessions
2. THE Reminder Scheduler SHALL schedule a notification for each class session based on the configured lead time
3. WHEN the class schedule is modified, THE Reminder Scheduler SHALL update all pending reminders
4. THE Reminder Scheduler SHALL cancel reminders for deleted classes
5. THE Reminder Scheduler SHALL reschedule reminders when the reminder lead time is changed

### Requirement 8: Notification Display

**User Story:** As a teacher, I want to receive a notification with sound before my class starts, so that I am reminded to prepare.

#### Acceptance Criteria

1. WHEN the scheduled reminder time arrives, THE Notification Manager SHALL display a system notification
2. THE Notification Manager SHALL include the class name and start time in the notification
3. THE Notification Manager SHALL play the system default notification sound
4. WHEN the teacher taps the notification, THE Notification Manager SHALL open the class detail screen
5. THE Notification Manager SHALL display notifications even when the app is in the background or closed

### Requirement 9: Reminder Permissions

**User Story:** As a teacher, I want to grant notification permissions, so that I can receive class reminders.

#### Acceptance Criteria

1. WHEN the teacher first enables reminders, THE Reminder System SHALL request notification permission
2. IF notification permission is denied, THE Reminder System SHALL display a message explaining why permission is needed
3. THE Reminder System SHALL provide a link to app settings if permission needs to be granted manually
4. THE Reminder System SHALL check for notification permission before scheduling reminders

### Requirement 10: Reminder Management

**User Story:** As a teacher, I want to view and manage my upcoming reminders, so that I can verify they are set correctly.

#### Acceptance Criteria

1. THE Reminder System SHALL display the reminder status (enabled/disabled) on the class detail screen
2. THE Reminder System SHALL show the configured lead time for each class
3. THE Reminder System SHALL allow the teacher to quickly enable/disable reminders from the class list
4. WHEN a reminder is triggered, THE Reminder System SHALL mark it as delivered
5. THE Reminder System SHALL not schedule reminders for past class sessions


### Requirement 11: Calendar View with Class Count

**User Story:** As a teacher, I want to see a calendar view with the number of classes on each day, so that I can quickly see my schedule at a glance.

#### Acceptance Criteria

1. THE Calendar System SHALL display a monthly calendar view
2. WHEN a day has scheduled classes, THE Calendar System SHALL display a small badge with the class count
3. THE Calendar System SHALL position the badge in the top-right corner of the date cell
4. THE Calendar System SHALL calculate the class count based on the class schedule configuration (scheduleDaysOfWeek, repeatInterval, repeatUnit)
5. THE Calendar System SHALL synchronize the class count with the actual scheduled class sessions
6. WHEN a class schedule is modified, THE Calendar System SHALL update the badge counts for affected dates
7. WHEN a day has no classes, THE Calendar System SHALL not display a badge

### Requirement 12: Calendar Navigation

**User Story:** As a teacher, I want to navigate between months in the calendar, so that I can view my schedule for different time periods.

#### Acceptance Criteria

1. THE Calendar System SHALL allow the teacher to navigate to the previous month
2. THE Calendar System SHALL allow the teacher to navigate to the next month
3. THE Calendar System SHALL highlight the current day
4. THE Calendar System SHALL display the month and year at the top of the calendar
5. WHEN navigating to a different month, THE Calendar System SHALL update the class count badges

### Requirement 13: Calendar Day Selection

**User Story:** As a teacher, I want to tap on a day in the calendar to see the classes scheduled for that day, so that I can view details.

#### Acceptance Criteria

1. WHEN the teacher taps on a day, THE Calendar System SHALL display a list of classes scheduled for that day
2. THE Calendar System SHALL show the class name, time, and student count for each class
3. WHEN the teacher taps on a class in the list, THE Calendar System SHALL navigate to the class detail screen
4. THE Calendar System SHALL highlight the selected day in the calendar
5. IF a day has no classes, THE Calendar System SHALL display a message indicating no classes are scheduled

### Requirement 14: Calendar Badge Styling

**User Story:** As a teacher, I want the class count badges to be visually distinct, so that I can easily identify busy days.

#### Acceptance Criteria

1. THE Calendar System SHALL display badges with a contrasting background color
2. THE Calendar System SHALL use a small, readable font size for the count number
3. THE Calendar System SHALL ensure badges do not overlap with the date number
4. THE Calendar System SHALL display the actual number of classes regardless of count (no limit)
5. THE Calendar System SHALL adjust badge size to accommodate larger numbers
6. THE Calendar System SHALL use consistent badge styling across all calendar dates
