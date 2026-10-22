@echo off
title Sudoku Game Launcher
echo ===================================================
echo           Starting Sudoku Game Server...
echo ===================================================

cd /d "%~dp0backend"

if not exist "target\sudoku-backend-1.0.0.jar" (
    echo Building backend with Maven...
    if exist "%USERPROFILE%\apache-maven-3.9.6\bin\mvn.cmd" (
        call "%USERPROFILE%\apache-maven-3.9.6\bin\mvn.cmd" clean package -DskipTests
    ) else (
        call mvn clean package -DskipTests
    )
)

echo Starting Java Server on port 8080...
start "" javaw -jar target\sudoku-backend-1.0.0.jar

timeout /t 2 /nobreak > nul

echo Opening Sudoku in your browser...
start http://localhost:8080/

echo.
echo ===================================================
echo  Sudoku is running!
echo  Web UI: http://localhost:8080
echo ===================================================
timeout /t 5 > nul
