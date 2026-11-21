@echo off
REM Builds and installs the real Android app with the Java logic INSIDE it (no PC server needed).
REM Needs Android Studio (Android SDK) + JDK 17, and a phone with USB debugging or an emulator.
cd /d "%~dp0mobile"
if not exist node_modules call npm install
call npx expo run:android
pause
