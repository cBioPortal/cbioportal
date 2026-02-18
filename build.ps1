$env:JAVA_HOME = [Environment]::GetEnvironmentVariable('JAVA_HOME', 'User')
$env:M2_HOME = [Environment]::GetEnvironmentVariable('M2_HOME', 'User')
$gitBin = "C:\Program Files\Git\bin"
$env:PATH = "$env:JAVA_HOME\bin;$env:M2_HOME\bin;$gitBin;$env:PATH"

Write-Host "Running Maven with JAVA_HOME=$env:JAVA_HOME"
mvn clean install "-DskipTests" "-Dmaven.antrun.skip=true" "-Dgit.commit.id.skip=true" "-Dmaven.gitcommitid.skip=true"
