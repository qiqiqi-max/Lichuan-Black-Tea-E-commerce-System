param(
    [string]$ApiBaseUrl = "http://localhost:8080",
    [string]$UploadDir = "",
    [string]$FallbackImage = "",
    [switch]$Apply
)

$ErrorActionPreference = "Stop"

function Resolve-RepoRoot {
    $backendDir = Resolve-Path (Join-Path $PSScriptRoot "..")
    return Resolve-Path (Join-Path $backendDir "..")
}

function Resolve-Defaults {
    $repoRoot = Resolve-RepoRoot
    if ([string]::IsNullOrWhiteSpace($UploadDir)) {
        $script:UploadDir = Join-Path $repoRoot "backend\uploads\farmer-stories"
    } else {
        $script:UploadDir = (Resolve-Path $UploadDir).Path
    }

    if ([string]::IsNullOrWhiteSpace($FallbackImage)) {
        $script:FallbackImage = Join-Path $repoRoot "frontend\public\images\farmers\farmer1.1.jpg"
    } else {
        $script:FallbackImage = (Resolve-Path $FallbackImage).Path
    }
}

function Extract-ImageFileName([string]$imageUrl) {
    if ([string]::IsNullOrWhiteSpace($imageUrl)) {
        return $null
    }

    $trimmed = $imageUrl.Trim()
    $apiPattern = '(?i)(?:https?://[^/]+)?/api/farmer-stories/image/(?<name>[^/?#]+)$'
    if ($trimmed -match $apiPattern) {
        return $Matches["name"]
    }

    $fileNamePattern = '(?i)^[a-z0-9-]+\.(jpg|jpeg|png|webp)$'
    if ($trimmed -match $fileNamePattern) {
        return $trimmed
    }

    return $null
}

Resolve-Defaults

if (-not (Test-Path $FallbackImage)) {
    throw "Fallback image not found: $FallbackImage"
}

$url = "$($ApiBaseUrl.TrimEnd('/'))/api/farmer-stories/active"
$response = Invoke-RestMethod -Uri $url -Method Get -TimeoutSec 15

$stories = @()
if ($response -is [System.Collections.IEnumerable] -and -not ($response.PSObject.Properties.Name -contains "code")) {
    $stories = @($response)
} elseif ($response.code -eq 200) {
    $stories = @($response.data)
} else {
    throw "Fetch farmer stories failed: $($response | ConvertTo-Json -Depth 5)"
}

$missing = @()
foreach ($s in $stories) {
    $fileName = Extract-ImageFileName $s.imageUrl
    if (-not $fileName) {
        continue
    }

    $target = Join-Path $UploadDir $fileName
    if (-not (Test-Path $target)) {
        $missing += [PSCustomObject]@{
            StoryId = $s.id
            FarmerName = $s.farmerName
            ImageUrl = $s.imageUrl
            FileName = $fileName
            Target = $target
        }
    }
}

Write-Host "Farmer stories scanned: $($stories.Count)"
Write-Host "Missing image files: $($missing.Count)"

if ($missing.Count -eq 0) {
    Write-Host "Nothing to repair."
    exit 0
}

$missing | Select-Object StoryId, FarmerName, ImageUrl, FileName | Format-Table -AutoSize

if (-not $Apply) {
    Write-Host ""
    Write-Host "Dry-run only. Use -Apply to create placeholder files."
    exit 0
}

New-Item -ItemType Directory -Path $UploadDir -Force | Out-Null

$created = 0
foreach ($item in $missing) {
    $targetDir = Split-Path -Parent $item.Target
    New-Item -ItemType Directory -Path $targetDir -Force | Out-Null
    Copy-Item -Path $FallbackImage -Destination $item.Target -Force
    $created++
}

Write-Host ""
Write-Host "Repair done. Placeholder files created: $created"
Write-Host "Upload directory: $UploadDir"
