$srcBase = "C:\flutter\packages\flutter_tools\templates\app_shared\android.tmpl\app\src\main\res"
$dstBase = "C:\Users\ASUS\Documents\Work\GFRND\Dev\Helper\helper-flutter-app\android\app\src\main\res"

$densities = @('mipmap-hdpi', 'mipmap-mdpi', 'mipmap-xhdpi', 'mipmap-xxhdpi', 'mipmap-xxxhdpi')

foreach ($density in $densities) {
    $srcDir = "$srcBase\$density"
    $dstDir = "$dstBase\$density"

    if (-not (Test-Path $dstDir)) {
        New-Item -ItemType Directory -Path $dstDir -Force | Out-Null
    }

    Copy-Item "$srcDir\ic_launcher.png" "$dstDir\ic_launcher.png" -Force
    Write-Host "Copied $density\ic_launcher.png"
}

Write-Host "All icons copied!"
