# Helper script to move Docker Desktop data to D: drive
# Run this AFTER installing Docker Desktop and ensuring it is running.

$ErrorActionPreference = "Stop"
$targetDir = "D:\docker-data"

Write-Host "This script will move your Docker WSL distribution to $targetDir."
Write-Host "Please ensure Docker Desktop is valid and you have run it at least once."
Pause

# 1. Shutdown WSL
Write-Host "Shutting down WSL..."
wsl --shutdown

# 2. Check if distros exist
$distroName = "docker-desktop-data"
$check = wsl --list --quiet
if ($check -notlike "*$distroName*") {
    Write-Error "Distribution '$distroName' not found! make sure Docker Desktop is installed and has run once."
    exit 1
}

# 3. Export
Write-Host "Exporting $distroName to $targetDir\${distroName}.tar (this may take time)..."
if (!(Test-Path $targetDir)) { New-Item -ItemType Directory -Path $targetDir -Force }
wsl --export $distroName "$targetDir\${distroName}.tar"

# 4. Unregister (Delete from C:)
Write-Host "Unregistering old distribution (freeing space on C:)..."
wsl --unregister $distroName

# 5. Import to D:
Write-Host "Importing distribution from D:..."
wsl --import $distroName "$targetDir\data" "$targetDir\${distroName}.tar" --version 2

# 6. Cleanup
Write-Host "Cleaning up tar file..."
Remove-Item "$targetDir\${distroName}.tar"

Write-Host "Success! Docker data is now on D:."
Write-Host "You can restart Docker Desktop now."
