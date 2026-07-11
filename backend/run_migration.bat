@echo off
echo Running Alembic migration to add internal mark messages...
cd /d "%~dp0"
call venv\Scripts\activate.bat
alembic revision --autogenerate -m "Add internal mark messages"
alembic upgrade head
echo.
echo Migration completed! Please restart your backend server.
pause
