# Implementation Plan

- [x] 1. Tạo ScheduleCalculator utility class





  - Tạo file `app/src/main/kotlin/com/studentmanagement/app/util/ScheduleCalculator.kt`
  - Implement hàm `calculateScheduleDates()` để tính toán các ngày học dựa trên scheduleDaysOfWeek, repeatInterval, repeatUnit
  - Xử lý logic cho WEEK, MONTH, YEAR repeat units
  - Parse JSON scheduleDaysOfWeek thành List<Int>
  - _Requirements: 1.1, 1.3_

- [x] 2. Mở rộng DailyRecordRepository





  - Thêm hàm `createBulkRecords(records: List<DailyRecordEntity>)` để insert nhiều records cùng lúc
  - Thêm hàm `deleteEmptyRecordsByClass(classId: Long)` để xóa records chưa có dữ liệu
  - Thêm hàm `recordExists(studentId: Long, classId: Long, date: String): Boolean` để check duplicate
  - _Requirements: 1.2, 1.4_

- [x] 3. Tạo ScheduleService





  - Tạo file `app/src/main/kotlin/com/studentmanagement/app/service/ScheduleService.kt`
  - Inject StudentRepository, DailyRecordRepository, ScheduleCalculator
  - Implement `generateScheduleForClass(classEntity: ClassEntity)` để tạo lịch cho lớp mới
  - Implement `regenerateScheduleForClass(classEntity: ClassEntity)` để tạo lại lịch khi sửa
  - Xử lý trường hợp lớp chưa có học sinh (không tạo records)
  - _Requirements: 1.1, 1.2, 1.5_

- [x] 4. Cập nhật ClassListViewModel





  - Inject ScheduleService vào ClassListViewModel
  - Gọi `scheduleService.generateScheduleForClass()` sau khi tạo lớp thành công trong hàm `createClass()`
  - Xử lý exceptions và cập nhật UI state nếu có lỗi
  - _Requirements: 1.1_

- [x] 5. Cập nhật ClassEditViewModel





  - Inject ScheduleService vào ClassEditViewModel
  - Gọi `scheduleService.regenerateScheduleForClass()` sau khi cập nhật lớp thành công trong hàm `updateClass()`
  - Chỉ regenerate nếu lịch học thay đổi (scheduleDaysOfWeek, repeatInterval, hoặc repeatUnit)
  - _Requirements: 1.2_

- [x] 6. Thêm icon cho ứng dụng





  - Copy file `student-management-01.png` vào thư mục tạm
  - Sử dụng Android Image Asset Studio hoặc script để tạo các kích thước icon (mdpi, hdpi, xhdpi, xxhdpi, xxxhdpi)
  - Copy các file icon vào `app/src/main/res/mipmap-*` folders
  - Cập nhật `AndroidManifest.xml` để sử dụng icon mới (android:icon="@mipmap/ic_launcher")
  - _Requirements: 2.1, 2.2, 2.3, 2.4_

- [x] 7. Viết unit tests





  - Test ScheduleCalculator với các trường hợp: WEEK, MONTH, YEAR repeat
  - Test ScheduleCalculator với scheduleDaysOfWeek khác nhau
  - Test ScheduleService.generateScheduleForClass với lớp có/không có học sinh
  - _Requirements: 1.1, 1.3, 1.5_
