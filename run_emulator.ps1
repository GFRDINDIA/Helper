$emulator = "C:\Users\ASUS\AppData\Local\Android\Sdk\emulator\emulator.exe"
Write-Host "Starting emulator..."
$result = & $emulator -avd Helper_AVD -no-snapshot-load -verbose 2>&1
Write-Host $result
