@echo off
title CAMS Backend Server
echo ==========================================
echo   CAMS Backend Starting on port 8000...
echo ==========================================
echo.

REM Use the venv Python directly (most reliable on Windows)
if exist ".venv\Scripts\python.exe" (
    echo Using venv Python...
    .venv\Scripts\python.exe app.py
) else if exist "venv\Scripts\python.exe" (
    echo Using venv Python...
    venv\Scripts\python.exe app.py
) else (
    echo Using system Python launcher...
    py app.py
)

pause
