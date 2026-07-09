param(
    [int]$Port = 8080,
    [switch]$KillPortOwner
)

$ErrorActionPreference = "Stop"

$listener = Get-NetTCPConnection -LocalPort $Port -State Listen -ErrorAction SilentlyContinue | Select-Object -First 1
if ($listener) {
    $proc = Get-Process -Id $listener.OwningProcess -ErrorAction SilentlyContinue
    $procName = if ($proc) { $proc.ProcessName } else { "unknown" }
    Write-Host "Port $Port is already in use by PID $($listener.OwningProcess) ($procName)."

    if (-not $KillPortOwner) {
        Write-Host "Use -KillPortOwner to stop it automatically, or free the port manually."
        exit 1
    }

    Stop-Process -Id $listener.OwningProcess -Force
    Start-Sleep -Seconds 1
}

mvn spring-boot:run "-Dspring-boot.run.arguments=--spring.profiles.active=dev"
