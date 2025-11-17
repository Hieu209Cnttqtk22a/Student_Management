# Thiết kế App Mobile **Quản lý học sinh**  
_Kotlin + Jetpack Compose + Room_

> Tài liệu đặc tả chức năng và kiến trúc để triển khai app mobile quản lý học sinh cho Android.

---

## 1. Mục tiêu & phạm vi

### 1.1 Mục tiêu
- Quản lý lớp học và học sinh cho gia sư / giáo viên dạy thêm.
- Ghi nhận nhanh trong buổi học: điểm, tình trạng học, nghỉ/bù, bài tập, ghi chú, hình ảnh.
- Xem lại lịch sử học tập của từng học sinh theo ngày.
- Giao diện trực quan, thao tác một tay, ưu tiên tốc độ nhập dữ liệu.
- Hạn chế lỗi, dễ debug và mở rộng trong tương lai.

### 1.2 Phạm vi phiên bản v1
- Chỉ Android.
- Lưu trữ **offline** trên máy bằng Room (SQLite).
- Không có server/sync (có thể bổ sung sau).

---

## 2. Công nghệ & kiến trúc tổng thể

### 2.1 Tech stack chính

- **Ngôn ngữ:** Kotlin.
- **UI:** Jetpack Compose.
- **Navigation:** Android Jetpack Navigation (navigation-compose).
- **Lưu trữ local:** Room.
- **State & async:** ViewModel + Kotlin Coroutines + Flow.
- **DI (gợi ý):** Hilt hoặc Koin (tuỳ chọn, nhưng nên dùng Hilt).
- **Preference:** DataStore (dark/light mode, nhắc hẹn…).

### 2.2 Kiến trúc 3-layer

1. **UI Layer (Compose)**
   - Màn hình, component, theming.
   - Nhận state từ ViewModel, bắn event lên ViewModel.

2. **Domain Layer (tuỳ chọn nếu app lớn)**
   - Use case (interactor): tách logic khỏi ViewModel.
   - Ví dụ: `GetClassWithStudentsUseCase`, `SaveDailyRecordUseCase`…

3. **Data Layer**
   - Repository tổng hợp dữ liệu từ Room (về sau có thể thêm API).
   - DAO cho từng bảng: `ClassDao`, `StudentDao`, `DailyRecordDao`, `AttachmentDao`, `TagDao`…

---

## 3. Cấu trúc điều hướng (Navigation)

### 3.1 Bottom Navigation

App có **3 tab chính**:

1. **Home** (main_UI) – Danh sách lớp
2. **Lịch** (calendar_UI) – Lịch buổi học + filter theo ngày
3. **Cài đặt** (setting_UI)

### 3.2 Các màn hình chi tiết

- **Home / main_UI**
  - `ClassListScreen`
  - `ClassCreateScreen` (class_creator_UI)
  - `ClassEditScreen` (edit_class_UI)
  - `ClassDetailScreen` (detail_class_UI)
- **Student**
  - `StudentCreateScreen` (student_creator_UI)
  - `StudentEditScreen` (edit_student_UI)
  - `StudentDailyEditScreen` (màn hình nhập dữ liệu một ngày cho 1 học sinh, liên quan tới detail_class_UI)
  - `StudentDailyHistoryListScreen` (list_detail_student_UI)
  - `StudentDailyDetailScreen` (detail_student_UI)
- **Calendar**
  - `CalendarScreen`
- **Settings**
  - `SettingsScreen` (setting_UI)

Mỗi màn hình là một composable + một ViewModel riêng (hoặc dùng shared ViewModel cho nhóm màn hình có logic liên quan).

---

## 4. Tính năng chi tiết

### 4.1 Tag trạng thái buổi học (dùng trong detail_class_UI & daily record)

Các tag áp dụng trên **bản ghi hằng ngày của học sinh** (daily record):
- Có điểm
- Điểm kém
- Học … (ghi chú nội dung buổi học)
- Bù bài
- Nghỉ học (điểm là “-”)
- Chưa làm bài tập
- Đầy đủ bài tập

