@echo off
REM Runs the Java game logic on this PC so the app (web / Expo Go) can play against it.
cd /d "%~dp0"
where javac >nul 2>nul || (echo [!] javac not found. Install JDK 17+ ^(Eclipse Temurin^) and reopen this window. & pause & exit /b 1)
if exist out-dev rmdir /s /q out-dev
dir /s /b core\src\main\*.java devserver\src\*.java > sources-dev.txt
javac --release 11 -encoding UTF-8 -d out-dev @sources-dev.txt || (echo [!] Compile failed. & del sources-dev.txt & pause & exit /b 1)
del sources-dev.txt
echo.
echo Keep this window open while you play. If your phone cannot connect, allow Java through Windows Firewall.
java -cp out-dev com.lomiking.devserver.DevServer 8787
pause
