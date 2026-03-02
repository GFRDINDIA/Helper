$sdkManager = "C:\Users\ASUS\AppData\Local\Android\Sdk\cmdline-tools\latest\bin\sdkmanager.bat"
$sdkRoot = "C:\Users\ASUS\AppData\Local\Android\Sdk"

Write-Host "Installing Android Emulator Hypervisor Driver..."
$result = & $sdkManager --sdk_root=$sdkRoot "extras;google;Android_Emulator_Hypervisor_Driver" 2>&1
Write-Host $result
Write-Host ""
Write-Host "=== Checking install path ==="
$driverPath = "$sdkRoot\extras\google\Android_Emulator_Hypervisor_Driver"
if (Test-Path $driverPath) {
    Get-ChildItem $driverPath
} else {
    Write-Host "Driver path not found: $driverPath"
}
