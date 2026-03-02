$emulator = "C:\Users\ASUS\AppData\Local\Android\Sdk\emulator\emulator.exe"
$adb = "C:\Users\ASUS\AppData\Local\Android\Sdk\platform-tools\adb.exe"

Write-Host "Starting emulator in background..."
Start-Process -FilePath $emulator -ArgumentList "-avd", "Helper_AVD", "-no-snapshot-load", "-no-boot-anim" -WindowStyle Normal

Write-Host "Waiting for emulator to boot (up to 3 minutes)..."
$timeout = 180
$elapsed = 0
$booted = $false

while ($elapsed -lt $timeout) {
    Start-Sleep -Seconds 10
    $elapsed += 10

    $devices = & $adb devices 2>&1
    Write-Host "[$elapsed s] Devices: $devices"

    if ($devices -match "emulator-\d+\s+device") {
        $bootComplete = & $adb shell getprop sys.boot_completed 2>&1
        Write-Host "boot_completed = $bootComplete"
        if ($bootComplete -match "1") {
            $booted = $true
            break
        }
    }
}

if ($booted) {
    Write-Host "Emulator is ready!"
} else {
    Write-Host "Emulator did not fully boot in time."
}
