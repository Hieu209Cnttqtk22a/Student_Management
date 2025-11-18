# Script xóa database và cài đặt lại app

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "XOA DATABASE VA CAI DAT LAI APP" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Kiểm tra ADB
Write-Host "[1/4] Kiem tra ket noi thiet bi..." -ForegroundColor Yellow
$devices = adb devices
if ($devices -match "device$") {
    Write-Host "Thiet bi da ket noi!" -ForegroundColor Green
} else {
    Write-Host "KHONG TIM THAY THIET BI!" -ForegroundColor Red
    Write-Host "Vui long ket noi thiet bi Android va bat USB Debugging" -ForegroundColor Red
    pause
    exit
}

Write-Host ""
Write-Host "[2/4] Xoa du lieu app cu..." -ForegroundColor Yellow
adb shell pm clear com.studentmanagement.app
if ($LASTEXITCODE -eq 0) {
    Write-Host "Da xoa du lieu app thanh cong!" -ForegroundColor Green
} else {
    Write-Host "Khong the xoa du lieu (co the app chua duoc cai dat)" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "[3/4] Cai dat APK moi..." -ForegroundColor Yellow
$apkPath = "app\build\outputs\apk\debug\app-debug.apk"
if (Test-Path $apkPath) {
    adb install -r $apkPath
    if ($LASTEXITCODE -eq 0) {
        Write-Host "Cai dat thanh cong!" -ForegroundColor Green
    } else {
        Write-Host "Cai dat that bai!" -ForegroundColor Red
    }
} else {
    Write-Host "Khong tim thay file APK!" -ForegroundColor Red
    Write-Host "Vui long build app truoc: .\gradlew.bat assembleDebug" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "[4/4] Copy APK ra Desktop..." -ForegroundColor Yellow
$desktopPath = "$env:USERPROFILE\Desktop\StudentManagement.apk"
Copy-Item $apkPath $desktopPath -Force
Write-Host "Da copy APK ra: $desktopPath" -ForegroundColor Green

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "HOAN THANH!" -ForegroundColor Green
Write-Host "- Database da duoc xoa" -ForegroundColor White
Write-Host "- App da duoc cai dat lai" -ForegroundColor White
Write-Host "- APK da duoc copy ra Desktop" -ForegroundColor White
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
pause
