$os = Get-CimInstance Win32_OperatingSystem
$totalRAM = [math]::Round($os.TotalVisibleMemorySize / 1MB, 1)
$freeRAM = [math]::Round($os.FreePhysicalMemory / 1MB, 1)
$usedRAM = $totalRAM - $freeRAM
$pageFile = [math]::Round($os.TotalVirtualMemorySize / 1MB, 1)
$freeVirtual = [math]::Round($os.FreeVirtualMemory / 1MB, 1)

Write-Host "=== System Memory ==="
Write-Host "Total RAM:     $totalRAM GB"
Write-Host "Used RAM:      $usedRAM GB"
Write-Host "Free RAM:      $freeRAM GB"
Write-Host "Total Virtual: $pageFile GB"
Write-Host "Free Virtual:  $freeVirtual GB"
