# Sudoku Game PowerShell Launcher
Write-Host "===================================================" -ForegroundColor Cyan
Write-Host "           Starting Sudoku Game Server...          " -ForegroundColor Cyan
Write-Host "===================================================" -ForegroundColor Cyan

$backendDir = Join-Path $PSScriptRoot "backend"
$jarPath = Join-Path $backendDir "target\sudoku-backend-1.0.0.jar"

if (-not (Test-Path $jarPath)) {
    Write-Host "Building backend with Maven..." -ForegroundColor Yellow
    $mvnPath = "$env:USERPROFILE\apache-maven-3.9.6\bin\mvn.cmd"
    if (Test-Path $mvnPath) {
        & $mvnPath clean package -DskipTests
    } else {
        mvn clean package -DskipTests
    }
}

Write-Host "Starting Java Server on port 8080..." -ForegroundColor Green
Start-Process -FilePath "java" -ArgumentList "-jar", $jarPath -WorkingDirectory $backendDir -WindowStyle Hidden

Start-Sleep -Seconds 2

Write-Host "Opening Sudoku in your browser..." -ForegroundColor Green
Start-Process "http://localhost:8080/"

Write-Host "Sudoku is running at http://localhost:8080" -ForegroundColor Cyan