Thiết kế dưới dạng:
- Enum trong Kotlin (`DailyTag`) + bảng liên kết nhiều-nhiều trong database (DailyRecord – Tag).
- UI dùng chip button / toggle pill, cho phép chọn nhiều tag.

### 4.2 Tự động update dữ liệu và lịch

- Khi tạo lớp, người dùng có thể chọn **ngày giờ học cố định** (ví dụ: Thứ 2 – 4 – 6, 18:00–19:30).
- Tab Lịch dựa vào thông tin này để:
  - Tạo danh sách buổi học dự kiến theo ngày.
  - Highlight ngày có buổi học.
- Khi người dùng ghi dữ liệu cho 1 buổi (trong detail_class_UI), lịch sẽ lưu bản ghi thật cho ngày đó (DailySession + DailyRecord).

> **Lưu ý:** v1 có thể làm đơn giản: không tạo trước tất cả buổi học, mà chỉ:
> - Tính toán danh sách buổi học “dự kiến” trong một khoảng thời gian ngắn (ví dụ 1–2 tháng) để hiển thị trên tab Lịch.
> - Khi user vào class, chọn ngày/D buổi đó và bắt đầu nhập dữ liệu => lúc này mới tạo record thực tế.

---

## 5. Flow sử dụng chính

### 5.1 Flow 1: Tạo lớp học mới

1. Từ **main_UI**, người dùng ấn nút **“Tạo lớp học”**.
2. Chuyển sang `ClassCreateScreen`:
   - Nhập: Tên lớp, Môn học, Ghi chú, Lịch học (ngày trong tuần, giờ bắt đầu, thời lượng).
   - Nút **Lưu** → tạo Class trong DB.
3. Xong → Back về **main_UI**, danh sách lớp được refresh (auto hoặc ấn nút refresh).

### 5.2 Flow 2: Thêm học sinh vào lớp

1. Trong `ClassDetailScreen` → nút **“Thêm học sinh”**.
2. Chuyển sang `StudentCreateScreen`:
   - Họ tên, biệt danh (optional), số điện thoại phụ huynh, ngày vào học, ghi chú.
   - Nút **Lưu** → tạo Student gắn với Class.
3. Trả về `ClassDetailScreen`, danh sách học sinh hiển thị học sinh mới.

### 5.3 Flow 3: Ghi dữ liệu buổi học cho cả lớp (detail_class_UI)

1. Từ **main_UI**, chọn một lớp → mở `ClassDetailScreen`.
2. Mặc định hiển thị **ngày hôm nay** (có DatePicker để đổi ngày).
3. Danh sách học sinh theo lớp, mỗi dòng gồm:
   - Tên học sinh.
   - Icon/tóm tắt tag chính của ngày đó (nếu đã có bản ghi).
   - Điểm (nếu có).
   - Nút/bấm toàn dòng để mở `StudentDailyEditScreen` cho học sinh đó, ngày đó.

4. Tại `StudentDailyEditScreen`:
   - Hiển thị tên học sinh, ngày đang nhập.
   - Khung nhập **điểm** (optional, có thể để trống).
   - UI chọn **tag** (chip toggle) với các tag đã nêu.
   - Khung **thêm ảnh**:
     - Nút **Chụp ảnh** (Camera).
     - Nút **Chọn ảnh từ thư viện**.
     - Cho phép chọn nhiều ảnh, hiển thị list thumbnail, có thể xoá before save.
   - Nút **Lưu**:
     - Tạo/Update `DailyRecord` tương ứng (studentId + date).
     - Lưu liên kết tag + ảnh.

5. Nút **Back** từ `StudentDailyEditScreen` → quay về `ClassDetailScreen` (coi như cancel nếu chưa lưu).

### 5.4 Flow 4: Xem lịch sử theo học sinh (list_detail_student_UI)

1. Từ `ClassDetailScreen`, long-press hoặc nút riêng `"Xem lịch sử"` trên mỗi hàng học sinh.
2. Mở `StudentDailyHistoryListScreen`:
   - Hiển thị **list ngày có dữ liệu** cho học sinh đó.
   - Mỗi item: Ngày, tag chính, điểm, số ảnh.
   - Có filter:
     - Theo ngày (Date range, hoặc tháng).
     - Số bản ghi hiển thị trên 1 trang: 5, 10, 50, 100 (pagination đơn giản).
   - Footer: **Trang trước / Trang sau** nếu số lượng lớn.

