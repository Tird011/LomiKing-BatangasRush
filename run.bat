@echo off
REM LomiKing: compile the Java core, run the logic tests, then play one simulated day.
cd /d "%~dp0"
where javac >/dev/null 2>/dev/null || (echo [!] javac not found. Install a JDK 11+ ^(e.g. Eclipse Temurin^) and reopen this window. & pause & exit /b 1)
if exist out rmdir /s /q out
dir /s /b core\src\*.java > sources.txt
echo Compiling...
javac --release 11 -encoding UTF-8 -d out @sources.txt || (echo [!] Compile failed. & del sources.txt & pause & exit /b 1)
del sources.txt
echo.
echo ===== Logic tests =====
java -cp out com.lomiking.core.LogicTests
echo.
echo ===== Simulated day (bot plays) =====
java -cp out com.lomiking.core.PlaythroughDemo
echo.
pause
