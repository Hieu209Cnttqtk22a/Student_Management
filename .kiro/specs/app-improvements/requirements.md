# Requirements Document

## Introduction

Tài liệu này mô tả các yêu cầu cải tiến cho ứng dụng quản lý học sinh hiện tại. Các cải tiến tập trung vào việc loại bỏ dữ liệu demo, sửa lỗi chức năng dark/light mode, cải thiện giao diện nhập liệu, và bổ sung tính năng chọn lịch làm việc với khung thời gian linh hoạt.

## Glossary

- **Application**: Ứng dụng quản lý học sinh được xây dựng bằng Kotlin và Jetpack Compose
- **Demo Data**: Dữ liệu mẫu được tạo sẵn trong ứng dụng để minh họa
- **Dark Mode**: Chế độ giao diện tối của ứng dụng
- **Light Mode**: Chế độ giao diện sáng của ứng dụng
- **Calendar View**: Màn hình hiển thị lịch làm việc
- **Time Period Selector**: Bộ chọn khung thời gian (tuần/tháng/năm)
- **Class Detail Screen**: Màn hình chi tiết lớp học (detail_class_UI)
- **Daily Record**: Bản ghi hằng ngày của học sinh

## Requirements

### Requirement 1

**User Story:** Là một giáo viên, tôi muốn ứng dụng không có dữ liệu demo khi khởi động lần đầu, để tôi có thể bắt đầu với dữ liệu thực của mình

#### Acceptance Criteria

1. WHEN the Application launches for the first time, THE Application SHALL display an empty state without any pre-populated demo data
2. WHEN the Application initializes the database, THE Application SHALL create only the necessary schema and tag definitions without creating any demo classes or students
3. WHEN a user opens the class list screen, THE Application SHALL display an empty list with a message prompting the user to create their first class
4. WHEN a user opens the calendar view, THE Application SHALL display an empty calendar without any pre-scheduled sessions

### Requirement 2

**User Story:** Là một giáo viên, tôi muốn xem lịch cụ thể của ngày hiện tại, để tôi biết các buổi học hôm nay

#### Acceptance Criteria

1. WHEN the Application opens the Calendar View, THE Application SHALL highlight the current date by default
2. WHEN the Application displays the Calendar View, THE Application SHALL show all scheduled classes for the current date in a list below the calendar
3. WHEN a user selects the current date on the calendar, THE Application SHALL display detailed information about each scheduled class for that date
4. WHEN the current date has no scheduled classes, THE Application SHALL display a message indicating no classes are scheduled for today

### Requirement 3

**User Story:** Là một người dùng, tôi muốn chức năng dark/light mode hoạt động chính xác, để tôi có thể thay đổi giao diện theo sở thích

#### Acceptance Criteria

1. WHEN a user toggles the dark mode switch in settings, THE Application SHALL immediately apply the dark theme to all screens
2. WHEN a user toggles the light mode switch in settings, THE Application SHALL immediately apply the light theme to all screens
3. WHEN the Application restarts, THE Application SHALL load and apply the previously saved theme preference
4. WHEN the theme changes, THE Application SHALL update all UI components including navigation bar, status bar, and content areas with the appropriate color scheme

### Requirement 4

**User Story:** Là một giáo viên, tôi muốn giao diện nhập liệu đơn giản hơn bằng cách loại bỏ các trường không cần thiết, để tôi có thể nhập dữ liệu nhanh hơn

#### Acceptance Criteria

1. WHEN the Application displays the Class Detail Screen, THE Application SHALL not display input fields for "Môn học" (Subject)
2. WHEN the Application displays the Class Detail Screen, THE Application SHALL not display input fields for "Ghi chú" (Note)
3. WHEN a user creates a new class, THE Application SHALL only require the class name and schedule information
4. WHEN a user edits an existing class, THE Application SHALL only allow editing of class name and schedule information

### Requirement 5

**User Story:** Là một giáo viên, tôi muốn chọn khung thời gian xem lịch (tuần/tháng/năm), để tôi có thể lập kế hoạch dài hạn hoặc ngắn hạn

#### Acceptance Criteria

1. WHEN the Application displays the Calendar View, THE Application SHALL provide a selector with three options: "Tuần" (Week), "Tháng" (Month), and "Năm" (Year)
2. WHEN a user selects "Tuần" (Week), THE Application SHALL display a weekly calendar view showing 7 days starting from Monday
3. WHEN a user selects "Tháng" (Month), THE Application SHALL display a monthly calendar view showing all days in the current month
4. WHEN a user selects "Năm" (Year), THE Application SHALL display a yearly overview showing all 12 months with indicators for scheduled classes
5. WHEN the time period changes, THE Application SHALL persist the user's selection and restore it on the next app launch
