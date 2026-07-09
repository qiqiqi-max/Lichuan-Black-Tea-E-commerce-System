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
        $script:UploadDir = Join-Path $repoRoot "backend\uploads\product-images"
    } else {
        $script:UploadDir = (Resolve-Path $UploadDir).Path
    }

    if ([string]::IsNullOrWhiteSpace($FallbackImage)) {
        $script:FallbackImage = Join-Path $repoRoot "frontend\public\images\products\product1.jpg"
    } else {
        $script:FallbackImage = (Resolve-Path $FallbackImage).Path
    }
}

function Extract-ImageFileName([string]$coverImg) {
    if ([string]::IsNullOrWhiteSpace($coverImg)) {
        return $null
    }

    $trimmed = $coverImg.Trim()
    $apiPattern = '(?i)(?:https?://[^/]+)?/api/products/image/(?<name>[^/?#]+)$'
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

$url = "$($ApiBaseUrl.TrimEnd('/'))/api/products"
$response = Invoke-RestMethod -Uri $url -Method Get -TimeoutSec 15

$products = @()
if ($response -is [System.Collections.IEnumerable] -and -not ($response.PSObject.Properties.Name -contains "code")) {
    $products = @($response)
} elseif ($response.code -eq 200) {
    $products = @($response.data)
} else {
    throw "Fetch products failed: $($response | ConvertTo-Json -Depth 5)"
}

$missing = @()
foreach ($p in $products) {
    $fileName = Extract-ImageFileName $p.coverImg
    if (-not $fileName) {
        continue
    }

    $target = Join-Path $UploadDir $fileName
    if (-not (Test-Path $target)) {
        $missing += [PSCustomObject]@{
            ProductId = $p.id
            ProductName = $p.name
            CoverImg = $p.coverImg
            FileName = $fileName
            Target = $target
        }
    }
}

Write-Host "Products scanned: $($products.Count)"
Write-Host "Missing image files: $($missing.Count)"

if ($missing.Count -eq 0) {
    Write-Host "Nothing to repair."
    exit 0
}

$missing | Select-Object ProductId, ProductName, CoverImg, FileName | Format-Table -AutoSize

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
