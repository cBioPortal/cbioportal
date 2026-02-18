$env:JAVA_HOME = [Environment]::GetEnvironmentVariable('JAVA_HOME', 'User')
$env:M2_HOME = [Environment]::GetEnvironmentVariable('M2_HOME', 'User')
$env:PATH = "$env:JAVA_HOME\bin;$env:M2_HOME\bin;$env:PATH"
$env:MAVEN_OPTS = "-Xmx1536m -XX:+UseSerialGC"
mvn test -Dtest=StudyViewControllerTest
