# Hướng dẫn xóa Database và Demo Data

## Vấn đề
App vẫn hiển thị các lớp demo (Lớp 1, Lớp 2, Lớp 3) từ database cũ.

## Giải pháp

### Cách 1: Xóa dữ liệu app (Khuyến nghị)
1. Vào **Settings** (Cài đặt) trên điện thoại
2. Chọn **Apps** (Ứng dụng)
3. Tìm và chọn **Student Management**
4. Chọn **Storage** (Bộ nhớ)
5. Nhấn **Clear Data** (Xóa dữ liệu)
6. Mở lại app

### Cách 2: Gỡ cài đặt và cài lại
1. Gỡ cài đặt app cũ hoàn toàn
2. Cài đặt file APK mới từ Desktop: `StudentManagement.apk`
3. Mở app - database sẽ rỗng

### Cách 3: Dùng ADB (Cho developer)
Chạy lệnh sau trong terminal:
```
adb shell pm clear com.studentmanagement.app
```

## Xác nhận
Sau khi xóa database, app sẽ:
- Không có lớp học nào
- Hiển thị màn hình trống
- Có thể tạo lớp học mới

## Lưu ý
- Database version đã được tăng lên 3
- Mọi dữ liệu cũ sẽ bị xóa khi cài đặt version mới
- Không thể khôi phục dữ liệu sau khi xóa
