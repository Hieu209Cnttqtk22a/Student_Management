# Requirements Document

## Introduction

Hệ thống quản lý học sinh hiện tại có 2 vấn đề cần khắc phục:
1. Khi tạo hoặc chỉnh sửa lớp học với lịch học (chọn thứ mấy, lặp lại bao nhiêu), hệ thống không tự động tạo các bản ghi điểm danh (DailyRecord) cho học sinh theo lịch đã thiết lập
2. Ứng dụng chưa có icon, cần thêm icon từ file `student-management-01.png`

## Glossary

- **System**: Ứng dụng quản lý học sinh Android
- **ClassEntity**: Entity đại diện cho lớp học trong database
- **DailyRecord**: Bản ghi điểm danh hàng ngày cho mỗi học sinh
- **ScheduleGenerator**: Component tạo lịch học tự động
- **App Icon**: Icon hiển thị trên màn hình chính của thiết bị Android

## Requirements

### Requirement 1: Tự động tạo lịch học cho học sinh

**User Story:** Là giáo viên, tôi muốn hệ thống tự động tạo lịch điểm danh cho tất cả học sinh khi tôi thiết lập lịch học của lớp, để tôi không phải tạo thủ công từng buổi học.

#### Acceptance Criteria

1. WHEN giáo viên tạo lớp học mới với lịch học (scheduleDaysOfWeek, repeatInterval, repeatUnit), THE System SHALL tạo DailyRecord cho tất cả học sinh trong lớp theo lịch đã chọn trong vòng 3 tháng tiếp theo
2. WHEN giáo viên chỉnh sửa lịch học của lớp, THE System SHALL xóa các DailyRecord chưa có dữ liệu (score = null, note = null, không có tags) và tạo lại lịch mới cho 3 tháng tiếp theo
3. WHEN hệ thống tạo DailyRecord theo lịch, THE System SHALL tính toán đúng ngày dựa trên scheduleDaysOfWeek (1=CN, 2=T2, 3=T3, 4=T4, 5=T5, 6=T6, 7=T7), repeatInterval và repeatUnit
4. WHEN hệ thống tạo DailyRecord, THE System SHALL không tạo trùng lặp cho cùng một học sinh, lớp và ngày
5. WHEN lớp học chưa có học sinh, THE System SHALL không tạo DailyRecord nào

### Requirement 2: Thêm icon cho ứng dụng

**User Story:** Là người dùng, tôi muốn ứng dụng có icon đẹp trên màn hình chính, để dễ nhận diện và truy cập ứng dụng.

#### Acceptance Criteria

1. THE System SHALL sử dụng file `student-management-01.png` làm launcher icon cho ứng dụng
2. THE System SHALL tạo các kích thước icon phù hợp cho các mật độ màn hình khác nhau (mdpi, hdpi, xhdpi, xxhdpi, xxxhdpi)
3. THE System SHALL hiển thị icon trên màn hình chính của thiết bị Android
4. THE System SHALL hiển thị icon trong danh sách ứng dụng của thiết bị
