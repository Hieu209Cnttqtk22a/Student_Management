# Student Management App

Ứng dụng mobile quản lý học sinh cho Android, xây dựng bằng Kotlin + Jetpack Compose + Room.

## Cấu trúc Project

```
StudentManagementApp/
├── app/
│   ├── src/main/
│   │   ├── kotlin/com/studentmanagement/app/
│   │   │   ├── StudentManagementApp.kt          # Application class
│   │   │   ├── MainActivity.kt                  # Main activity
│   │   │   ├── data/
│   │   │   │   ├── entity/                      # Room entities
│   │   │   │   ├── dao/                         # Data Access Objects
│   │   │   │   ├── database/                    # Database setup
│   │   │   │   └── repository/                  # Repository pattern
│   │   │   ├── di/                              # Dependency injection (Hilt)
│   │   │   ├── ui/
│   │   │   │   ├── theme/                       # Material Design theme
│   │   │   │   ├── navigation/                  # Navigation setup
│   │   │   │   └── screen/                      # UI screens
│   │   │   │       ├── home/
│   │   │   │       ├── calendar/
│   │   │   │       └── settings/
│   │   │   └── util/                            # Utilities
│   │   └── res/                                 # Resources
│   └── build.gradle.kts
├── build.gradle.kts
├── settings.gradle.kts
└── gradle.properties
```

## Tech Stack

- **Kotlin** - Ngôn ngữ lập trình
- **Jetpack Compose** - UI framework
- **Room** - Local database (SQLite)
- **Hilt** - Dependency injection
- **Coroutines** - Async programming
- **DataStore** - Preferences storage
- **Navigation Compose** - Screen navigation
- **Coil** - Image loading
- **Timber** - Logging

## Tính năng chính

### 1. Quản lý lớp học
- Tạo, sửa, xoá lớp học
- Xem danh sách lớp
- Cấu hình lịch học cố định

### 2. Quản lý học sinh
- Thêm học sinh vào lớp
- Sửa thông tin học sinh
- Xoá học sinh

### 3. Ghi nhận buổi học
- Nhập điểm cho học sinh
- Chọn tag trạng thái (Nghỉ học, Điểm kém, Bù bài, v.v.)
- Chụp/tải ảnh minh chứng
- Ghi chú buổi học

### 4. Lịch sử học tập
- Xem lịch sử buổi học của từng học sinh
- Lọc theo khoảng thời gian
- Phân trang dữ liệu

### 5. Lịch học
- Hiển thị lịch tháng
- Highlight ngày có buổi học
- Chọn ngày để xem chi tiết

### 6. Cài đặt
- Dark/Light mode
- Nhắc hẹn trước giờ học
- Thông tin phiên bản

## Build APK

### Yêu cầu
- Android Studio Flamingo trở lên
- JDK 17+
- Android SDK 34+

### Các bước build

1. **Clone project**
   ```bash
   git clone <repository>
   cd StudentManagementApp
   ```

2. **Mở project trong Android Studio**
   - File → Open → Chọn thư mục project

3. **Build APK**
   - Build → Build Bundle(s) / APK(s) → Build APK(s)
   - Hoặc dùng command line:
   ```bash
   ./gradlew assembleRelease
   ```

4. **APK được tạo tại**
   ```
   app/build/outputs/apk/release/app-release.apk
   ```

5. **Cài đặt trên thiết bị**
   ```bash
   adb install app/build/outputs/apk/release/app-release.apk
   ```

## Cấu trúc Database

### Entities
- **ClassEntity** - Thông tin lớp học
- **StudentEntity** - Thông tin học sinh
- **DailyRecordEntity** - Bản ghi buổi học
- **TagEntity** - Tag trạng thái
- **DailyRecordTagCrossRef** - Liên kết many-to-many
- **AttachmentEntity** - Ảnh minh chứng

### Tags mặc định
- HAS_SCORE - Có điểm
- LOW_SCORE - Điểm kém
- STUDIED - Học
- MAKEUP - Bù bài
- ABSENT - Nghỉ học
- NO_HOMEWORK - Chưa làm bài tập
- FULL_HOMEWORK - Đầy đủ bài tập

## Phát triển

### Chạy app trên emulator
```bash
./gradlew installDebug
```

### Chạy tests
```bash
./gradlew test
```

### Lint check
```bash
./gradlew lint
```

## Hướng phát triển tương lai

- Đồng bộ dữ liệu lên cloud (Firebase)
- Export PDF/Excel
- Thống kê điểm và tỉ lệ
- Thông báo push
- Đăng nhập đa thiết bị

## License

MIT License
