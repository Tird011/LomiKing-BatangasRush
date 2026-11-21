@echo off
REM Play in your browser: Java logic on this PC + the React Native app as a web page.
cd /d "%~dp0"
start "LomiKing Java logic" cmd /k run-devserver.bat
cd mobile
if not exist node_modules call npm install
call npx expo start --web
pause
