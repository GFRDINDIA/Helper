$sdkDir = "$env:LOCALAPPDATA\Android\Sdk"
$sdkManager = "$sdkDir\cmdline-tools\latest\bin\sdkmanager.bat"

Write-Host "=== Installing Android SDK packages ==="
Write-Host "SDK root: $sdkDir"

# Set env vars for this session
$env:ANDROID_HOME = $sdkDir
$env:ANDROID_SDK_ROOT = $sdkDir

# Accept all licenses by pre-creating license files
$licensesDir = "$sdkDir\licenses"
New-Item -ItemType Directory -Force -Path $licensesDir | Out-Null

# Standard Android SDK license hashes
Set-Content -Path "$licensesDir\android-sdk-license" -Value "`n24333f8a63b6825ea9c5514f83c2829b004d1fee`n8933bad161af4178b1185d1a37fbf41ea5269c55`nd56f5187479451eabf01fb78af6dfcb131a6481e`n8933bad161af4178b1185d1a37fbf41ea5269c55"
Set-Content -Path "$licensesDir\android-sdk-preview-license" -Value "`n84831b9409646a918e30573bab4c9c91346d8abd"
Set-Content -Path "$licensesDir\android-googletv-license" -Value "`n601085b94cd77f0b54ff86406957099ebe79c4d6"
Set-Content -Path "$licensesDir\google-gdk-license" -Value "`n33b6a2b64607f11b759f320ef9dff4ae5c47d97a"
Set-Content -Path "$licensesDir\intel-android-extra-license" -Value "`nd975f751698a77b662f1254ddbeed3901e976f5a"
Set-Content -Path "$licensesDir\mips-android-sysimage-license" -Value "`ne9acab5b5fbb560a72cfaecce8946896ff6aab9d"
Set-Content -Path "$licensesDir\android-sdk-arm-dbt-license" -Value "`n859f317696f67ef3d7f30a50a5560e7834b43903"

Write-Host "Licenses pre-accepted. Installing packages..."

# Install packages (warnings from sdkmanager are expected, not errors)
$packages = @(
    "platform-tools",
    "build-tools;35.0.0",
    "platforms;android-35",
    "system-images;android-35;google_apis;x86_64"
)

foreach ($pkg in $packages) {
    Write-Host "Installing: $pkg"
    $result = & $sdkManager --sdk_root=$sdkDir $pkg 2>&1
    Write-Host $result
}

Write-Host ""
Write-Host "=== SDK packages installed ==="
Write-Host "Installed components:"
& $sdkManager --sdk_root=$sdkDir --list_installed 2>&1 | Select-String -Pattern "^\s+[a-z]"
