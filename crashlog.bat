@echo off
rem Installs the latest LomiKing APK on the connected phone/emulator, opens it, and saves the crash log.
cd /d "%~dp0"
set ADB=%LOCALAPPDATA%\Android\Sdk\platform-tools\adb.exe
echo Connected devices:
"%ADB%" devices
echo Installing...
"%ADB%" install -r "mobile\android\app\build\outputs\apk\release\app-release.apk" > install-result.txt 2>&1
type install-result.txt
"%ADB%" logcat -c
"%ADB%" logcat -b crash -c
echo Opening LomiKing...
"%ADB%" shell am start -W -n com.lomiking.batangasrush/.MainActivity
echo Waiting 15 seconds for it to crash...
timeout /t 15 /nobreak > nul
"%ADB%" logcat -d -v time > crash.txt
"%ADB%" logcat -d -b crash -v time > crash-buffer.txt
echo.
echo Done. Go back to Claude and type: done
pause
