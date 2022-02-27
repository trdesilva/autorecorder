@echo off
echo Autorecorder Launcher
java -jar %LOCALAPPDATA%\autorecorder\updater.jar
for /f %%i in ('dir /b /s autorecorder*.jar') do set RESULT=%%i
echo Launching...
start /B "Autorecorder" "javaw" "-jar" "%RESULT%" >> log.txt
