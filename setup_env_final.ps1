$ErrorActionPreference = "Stop"
$toolsDir = "d:\cbioportal\dev_tools"
$mavenUrl = "https://dlcdn.apache.org/maven/maven-3/3.9.12/binaries/apache-maven-3.9.12-bin.zip"

# 1. Java Extraction (File already exists)
if (Test-Path "$toolsDir\java.zip") {
    Write-Host "Extracting Java..."
    Expand-Archive -Path "$toolsDir\java.zip" -DestinationPath $toolsDir -Force
    
    # Rename for simplicity
    $jdkDir = Get-ChildItem -Path $toolsDir -Directory -Filter "jdk-21*" | Select-Object -First 1
    if ($jdkDir) {
        if (Test-Path "$toolsDir\java-21") { Remove-Item "$toolsDir\java-21" -Recurse -Force }
        Rename-Item -Path $jdkDir.FullName -NewName "java-21"
    }
    Remove-Item "$toolsDir\java.zip"
} else {
    Write-Warning "Java zip not found at $toolsDir\java.zip! Skipping Java extraction."
}

# 2. Maven Download & Extraction
Write-Host "Downloading Maven 3.9.12..."
$mavenZip = "$toolsDir\maven.zip"
Invoke-WebRequest -Uri $mavenUrl -OutFile $mavenZip
Write-Host "Extracting Maven..."
Expand-Archive -Path $mavenZip -DestinationPath $toolsDir -Force

# Rename for simplicity
$mvnDir = Get-ChildItem -Path $toolsDir -Directory -Filter "apache-maven*" | Select-Object -First 1
if ($mvnDir) {
    if (Test-Path "$toolsDir\maven") { Remove-Item "$toolsDir\maven" -Recurse -Force }
    Rename-Item -Path $mvnDir.FullName -NewName "maven"
}
Remove-Item $mavenZip

# 3. Environment Variables (Session only - user needs to set permanently for future)
$env:JAVA_HOME = "$toolsDir\java-21"
$env:M2_HOME = "$toolsDir\maven"
$env:PATH = "$env:JAVA_HOME\bin;$env:M2_HOME\bin;$env:PATH"

# 4. Verification
Write-Host "Verifying Installation..."
java -version
mvn -version
