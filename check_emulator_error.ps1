# Try running emulator and capture output
$emulator = "C:\Users\ASUS\AppData\Local\Android\Sdk\emulator\emulator.exe"
Write-Host "Running emulator check..."

# Run with accel check
$result = & $emulator -accel-check 2>&1
Write-Host "=== Accel Check ==="
Write-Host $result

# Check if HAXM or WHPX available
$result2 = & $emulator -avd Helper_AVD -no-window -no-audio -quit-after-boot 5 2>&1
Write-Host "=== Boot test (5s) ==="
Write-Host $result2
