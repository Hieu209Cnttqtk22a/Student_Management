# Requirements Document

## Introduction

Tính năng lọc điểm học sinh cho phép giáo viên lọc và xem danh sách học sinh theo các tiêu chí điểm số cụ thể trong một ngày học nhất định. Tính năng này giúp giáo viên nhanh chóng xác định các học sinh cần quan tâm đặc biệt như học sinh có điểm kém, chưa có điểm, hoặc đạt điểm tối đa.

## Glossary

- **System**: Hệ thống quản lý học sinh (Student Management App)
- **Filter Combo Box**: Hộp chọn lọc dạng dropdown cho phép chọn một trong các tiêu chí lọc
- **Score Filter**: Bộ lọc điểm số áp dụng cho danh sách học sinh
- **Daily Record**: Bản ghi điểm số hàng ngày của học sinh
- **Scheduled Date**: Ngày học đã được lên lịch trong lớp học
- **Student Grid**: Bảng hiển thị danh sách học sinh và điểm số theo ngày

## Requirements

### Requirement 1

**User Story:** Là một giáo viên, tôi muốn có một combo box để chọn tiêu chí lọc điểm, để tôi có thể nhanh chóng xem các nhóm học sinh cụ thể.

#### Acceptance Criteria

1. WHEN THE System renders the class detail screen, THE System SHALL display a filter combo box in the top app bar
2. THE System SHALL provide the following filter options in the combo box: "Tất cả", "Điểm kém (< 7)", "Chưa có điểm", "Điểm 10"
3. WHEN a user clicks on the filter combo box, THE System SHALL display a dropdown menu with all available filter options
4. THE System SHALL highlight the currently selected filter option in the dropdown menu
5. WHEN a user selects a filter option, THE System SHALL close the dropdown menu and update the displayed filter label

### Requirement 2

**User Story:** Là một giáo viên, tôi muốn lọc học sinh có điểm kém trong một ngày cụ thể, để tôi có thể xác định học sinh cần hỗ trợ thêm.

#### Acceptance Criteria

1. WHEN a user selects the "Điểm kém (< 7)" filter option, THE System SHALL prompt the user to select a specific date
2. WHEN a user selects a date from the scheduled dates, THE System SHALL filter the student list to show only students with scores less than 7.0 on that date
3. THE System SHALL hide students who have scores greater than or equal to 7.0 on the selected date
4. THE System SHALL hide students who have no score recorded on the selected date
5. IF no students match the filter criteria, THEN THE System SHALL display a message indicating no students found with the specified criteria

### Requirement 3

**User Story:** Là một giáo viên, tôi muốn lọc học sinh chưa có điểm trong một ngày cụ thể, để tôi có thể nhắc nhở nhập điểm cho các học sinh đó.

#### Acceptance Criteria

1. WHEN a user selects the "Chưa có điểm" filter option, THE System SHALL prompt the user to select a specific date
2. WHEN a user selects a date from the scheduled dates, THE System SHALL filter the student list to show only students with no score recorded on that date
3. THE System SHALL hide students who have any score value (including 0) recorded on the selected date
4. IF no students match the filter criteria, THEN THE System SHALL display a message indicating all students have scores recorded

### Requirement 4

**User Story:** Là một giáo viên, tôi muốn lọc học sinh đạt điểm 10 trong một ngày cụ thể, để tôi có thể khen thưởng các học sinh xuất sắc.

#### Acceptance Criteria

1. WHEN a user selects the "Điểm 10" filter option, THE System SHALL prompt the user to select a specific date
2. WHEN a user selects a date from the scheduled dates, THE System SHALL filter the student list to show only students with scores equal to 10.0 on that date
3. THE System SHALL hide students who have scores not equal to 10.0 on the selected date
4. THE System SHALL hide students who have no score recorded on the selected date
5. IF no students match the filter criteria, THEN THE System SHALL display a message indicating no students achieved a perfect score

### Requirement 5

**User Story:** Là một giáo viên, tôi muốn xóa bộ lọc và xem tất cả học sinh, để tôi có thể quay lại chế độ xem đầy đủ.

#### Acceptance Criteria

1. WHEN a user selects the "Tất cả" filter option, THE System SHALL display all students in the class regardless of their scores
2. THE System SHALL clear any previously selected date filter
3. THE System SHALL display all scheduled date columns in the student grid
4. THE System SHALL restore the default view state of the student grid

### Requirement 6

**User Story:** Là một giáo viên, tôi muốn thấy ngày đã chọn khi áp dụng bộ lọc, để tôi biết đang xem điểm của ngày nào.

#### Acceptance Criteria

1. WHEN a date-specific filter is active, THE System SHALL display the selected date in the filter combo box label
2. THE System SHALL format the displayed date as "dd/MM/yyyy" for consistency with the rest of the application
3. WHEN the filter is cleared, THE System SHALL remove the date from the filter label
4. THE System SHALL persist the filter state when the user navigates away and returns to the class detail screen

### Requirement 7

**User Story:** Là một giáo viên, tôi muốn có một nút reset để nhanh chóng xóa bộ lọc, để tôi không cần mở combo box và chọn "Tất cả".

#### Acceptance Criteria

1. WHEN a filter is active (not "Tất cả"), THE System SHALL display a reset button in the top app bar next to the filter combo box
2. WHEN no filter is active, THE System SHALL hide the reset button
3. WHEN a user clicks the reset button, THE System SHALL clear all active filters and return to the "Tất cả" view
4. WHEN a user clicks the reset button, THE System SHALL clear any selected date filter
5. THE System SHALL provide visual feedback (icon or text) on the reset button to indicate its purpose

### Requirement 8

**User Story:** Là một người dùng có màn hình tần số quét cao, tôi muốn UI hoạt động mượt mà ở 120Hz/144Hz, để trải nghiệm sử dụng ứng dụng được tối ưu.

#### Acceptance Criteria

1. THE System SHALL optimize all animations and transitions to support refresh rates up to 144Hz
2. THE System SHALL use hardware acceleration for scrolling operations in the student grid
3. THE System SHALL minimize recomposition overhead when filtering the student list
4. THE System SHALL implement efficient state management to prevent unnecessary UI updates
5. WHEN rendering the student grid with filters applied, THE System SHALL maintain frame rates matching the device refresh rate (120-144 FPS) on devices with 120Hz or 144Hz displays
6. THE System SHALL use LazyColumn with proper key management to optimize list rendering performance
7. THE System SHALL defer non-critical UI updates to avoid blocking the main thread during filter operations
8. THE System SHALL implement smooth animations with duration and easing curves optimized for high refresh rate displays
9. THE System SHALL use Compose's derivedStateOf and remember functions to minimize unnecessary recompositions during filter changes