3. Chọn một ngày → mở `StudentDailyDetailScreen`:

### 5.5 Flow 5: Xem chi tiết bản ghi 1 ngày của một học sinh (detail_student_UI)

`StudentDailyDetailScreen`:
- Hiển thị:
  - Tên học sinh.
  - Ngày cụ thể.
  - Các tag của ngày đó.
  - Điểm.
  - Danh sách ảnh (tap để xem full).
- Không cho edit trong màn hình này.
- Có nút **Back** về `StudentDailyHistoryListScreen`.

### 5.6 Flow 6: Tab Lịch

1. `CalendarScreen` hiển thị:
   - Lịch tháng.
   - Những ngày có lớp (theo lịch học cấu hình của từng lớp) highlight.
   - Những ngày đã có dữ liệu thực tế hiển thị thêm dot/indicator.

2. Khi ấn vào 1 ngày:
   - Show bottom sheet / list các lớp có buổi trong ngày đó.
   - Chọn lớp → mở `ClassDetailScreen` với ngày đã chọn.

### 5.7 Flow 7: Cài đặt (setting_UI)

`SettingsScreen`:
- Hiển thị:
  - Version app hiện tại (lấy từ BuildConfig).
  - Toggle **Dark / Light mode** (lưu khi chọn).
  - Toggle **Nhắc hẹn** (notification reminder trước giờ học X phút).

- Về sau có thể thêm:
  - Export/import dữ liệu.
  - Reset data.

---

## 6. Thiết kế UI (mô tả cấu trúc, không phải pixel-perfect)

### 6.1 main_UI – ClassListScreen

- **AppBar**: “Lớp học của tôi”
  - Icon refresh.
  - Menu nhỏ (nếu cần thêm tuỳ chọn sau).

- **Nội dung chính**:
  - Row trên cùng: nút **Tạo lớp học**, nút **Filter / Sort** (sheet chọn: Tên lớp, Ngày gần nhất đến ca học, Tổng số học sinh).
  - Danh sách lớp dạng card:
    - Tên lớp.
    - Môn học.
    - Tổng số học sinh.
    - Ngày buổi học gần nhất (dự kiến hoặc đã có dữ liệu).
    - Hai nút nhỏ:
      - **Edit** → `ClassEditScreen`.
      - **Chi tiết** → `ClassDetailScreen`.

- **Bottom bar**: 3 tab (Home, Lịch, Cài đặt).

### 6.2 ClassCreateScreen / ClassEditScreen

- Field:
  - Tên lớp (TextField).
  - Môn học.
  - Ghi chú.
  - Block Lịch học:
    - Multi-select ngày trong tuần (Mon–Sun).
    - Giờ bắt đầu (TimePicker).
    - Thời lượng hoặc giờ kết thúc.
- Nút **Lưu**, nút **Huỷ** (Back).

### 6.3 ClassDetailScreen (detail_class_UI)

- AppBar:
  - Tên lớp.
  - Icon “Chỉnh sửa lớp”.
  - Icon “Thêm học sinh”.

- Thanh chọn ngày:
  - Hôm nay, hôm qua, DatePicker icon (mở calendar để chọn ngày khác).

- List học sinh:
  - Mỗi row:
    - Tên học sinh.
    - Icon/summary tag: ví dụ chip “Nghỉ học”, “Điểm kém” nếu có.
    - Điểm (nếu đã nhập).
    - Tap row → `StudentDailyEditScreen` cho học sinh đó.

- Có thể thêm:
  - Nút “Tự động đánh dấu tất cả là Đi học / Đầy đủ bài tập” rồi chỉnh lại từng bạn.

### 6.4 StudentDailyEditScreen

