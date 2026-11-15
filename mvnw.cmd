<# : batch script
@REM ----------------------------------------------------------------------------
@REM Licensed to the Apache Software Foundation (ASF) under one
@REM or more contributor license agreements.  See the NOTICE file
@REM distributed with this work for additional information
@REM regarding copyright ownership.  The ASF licenses this file
@REM to you under the Apache License, Version 2.0 (the
@REM "License"); you may not use this file except in compliance
@REM with the License.  You may obtain a copy of the License at
@REM
@REM    https://www.apache.org/licenses/LICENSE-2.0
@REM
@REM Unless required by applicable law or agreed to in writing,
@REM software distributed under the License is distributed on an
@REM "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
@REM KIND, either express or implied.  See the License for the
@REM specific language governing permissions and limitations
@REM under the License.
@REM ----------------------------------------------------------------------------

@REM ----------------------------------------------------------------------------
@REM Apache Maven Wrapper Startup Batch script, version 3.3.2
@REM ----------------------------------------------------------------------------

@echo off
setlocal

set "DIR=%~dp0"
set "WRAPPER_PROP=%DIR%.mvn\wrapper\maven-wrapper.properties"

if not exist "%WRAPPER_PROP%" (
  echo Error: Cannot find %WRAPPER_PROP%
  exit /b 1
)

for /f "usebackq tokens=1,* delims==" %%A in ("%WRAPPER_PROP%") do (
  if "%%A"=="distributionUrl" set "DIST_URL=%%B"
)

if "%DIST_URL%"=="" (
  echo Error: distributionUrl not found in %WRAPPER_PROP%
  exit /b 1
)

set "MAVEN_USER_HOME=%USERPROFILE%\.m2"
set "WRAPPER_DIR=%MAVEN_USER_HOME%\wrapper\dists"

powershell -NoProfile -ExecutionPolicy Bypass -Command "& {" ^
  "$distUrl = '%DIST_URL%';" ^
  "$distName = Split-Path $distUrl -Leaf;" ^
  "$sha1 = [System.Security.Cryptography.SHA1]::Create();" ^
  "$hashBytes = $sha1.ComputeHash([System.Text.Encoding]::UTF8.GetBytes($distUrl));" ^
  "$hash = ($hashBytes | ForEach-Object { $_.ToString('x2') }) -join '';" ^
  "$targetDir = Join-Path '%WRAPPER_DIR%' $hash;" ^
  "$unpackedDir = Join-Path $targetDir ($distName -replace '\.zip$', '');" ^
  "if (!(Test-Path $unpackedDir)) {" ^
    "New-Item -ItemType Directory -Force -Path $targetDir | Out-Null;" ^
    "$zipPath = Join-Path $targetDir $distName;" ^
    "Write-Host 'Downloading Maven from' $distUrl '...';" ^
    "[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12;" ^
    "(New-Object System.Net.WebClient).DownloadFile($distUrl, $zipPath);" ^
    "Write-Host 'Unpacking Maven to' $targetDir '...';" ^
    "Expand-Archive -Path $zipPath -DestinationPath $targetDir -Force;" ^
    "Remove-Item -Path $zipPath -Force;" ^
  "}" ^
  "$mvnCmd = Join-Path $unpackedDir 'bin\mvn.cmd';" ^
  "& $mvnCmd $args" ^
"} %*"

exit /b %ERRORLEVEL%
