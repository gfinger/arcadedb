@REM
@REM Copyright © 2021-present Arcade Data Ltd (info@arcadedata.com)
@REM
@REM Licensed under the Apache License, Version 2.0 (the "License");
@REM you may not use this file except in compliance with the License.
@REM You may obtain a copy of the License at
@REM
@REM     http://www.apache.org/licenses/LICENSE-2.0
@REM
@REM Unless required by applicable law or agreed to in writing, software
@REM distributed under the License is distributed on an "AS IS" BASIS,
@REM WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
@REM See the License for the specific language governing permissions and
@REM limitations under the License.
@REM

echo "ARCADEDB - PLAY WITH DATA - arcadedb.com"

rem Guess ARCADEDB_HOME if not defined
set CURRENT_DIR=%cd%

if exist "%JAVA_HOME:"=%\bin\java.exe" goto setJavaHome
set JAVA=java
goto okJava

:setJavaHome
set JAVA="%JAVA_HOME:"=%\bin\java"

:okJava
if not "%ARCADEDB_HOME%" == "" goto gotHome
set ARCADEDB_HOME=%CURRENT_DIR%
if exist "%ARCADEDB_HOME%\bin\server.bat" goto okHome
cd ..
set ARCADEDB_HOME=%cd%
cd %CURRENT_DIR%

:gotHome
if exist "%ARCADEDB_HOME%\bin\server.bat" goto okHome
echo The ARCADEDB_HOME environment variable is not defined correctly
echo This environment variable is needed to run this program
goto end

:okHome
rem Get remaining unshifted command line arguments and save them in the
set CMD_LINE_ARGS=

:setArgs
if ""%1""=="""" goto doneSetArgs
set CMD_LINE_ARGS=%CMD_LINE_ARGS% %1
shift
goto setArgs

:doneSetArgs

set JAVA_OPTS_SCRIPT=-XX:+HeapDumpOnOutOfMemoryError -Djava.awt.headless=true -Dfile.encoding=UTF8

rem ARCADEDB memory options, default uses the available RAM. To set it to a specific value, like 2GB of heap, use "-Xms2G -Xmx2G"
set ARCADEDB_OPTS_MEMORY=

set ARCADEDB_JMX=-Dcom.sun.management.jmxremote=true -Dcom.sun.management.jmxremote.local.only=false -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.port=9999 -Dcom.sun.management.jmxremote.rmi.port=9998


rem TO DEBUG ARCADEDB SERVER RUN IT WITH THESE OPTIONS:
rem -Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=1044
rem AND ATTACH TO THE CURRENT HOST, PORT 1044

call %JAVA% -server %JAVA_OPTS% %ARCADEDB_OPTS_MEMORY% %JAVA_OPTS_SCRIPT% %ARCADEDB_JMX% %ARCADEDB_SETTINGS% %CMD_LINE_ARGS% -cp "%ARCADEDB_HOME%\lib\*" com.arcadedb.server.ArcadeDBServer

:end