- AppBar: “Nhập dữ liệu buổi học” + tên học sinh.
- Nội dung:
  - Ngày (text + icon để đổi nếu cần).
  - TextField **Điểm** (chấp nhận trống).
  - Tag selection: grid các chip/toggle, cho phép chọn nhiều.
  - Khu vực ảnh:
    - Thumbnails ảnh đã chọn.
    - Nút **+Camera**, **+Thư viện**.
  - TextField ghi chú (optional).
- Nút **Lưu** (primary), nút Back.

### 6.5 StudentDailyHistoryListScreen (list_detail_student_UI)

- AppBar: “Lịch sử – [Tên học sinh]”.
- Bộ lọc trên:
  - Chọn khoảng thời gian (From–To hoặc chọn tháng).
  - Dropdown chọn số dòng/trang: 5/10/50/100.

- Danh sách:
  - Mỗi item: Ngày, tag chính, điểm, số ảnh.
  - Tap → `StudentDailyDetailScreen`.

- Footer: nút “Trang trước” / “Trang sau”.

### 6.6 StudentDailyDetailScreen (detail_student_UI)

- AppBar: “Chi tiết buổi học”.
- Nội dung:
  - Tên học sinh.
  - Ngày.
  - Tag (hiển thị dưới dạng chip-readonly).
  - Điểm.
  - Ghi chú.
  - Grid ảnh (tap để mở full screen viewer).

### 6.7 SettingsScreen

- List item dạng Preference:
  - “Phiên bản: v1.0.0” (read-only).
  - Switch “Dark mode”.
  - Switch “Nhắc hẹn trước giờ học X phút” (có thể thêm Dialog chọn X).

---

## 7. Model dữ liệu & cấu trúc Room

### 7.1 Thực thể chính

1. **ClassEntity**
   - `id: Long` (PK)
   - `name: String`
   - `subject: String?`
   - `note: String?`
   - `createdAt: Long`
   - `scheduleDaysOfWeek: List<Int>` (serialize JSON hoặc convert type – 1..7)
   - `startTimeMinutes: Int?` (ví dụ 18:00 → 18*60)
   - `durationMinutes: Int?`

2. **StudentEntity**
   - `id: Long` (PK)
   - `classId: Long` (FK → ClassEntity)
   - `name: String`
   - `nickname: String?`
   - `parentPhone: String?`
   - `joinedAt: Long?`
   - `note: String?`

3. **DailyRecordEntity**
   - `id: Long` (PK)
   - `studentId: Long` (FK → StudentEntity)
   - `classId: Long` (FK → ClassEntity – redundant nhưng giúp query nhanh hơn)
   - `date: String` (format `yyyy-MM-dd` hoặc Long epochDate)
   - `score: Float?`
   - `note: String?`
   - `createdAt: Long`
   - `updatedAt: Long`

4. **TagEntity**
   - `id: Long` (PK)
   - `code: String` (ví dụ: `HAS_SCORE`, `LOW_SCORE`, `MAKEUP`, `ABSENT`, `NO_HOMEWORK`, `FULL_HOMEWORK`)
   - `displayName: String` (tiếng Việt hiển thị)

5. **DailyRecordTagCrossRef**
   - `dailyRecordId: Long`
   - `tagId: Long`

6. **AttachmentEntity**
   - `id: Long` (PK)
   - `dailyRecordId: Long` (FK)
   - `uri: String` (Uri string của file ảnh trong máy)
   - `createdAt: Long`

7. **AppSettingsEntity** (hoặc DataStore)
   - Dark mode: Boolean
   - Reminder enabled: Boolean
   - Reminder minutes before: Int?

### 7.2 Ví dụ Kotlin data class (Room, giản lược)

```kotlin
@Entity(tableName = "classes")
data class ClassEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val subject: String? = null,
    val note: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val scheduleDaysOfWeek: List<Int> = emptyList(),
    val startTimeMinutes: Int? = null,
    val durationMinutes: Int? = null,
)
```

```kotlin
@Entity(
    tableName = "students",
    foreignKeys = [
        ForeignKey(
            entity = ClassEntity::class,
            parentColumns = ["id"],
            childColumns = ["classId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("classId")]
)
data class StudentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val classId: Long,
    val name: String,
    val nickname: String? = null,
    val parentPhone: String? = null,
    val joinedAt: Long? = null,
    val note: String? = null
)
```

