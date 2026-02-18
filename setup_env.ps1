$ErrorActionPreference = "Stop"
$toolsDir = "d:\cbioportal\dev_tools"

# URLs
$javaUrl = "https://github.com/adoptium/temurin21-binaries/releases/download/jdk-21.0.6%2B7/OpenJDK21U-jdk_x64_windows_hotspot_21.0.6_7.zip"
$mavenUrl = "https://dlcdn.apache.org/maven/maven-3/3.9.9/binaries/apache-maven-3.9.9-bin.zip"

# Download Java
Write-Host "Downloading Java 21..."
$javaZip = "$toolsDir\java.zip"
Invoke-WebRequest -Uri $javaUrl -OutFile $javaZip
Write-Host "Extracting Java..."
Expand-Archive -Path $javaZip -DestinationPath $toolsDir -Force
# Rename extraction folder to just 'jdk-21' for simplicity (find the directory first)
$jdkDir = Get-ChildItem -Path $toolsDir -Directory -Filter "jdk-21*" | Select-Object -First 1
if ($jdkDir) {
    Rename-Item -Path $jdkDir.FullName -NewName "java-21"
}

# Download Maven
Write-Host "Downloading Maven..."
$mavenZip = "$toolsDir\maven.zip"
Invoke-WebRequest -Uri $mavenUrl -OutFile $mavenZip
Write-Host "Extracting Maven..."
Expand-Archive -Path $mavenZip -DestinationPath $toolsDir -Force
$mvnDir = Get-ChildItem -Path $toolsDir -Directory -Filter "apache-maven*" | Select-Object -First 1
if ($mvnDir) {
    Rename-Item -Path $mvnDir.FullName -NewName "maven"
}

# Cleanup Zips
Remove-Item $javaZip
Remove-Item $mavenZip

# Set Environment Variables for the Session
$env:JAVA_HOME = "$toolsDir\java-21"
$env:M2_HOME = "$toolsDir\maven"
$env:PATH = "$env:JAVA_HOME\bin;$env:M2_HOME\bin;$env:PATH"

# Verify
java -version
mvn -version

Write-Host "Installation Complete. To use these tools in future sessions, you will need to set the environment variables permanently or run a setup script."
