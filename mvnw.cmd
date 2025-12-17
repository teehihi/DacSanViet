
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
@REM Maven Wrapper
@REM ----------------------------------------------------------------------------

@echo off
@if not "%OS%"=="Windows_NT" goto start

:start
@setlocal

set ERROR_CODE=0

@REM ----------------------------------------------------------------------------
@REM Find the Maven Wrapper jar
@REM ----------------------------------------------------------------------------

set BASE_DIR=%~dp0
set WRAPPER_JAR="%BASE_DIR%.mvn\wrapper\maven-wrapper.jar"
set WRAPPER_LAUNCHER=org.apache.maven.wrapper.MavenWrapperMain

if exist %WRAPPER_JAR% goto runWrapper

echo Error: Could not find or read maven-wrapper.jar in %WRAPPER_JAR%
set ERROR_CODE=1
goto end

:runWrapper
@REM ----------------------------------------------------------------------------
@REM Run the Maven wrapper
@REM ----------------------------------------------------------------------------

java %MAVEN_OPTS% -classpath %WRAPPER_JAR% "-Dmaven.multiModuleProjectDirectory=%BASE_DIR%" %WRAPPER_LAUNCHER% %*
if ERRORLEVEL 1 goto error
goto end

:error
set ERROR_CODE=1

:end
@endlocal & set ERROR_CODE=%ERROR_CODE%
if not "%OS%"=="Windows_NT" @endlocal
cmd /C exit /B %ERROR_CODE%