```kotlin
@Entity(
    tableName = "daily_records",
    foreignKeys = [
        ForeignKey(
            entity = StudentEntity::class,
            parentColumns = ["id"],
            childColumns = ["studentId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("studentId"), Index("classId"), Index("date")]
)
data class DailyRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val studentId: Long,
    val classId: Long,
    val date: String, // yyyy-MM-dd
    val score: Float? = null,
    val note: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
```

---

## 8. ViewModel & Repository (tóm tắt)

### 8.1 Repository

- `ClassRepository`
  - Lấy list lớp, tạo/sửa/xoá lớp.
  - Lấy lớp kèm số lượng học sinh.

- `StudentRepository`
  - Thêm/sửa/xoá học sinh.
  - Lấy list học sinh theo lớp.

- `DailyRecordRepository`
  - Lấy/ lưu `DailyRecord` theo (studentId, date).
  - Lấy lịch sử daily record theo studentId + filter ngày.
  - Quản lý tags và attachments.

### 8.2 ViewModel ví dụ

- `ClassListViewModel`
  - `state: StateFlow<ClassListState>` (list lớp, sort, loading, error).

- `ClassDetailViewModel`
  - Input: `classId`.
  - State gồm: thông tin lớp, ngày đang chọn, list học sinh + trạng thái ngày đó.

- `StudentDailyEditViewModel`
  - Input: `studentId`, `classId`, `date`.
  - Load record nếu có → hiển thị lên UI.
  - Hàm `onSave()` → validate + gọi repository lưu.

- `StudentHistoryViewModel`
  - Input: `studentId`.
  - State: list daily record, filter, paging.

---

## 9. Design System (Compose)

### 9.1 Màu sắc gợi ý

- Màu chủ đạo: xanh dương nhạt (tin cậy, giáo dục).
- Accent: cam hoặc xanh lá nhạt (thông báo, tag quan trọng).
- Tag:
  - Nghỉ học: đỏ nhạt.
  - Điểm kém: cam.
  - Đầy đủ bài tập: xanh lá.

Dùng `MaterialTheme`:
- `colorScheme`: light/dark.
- `Typography`: tiêu đề màn hình, tiêu đề card, text thường.

### 9.2 Component tái sử dụng

- `PrimaryButton`, `SecondaryButton`.
- `TagChip(selected: Boolean, label: String)`.
- `StudentRow()` cho list student ở ClassDetailScreen.
- `DailyRecordCard()` cho list lịch sử học sinh.

---

## 10. Logging & Debug

- Sử dụng `Timber` hoặc `Log.d` cho:
  - CRUD lớp/học sinh.
  - Lưu daily record.
  - Load lịch & filter.
- Thêm lớp `AppLogger` (interface) để sau dễ thay đổi (gửi log lên server nếu cần).
- Bọc các call Room trong try/catch, báo lỗi nhẹ nhàng trên UI (Snackbar).

---

## 11. Hướng phát triển sau này

- Đồng bộ dữ liệu lên cloud (Firebase/own backend).
- Đăng nhập đa thiết bị.
- Export PDF/Excel điểm và lịch sử.
- Thêm tính năng thống kê: điểm trung bình, tỉ lệ nghỉ học, biểu đồ.

---

## 12. Gợi ý bước triển khai

1. Tạo project Kotlin + Compose + Hilt + Room.
2. Implement model + DAO + Repository.
3. Làm tab **Home** hoàn chỉnh: tạo lớp, xem danh sách lớp.
4. Làm `ClassDetailScreen` + thêm/sửa học sinh.
5. Implement `StudentDailyEditScreen` và luồng lưu DailyRecord.
6. Bổ sung tab Lịch.
7. Cuối cùng thêm Settings, dark mode, reminder.

---

Tài liệu này đủ để bạn:
- Thiết kế UI chi tiết (Figma hoặc vẽ tay).
- Bắt đầu code cấu trúc project Kotlin + Compose.
- Mở rộng thêm tính năng mà không phải sửa lại kiến trúc lớn.
