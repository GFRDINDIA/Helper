$procs = Get-Process | Where-Object { $_.ProcessName -like '*emulator*' -or $_.ProcessName -like '*qemu*' }
if ($procs) {
    $procs | Select-Object Id, ProcessName, CPU
} else {
    Write-Host "No emulator process found"
}
