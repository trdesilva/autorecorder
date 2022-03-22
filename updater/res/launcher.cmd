@echo off
echo Autorecorder Launcher
java -jar %LOCALAPPDATA%\autorecorder\updater.jar
for /f %%i in ('dir /b /s autorecorder*.jar') do set RESULT=%%i
echo Launching...
for /f "delims= " %%a in ('"wmic path win32_useraccount where name='%UserName%' get sid"') do (
   if not "%%a"=="SID" (
      set SID=%%a
      goto :loop_end
   )
)

:loop_end
icacls %LOCALAPPDATA%\autorecorder /grant *"%SID%":F
start /B "Autorecorder" "javaw" "-jar" "%RESULT%" >> log.txt