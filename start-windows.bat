@echo off
chcp 65001 >nul
set "BASE_DIR=%~dp0"

echo Starting Backend...
cd /d "%BASE_DIR%backend\campus-zhihu"
start "Backend" cmd /c "mvnw.cmd spring-boot:run"

echo Starting Admin Panel...
cd /d "%BASE_DIR%admin"
start "Admin" cmd /c "pnpm run dev"

echo.
echo Backend  : http://localhost:8080
echo Admin    : http://localhost:5173
echo.
pause
