$javaProcs = Get-Process -Name "java" -ErrorAction SilentlyContinue
if ($javaProcs) {
    Write-Host "Killing $($javaProcs.Count) Java process(es)..."
    $javaProcs | Stop-Process -Force
    Write-Host "Done."
} else {
    Write-Host "No Java processes found."
}
Start-Sleep -Seconds 3

# Check free RAM now
$os = Get-CimInstance Win32_OperatingSystem
$freeRAM = [math]::Round($os.FreePhysicalMemory / 1MB, 1)
$freeVirtual = [math]::Round($os.FreeVirtualMemory / 1MB, 1)
Write-Host "Free RAM: $freeRAM GB"
Write-Host "Free Virtual: $freeVirtual GB"
