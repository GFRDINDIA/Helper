$features = @('HypervisorPlatform', 'Microsoft-Hyper-V-All', 'VirtualMachinePlatform')
foreach ($f in $features) {
    $result = Get-WindowsOptionalFeature -Online -FeatureName $f -ErrorAction SilentlyContinue
    if ($result) {
        Write-Host "$f : $($result.State)"
    } else {
        Write-Host "$f : Not found"
    }
}
