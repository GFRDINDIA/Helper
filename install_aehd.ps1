$sdkManager = "C:\Users\ASUS\AppData\Local\Android\Sdk\cmdline-tools\latest\bin\sdkmanager.bat"
$sdkRoot = "C:\Users\ASUS\AppData\Local\Android\Sdk"

Write-Host "Installing Android Emulator Hypervisor Driver for AMD processors..."
$result = & $sdkManager --sdk_root=$sdkRoot "extras;android;Android_Emulator_Hypervisor_Driver_for_AMD_Processors" 2>&1
Write-Host $result
Write-Host "Done."
