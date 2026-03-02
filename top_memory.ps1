Get-Process | Sort-Object WorkingSet -Descending | Select-Object -First 15 Name, @{N='RAM_MB';E={[math]::Round($_.WorkingSet/1MB,0)}} | Format-Table -AutoSize
