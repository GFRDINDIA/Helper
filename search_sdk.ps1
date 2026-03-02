$sdkManager = "C:\Users\ASUS\AppData\Local\Android\Sdk\cmdline-tools\latest\bin\sdkmanager.bat"
$sdkRoot = "C:\Users\ASUS\AppData\Local\Android\Sdk"

Write-Host "Searching for hypervisor/HAXM packages..."
$list = & $sdkManager --sdk_root=$sdkRoot --list 2>&1
$list | Where-Object { $_ -match 'haxm|hypervisor|HAXM|Hypervisor|aehd|AEHD|AMD|accel' }
