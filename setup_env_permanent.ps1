# Set Permanent Environment Variables for cBioPortal Development
$ErrorActionPreference = "Stop"

$toolsDir = "d:\cbioportal\dev_tools"
$javaHome = "$toolsDir\java-21"
$m2Home = "$toolsDir\maven"

# 1. Set JAVA_HOME
Write-Host "Setting JAVA_HOME to $javaHome..."
[System.Environment]::SetEnvironmentVariable("JAVA_HOME", $javaHome, [System.EnvironmentVariableTarget]::User)

# 2. Set M2_HOME
Write-Host "Setting M2_HOME to $m2Home..."
[System.Environment]::SetEnvironmentVariable("M2_HOME", $m2Home, [System.EnvironmentVariableTarget]::User)

# 3. Update PATH
Write-Host "Updating PATH..."
$currentPath = [System.Environment]::GetEnvironmentVariable("PATH", [System.EnvironmentVariableTarget]::User)
$newPathEntries = "$javaHome\bin;$m2Home\bin"

# Check if already in PATH to avoid duplicates
if ($currentPath -notlike "*$javaHome\bin*") {
    $currentPath = "$newPathEntries;$currentPath"
    [System.Environment]::SetEnvironmentVariable("PATH", $currentPath, [System.EnvironmentVariableTarget]::User)
    Write-Host "Path updated successfully."
} else {
    Write-Host "Path already contains Java/Maven binaries. Skipping update."
}

Write-Host "Environment variables set permanently for the User scope."
Write-Host "Please restart your terminal (or VS Code) for changes to take effect."
