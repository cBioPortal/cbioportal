# Script to clean up C: drive space
$ErrorActionPreference = "SilentlyContinue"

Write-Host "Cleaning up C: drive..."

# 1. Remove Failed Docker Installation
$dockerPath = "C:\Program Files\Docker"
if (Test-Path $dockerPath) {
    Write-Host "Removing $dockerPath (~2GB)..."
    Remove-Item -Path $dockerPath -Recurse -Force
}

# 2. Clean Temp Folder
$tempPath = "$env:LOCALAPPDATA\Temp"
Write-Host "Cleaning Temp folder $tempPath (~1GB)..."
Get-ChildItem -Path $tempPath -Recurse | Remove-Item -Recurse -Force

# 3. Check Space
$cDrive = Get-PSDrive C
Write-Host "Free Space on C: $([math]::Round($cDrive.Free / 1GB, 2)) GB"

Write-Host "Cleanup Complete."
Pause
