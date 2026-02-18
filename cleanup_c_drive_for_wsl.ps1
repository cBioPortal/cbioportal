# Cleanup script to free C: drive space for WSL installation
# This script will free up approximately 500MB

$ErrorActionPreference = "Stop"

Write-Host "=== C: Drive Cleanup for WSL Installation ===" -ForegroundColor Cyan
Write-Host ""

# Get current free space
$cDrive = Get-Volume -DriveLetter C
$initialFree = [math]::Round($cDrive.SizeRemaining/1GB, 2)
Write-Host "Current C: drive free space: $initialFree GB" -ForegroundColor Yellow
Write-Host ""

# 1. Move screen recordings to D:
Write-Host "[1/3] Moving screen recordings to D: drive..." -ForegroundColor Green
$screenRecDir = "C:\Users\PURVANSH JOSHI\Videos\Screen Recordings"
$targetDir = "D:\Backups\Screen Recordings"

if (Test-Path $screenRecDir) {
    if (!(Test-Path $targetDir)) { 
        New-Item -ItemType Directory -Path $targetDir -Force | Out-Null 
    }
    
    $files = Get-ChildItem $screenRecDir -File
    foreach ($file in $files) {
        Write-Host "  Moving: $($file.Name) ($([math]::Round($file.Length/1MB, 2)) MB)"
        Move-Item -Path $file.FullName -Destination $targetDir -Force
    }
    Write-Host "  ✓ Moved $($files.Count) files" -ForegroundColor Green
} else {
    Write-Host "  Screen recordings folder not found, skipping" -ForegroundColor Gray
}

# 2. Clear Hugging Face cache
Write-Host "[2/3] Clearing Hugging Face cache..." -ForegroundColor Green
$hfCache = "C:\Users\PURVANSH JOSHI\.cache\huggingface"

if (Test-Path $hfCache) {
    $size = (Get-ChildItem $hfCache -Recurse -File -ErrorAction SilentlyContinue | Measure-Object -Property Length -Sum).Sum
    $sizeMB = [math]::Round($size/1MB, 2)
    Write-Host "  Removing $sizeMB MB of cached models..."
    Remove-Item $hfCache -Recurse -Force -ErrorAction SilentlyContinue
    Write-Host "  ✓ Cache cleared" -ForegroundColor Green
} else {
    Write-Host "  Hugging Face cache not found, skipping" -ForegroundColor Gray
}

# 3. Clear temp files
Write-Host "[3/3] Clearing temporary files..." -ForegroundColor Green
$tempDir = $env:TEMP

if (Test-Path $tempDir) {
    $beforeSize = (Get-ChildItem $tempDir -Recurse -File -ErrorAction SilentlyContinue | Measure-Object -Property Length -Sum).Sum
    $beforeMB = [math]::Round($beforeSize/1MB, 2)
    
    Get-ChildItem $tempDir -Recurse -ErrorAction SilentlyContinue | Remove-Item -Recurse -Force -ErrorAction SilentlyContinue
    
    Write-Host "  ✓ Cleared ~$beforeMB MB of temp files" -ForegroundColor Green
}

# Final summary
Write-Host ""
Write-Host "=== Cleanup Complete ===" -ForegroundColor Cyan
$cDrive = Get-Volume -DriveLetter C
$finalFree = [math]::Round($cDrive.SizeRemaining/1GB, 2)
$freed = [math]::Round($finalFree - $initialFree, 2)

Write-Host "Initial free space: $initialFree GB" -ForegroundColor White
Write-Host "Final free space:   $finalFree GB" -ForegroundColor Green
Write-Host "Space freed:        $freed GB" -ForegroundColor Cyan
Write-Host ""

if ($finalFree -gt 0.5) {
    Write-Host "✓ SUCCESS: Enough space available for WSL installation!" -ForegroundColor Green
    Write-Host "Next step: Run 'wsl --install --no-distribution' as Administrator" -ForegroundColor Yellow
} else {
    Write-Host "⚠ WARNING: May still need more space. Current: $finalFree GB" -ForegroundColor Yellow
    Write-Host "Recommended: At least 0.5GB free for safe WSL installation" -ForegroundColor Yellow
}
