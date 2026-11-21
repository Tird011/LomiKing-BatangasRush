@echo off
REM Play on your phone with the Expo Go app (same Wi-Fi as this PC). No Android build needed.
cd /d "%~dp0"
start "LomiKing Java logic" cmd /k run-devserver.bat
cd mobile
if not exist node_modules call npm install
echo Scan the QR code with Expo Go. The app finds the Java logic on this PC automatically (port 8787).
call npx expo start
pause
