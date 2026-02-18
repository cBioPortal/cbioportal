# PowerShell script to install Docker Desktop to D: drive
$ErrorActionPreference = "Stop"

$installerPath = "D:\cbioportal\dev_tools\Docker Desktop Installer.exe"
$installDir = "D:\Program Files\Docker\Docker"
$dataDir = "D:\docker-data"

Write-Host "Installing Docker Desktop to D: drive..."
Write-Host "Installer: $installerPath"
Write-Host "Install Location: $installDir"
Write-Host "WSL Data Location: $dataDir"

# Create directories if they don't exist
if (!(Test-Path $installDir)) { New-Item -ItemType Directory -Path $installDir -Force | Out-Null }
if (!(Test-Path $dataDir)) { New-Item -ItemType Directory -Path $dataDir -Force | Out-Null }

Write-Host "Launching installer..."
# Removing --quiet flag so you can see the GUI progress if possible, or at least see errors.
# Also removed -Wait to see if that helps, but added Pause at end.

try {
    $process = Start-Process -FilePath $installerPath -ArgumentList "install --accept-license --installation-dir=`"$installDir`" --wsl-default-data-root=`"$dataDir`"" -PassThru -Wait
    
    if ($process.ExitCode -ne 0) {
        Write-Error "Installer failed with exit code $($process.ExitCode)."
    } else {
        Write-Host "Installation process finished successfully."
    }
} catch {
    Write-Error "Failed to start installer: $_"
}

Write-Host "Please check if Docker Desktop is installed."
Write-Host "If installed successfully, RESTART your computer now."
Pause
