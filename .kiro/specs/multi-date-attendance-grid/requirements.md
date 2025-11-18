# Requirements Document

## Introduction

This feature enhances the Class Detail Screen to display attendance records for all scheduled class dates instead of just the current date. The grid will show multiple date columns based on the class schedule configuration (scheduled days of week, start date, and duration).

## Glossary

- **Attendance Grid**: The table/grid in ClassDetailScreen that displays student attendance records
- **Scheduled Date**: A date when the class is scheduled to occur based on scheduleDaysOfWeek
- **Class Duration**: The time period defined by repeatInterval and repeatUnit (e.g., "4 weeks", "2 months")
- **Date Column**: A column in the grid representing one scheduled class date

## Requirements

### Requirement 1: Display Multiple Date Columns

**User Story:** As a teacher, I want to see attendance records for all scheduled class dates in the grid, so that I can view and manage attendance for the entire class duration at once.

#### Acceptance Criteria

1. WHEN the ClassDetailScreen loads, THE Attendance Grid SHALL display one Date Column for each Scheduled Date within the Class Duration
2. WHEN a class has scheduleDaysOfWeek containing multiple days, THE Attendance Grid SHALL display Date Columns for all matching days within the Class Duration
3. WHEN the Class Duration ends, THE Attendance Grid SHALL NOT display Date Columns beyond the end date
4. THE Attendance Grid SHALL display Date Columns in chronological order from left to right
5. WHEN a Scheduled Date is before the class creation date, THE Attendance Grid SHALL NOT display a Date Column for that date

### Requirement 2: Calculate Scheduled Dates

**User Story:** As a teacher, I want the system to automatically calculate all class dates based on my schedule configuration, so that I don't have to manually track which dates the class occurs.

#### Acceptance Criteria

1. THE System SHALL parse scheduleDaysOfWeek JSON to extract the list of scheduled weekdays
2. THE System SHALL convert the createdAt timestamp to a start date
3. THE System SHALL calculate the end date by adding repeatInterval units of repeatUnit to the start date
4. THE System SHALL generate a list of all dates between start date and end date that match the scheduled weekdays
5. WHEN repeatUnit is "WEEK", THE System SHALL add repeatInterval weeks to calculate the end date
6. WHEN repeatUnit is "MONTH", THE System SHALL add repeatInterval months to calculate the end date
7. WHEN repeatUnit is "YEAR", THE System SHALL add repeatInterval years to calculate the end date

### Requirement 3: Load Attendance Data for Multiple Dates

**User Story:** As a teacher, I want to see existing attendance scores for all scheduled dates, so that I can review past attendance records.

#### Acceptance Criteria

1. THE System SHALL load daily records for all Scheduled Dates when ClassDetailScreen loads
2. THE Attendance Grid SHALL display the score in each Date Column cell if a daily record exists for that student and date
3. WHEN no daily record exists for a student and date, THE Attendance Grid SHALL display an empty cell
4. THE System SHALL format scores with one decimal place (e.g., "8.5")

### Requirement 4: Horizontal Scrolling for Many Dates

**User Story:** As a teacher, I want to scroll horizontally through date columns when there are many dates, so that I can view all attendance data even on a small screen.

#### Acceptance Criteria

1. WHEN the total width of all Date Columns exceeds the screen width, THE Attendance Grid SHALL enable horizontal scrolling
2. THE Attendance Grid SHALL synchronize horizontal scrolling between the header row and data rows
3. THE Attendance Grid SHALL keep the "Chi tiết", "STT", "Họ", and "Tên" columns fixed while scrolling date columns
4. WHEN the total width of all columns fits within the screen width, THE Attendance Grid SHALL NOT enable horizontal scrolling

### Requirement 5: Edit Attendance for Any Date

**User Story:** As a teacher, I want to click on any date cell to edit attendance for that specific date, so that I can record or update attendance for past or future class sessions.

#### Acceptance Criteria

1. WHEN a teacher clicks on a Date Column cell, THE System SHALL navigate to the student daily edit screen with the selected date
2. THE System SHALL pass the student ID, class ID, and selected date to the edit screen
3. WHEN the teacher returns from the edit screen, THE Attendance Grid SHALL refresh to show the updated score

### Requirement 6: Date Column Header Format

**User Story:** As a teacher, I want to see clear date labels in the column headers, so that I can easily identify which date each column represents.

#### Acceptance Criteria

1. THE Attendance Grid SHALL display each Date Column header in "dd/MM/yyyy" format
2. WHEN a Scheduled Date is today, THE Date Column header SHALL display the date in "dd/MM/yyyy" format without special highlighting
3. THE Date Column header SHALL use a font size of 12sp
4. THE Date Column header SHALL have a width of 100dp
