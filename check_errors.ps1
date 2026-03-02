cd 'C:\Users\ASUS\Documents\Work\GFRND\Dev\Helper\helper-flutter-app'
$output = C:\flutter\bin\flutter.bat analyze 2>&1
$errors = $output | Where-Object { $_ -match '^\s+error' }
if ($errors) {
    Write-Host "=== ERRORS FOUND ==="
    $errors
} else {
    Write-Host "No errors found - only warnings/infos"
}
