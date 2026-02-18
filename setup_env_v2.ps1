$ErrorActionPreference = "Stop"
$toolsDir = "d:\cbioportal\dev_tools"

# URLs
$javaUrl = "https://github.com/adoptium/temurin21-binaries/releases/download/jdk-21.0.6%2B7/OpenJDK21U-jdk_x64_windows_hotspot_21.0.6_7.zip"
$mavenUrl = "https://dlcdn.apache.org/maven/maven-3/3.9.9/binaries/apache-maven-3.9.9-bin.zip"

Import-Module BitsTransfer

# Cleanup old attempts
if (Test-Path "$toolsDir\java.zip") { Remove-Item "$toolsDir\java.zip" -Force }
if (Test-Path "$toolsDir\maven.zip") { Remove-Item "$toolsDir\maven.zip" -Force }

# Download Java
Write-Host "Downloading Java 21 (BITS)..."
Start-BitsTransfer -Source $javaUrl -Destination "$toolsDir\java.zip" -Priority Foreground
Write-Host "Extracting Java..."
Expand-Archive -Path "$toolsDir\java.zip" -DestinationPath $toolsDir -Force
$jdkDir = Get-ChildItem -Path $toolsDir -Directory -Filter "jdk-21*" | Select-Object -First 1
if ($jdkDir) {
    if (Test-Path "$toolsDir\java-21") { Remove-Item "$toolsDir\java-21" -Recurse -Force }
    Rename-Item -Path $jdkDir.FullName -NewName "java-21"
}

# Download Maven
Write-Host "Downloading Maven (BITS)..."
Start-BitsTransfer -Source $mavenUrl -Destination "$toolsDir\maven.zip" -Priority Foreground
Write-Host "Extracting Maven..."
Expand-Archive -Path "$toolsDir\maven.zip" -DestinationPath $toolsDir -Force
$mvnDir = Get-ChildItem -Path $toolsDir -Directory -Filter "apache-maven*" | Select-Object -First 1
if ($mvnDir) {
    if (Test-Path "$toolsDir\maven") { Remove-Item "$toolsDir\maven" -Recurse -Force }
    Rename-Item -Path $mvnDir.FullName -NewName "maven"
}

# Cleanup Zips
Remove-Item "$toolsDir\java.zip"
Remove-Item "$toolsDir\maven.zip"

Write-Host "Installation Complete."
