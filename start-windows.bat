@echo off
chcp 65001 >nul
setlocal EnableDelayedExpansion

:: ===================================================================
::  校园知乎 问答社区 — Windows 一键启动脚本
::  功能：环境检查与自动安装 → 数据库初始化 → 端口清理 → 启动全部服务
::  注意：请以 管理员身份 运行本脚本（右键 → 以管理员身份运行）
:: ===================================================================

set "BASE_DIR=%~dp0"
set "LOG_DIR=%BASE_DIR%logs"
if not exist "%LOG_DIR%" mkdir "%LOG_DIR%"

echo.
echo ╔══════════════════════════════════════════════════════════╗
echo ║          校 园 知 乎  问 答 社 区                       ║
echo ║          一键环境检查 ^& 启动脚本 (Windows)              ║
echo ╚══════════════════════════════════════════════════════════╝
echo.

:: ============ 第一步：检查管理员权限 ============
net session >nul 2>&1
if %errorlevel% neq 0 (
    echo   ⚠️ 请以管理员身份运行本脚本！
    echo   操作：右键 start-windows.bat → 以管理员身份运行
    pause
    exit /b 1
)
echo   ✅ 管理员权限确认

:: ============ 第二步：检查并安装 Chocolatey ============
echo.
echo ━━━━━ 第 1 步：检查包管理器 Chocolatey ━━━━━
where choco >nul 2>&1
if %errorlevel% neq 0 (
    echo   ❌ Chocolatey 未安装，正在安装...
    powershell -NoProfile -ExecutionPolicy Bypass -Command "Set-ExecutionPolicy Bypass -Scope Process -Force; [System.Net.ServicePointManager]::SecurityProtocol = [System.Net.ServicePointManager]::SecurityProtocol -bor 3072; iex ((New-Object System.Net.WebClient).DownloadString('https://community.chocolatey.org/install.ps1'))"
    if !errorlevel! neq 0 (
        echo   ❌ Chocolatey 安装失败，请手动安装
        pause
        exit /b 1
    )
    :: 刷新环境变量
    call refreshenv >nul 2>&1
    echo   ✅ Chocolatey 安装成功
) else (
    echo   ✅ Chocolatey 已安装
)

:: ============ 第三步：检查并安装依赖 ============
echo.
echo ━━━━━ 第 2 步：检查并安装环境依赖 ━━━━━

:: Java 17
echo   🔍 检查 Java 17 ...
java -version 2>&1 | findstr /i "17" >nul
if %errorlevel% neq 0 (
    echo      ❌ Java 17 未安装，正在通过 Chocolatey 安装...
    choco install temurin17 -y
    call refreshenv >nul 2>&1
    echo      ✅ Java 17 安装完成
) else (
    echo      ✅ Java 17 已安装
)

:: Maven
echo   🔍 检查 Maven ...
where mvn >nul 2>&1
if %errorlevel% neq 0 (
    echo      ❌ Maven 未安装，正在通过 Chocolatey 安装...
    choco install maven -y
    call refreshenv >nul 2>&1
    echo      ✅ Maven 安装完成
) else (
    echo      ✅ Maven 已安装
)

:: Node.js
echo   🔍 检查 Node.js ...
where node >nul 2>&1
if %errorlevel% neq 0 (
    echo      ❌ Node.js 未安装，正在通过 Chocolatey 安装...
    choco install nodejs-lts -y
    call refreshenv >nul 2>&1
    echo      ✅ Node.js 安装完成
) else (
    echo      ✅ Node.js 已安装
)

:: pnpm
echo   🔍 检查 pnpm ...
where pnpm >nul 2>&1
if %errorlevel% neq 0 (
    echo      ❌ pnpm 未安装，正在通过 Chocolatey 安装...
    choco install pnpm -y
    call refreshenv >nul 2>&1
    echo      ✅ pnpm 安装完成
) else (
    echo      ✅ pnpm 已安装
)

:: MySQL
echo   🔍 检查 MySQL ...
where mysql >nul 2>&1
if %errorlevel% neq 0 (
    echo      ❌ MySQL 未安装，正在通过 Chocolatey 安装...
    choco install mysql -y
    call refreshenv >nul 2>&1
    echo      ✅ MySQL 安装完成
) else (
    echo      ✅ MySQL 已安装
)

:: ============ 第四步：数据库初始化 ============
echo.
echo ━━━━━ 第 3 步：检查并初始化数据库 ━━━━━

set "DB_USER=root"
set "DB_PASS=ab123168"
set "DB_NAME=campus_zhihu"
set "INIT_SQL=%BASE_DIR%backend\sql\init.sql"
set "DATA_SQL=%BASE_DIR%backend\sql\data.sql"

:: 检查数据库是否存在
mysql -u%DB_USER% -p%DB_PASS% -e "USE %DB_NAME%;" >nul 2>&1
if %errorlevel% neq 0 (
    echo   📦 数据库 %DB_NAME% 不存在，正在创建并导入数据...
    if exist "%INIT_SQL%" (
        mysql -u%DB_USER% -p%DB_PASS% < "%INIT_SQL%" 2>nul
        if !errorlevel! equ 0 (
            echo      ✅ 表结构已创建 ^(init.sql^)
        ) else (
            echo      ❌ init.sql 导入失败，请检查 MySQL 是否启动
            echo      提示: 用户名=%DB_USER% 密码=%DB_PASS%
            pause
            exit /b 1
        )
    )
    if exist "%DATA_SQL%" (
        mysql -u%DB_USER% -p%DB_PASS% %DB_NAME% < "%DATA_SQL%" 2>nul
        if !errorlevel! equ 0 (
            echo      ✅ 测试数据已导入 ^(data.sql^)
        ) else (
            echo      ⚠️ data.sql 导入失败（可忽略）
        )
    )
) else (
    echo   ✅ 数据库 %DB_NAME% 已存在，跳过初始化
)

:: ============ 第五步：清理端口 ============
echo.
echo ━━━━━ 第 4 步：检查并释放端口 ━━━━━

for %%P in (8080 5173) do (
    echo   🔍 检查端口 %%P ...
    for /f "tokens=5" %%a in ('netstat -ano ^| findstr :%%P ^| findstr LISTEN 2^>nul') do (
        echo      ⚡ 端口 %%P 被占用 ^(PID: %%a^)，正在释放...
        taskkill /F /PID %%a >nul 2>&1
        echo      ✅ 端口 %%P 已释放
    )
)

:: ============ 第六步：安装前端依赖 ============
echo.
echo ━━━━━ 第 5 步：检查前端依赖 ━━━━━

if not exist "%BASE_DIR%admin\node_modules" (
    echo   📦 管理后台：安装依赖...
    cd /d "%BASE_DIR%admin"
    call pnpm install
    echo   ✅ 管理后台依赖安装完成
) else (
    echo   ✅ 管理后台依赖已就绪
)

:: ============ 第七步：启动所有服务 ============
echo.
echo ━━━━━ 第 6 步：启动所有服务 ━━━━━

:: 启动后端
echo   🚀 启动后端 (Spring Boot)...
cd /d "%BASE_DIR%backend\campus-zhihu"
start "CampusZhihu-Backend" cmd /c "mvnw.cmd spring-boot:run 2>&1 | tee %LOG_DIR%\backend.log"
echo      PID 已分配 → 日志: logs\backend.log

:: 等待后端启动
echo   ⏳ 等待后端启动 (最多60秒)...
set /a waited=0
:wait_backend
timeout /t 2 /nobreak >nul
set /a waited+=2
curl -s http://localhost:8080/api/auth/current >nul 2>&1
if %errorlevel% equ 0 (
    echo   ✅ 后端已就绪 ^(%waited%秒^)
    goto backend_ready
)
if %waited% lss 60 goto wait_backend
echo   ⚠️ 后端启动超时，请检查 logs\backend.log
:backend_ready

:: 启动管理后台
echo   🚀 启动管理后台 (Vue3 + Element Plus)...
cd /d "%BASE_DIR%admin"
start "CampusZhihu-Admin" cmd /c "pnpm run dev 2>&1 | tee %LOG_DIR%\admin.log"
echo      PID 已分配 → 日志: logs\admin.log

timeout /t 3 /nobreak >nul

:: ============ 第八步：输出信息 ============
echo.
echo ╔══════════════════════════════════════════════════════════╗
echo ║            🎉 所有服务已启动！                          ║
echo ╠══════════════════════════════════════════════════════════╣
echo ║                                                          ║
echo ║  📌 服务地址:                                           ║
echo ║     后端 API     → http://localhost:8080                 ║
echo ║     API 文档     → http://localhost:8080/doc.html        ║
echo ║     管理后台     → http://localhost:5173                 ║
echo ║     Android客户端 → 请用 Android Studio 打开 android\    ║
echo ║                                                          ║
echo ╠══════════════════════════════════════════════════════════╣
echo ║                                                          ║
echo ║  📌 测试账号:                                           ║
echo ║  ┌──────────┬───────────────┬──────────┐               ║
echo ║  │  角色    │  用户名       │  密码    │               ║
echo ║  ├──────────┼───────────────┼──────────┤               ║
echo ║  │  管理员  │  admin        │ admin123 │               ║
echo ║  │  教师    │  teacher01    │ 123456   │               ║
echo ║  │  学生    │  student01    │ 123456   │               ║
echo ║  └──────────┴───────────────┴──────────┘               ║
echo ║                                                          ║
echo ╠══════════════════════════════════════════════════════════╣
echo ║                                                          ║
echo ║  📌 日志目录: logs\                                     ║
echo ║     backend.log  — 后端日志                              ║
echo ║     admin.log    — 管理后台日志                          ║
echo ║                                                          ║
echo ║  关闭本窗口或按 Ctrl+C 不会停止后台服务                 ║
echo ║  如需停止，请关闭对应的 CMD 窗口                        ║
echo ║                                                          ║
echo ╚══════════════════════════════════════════════════════════╝
echo.

:: 实时监控日志错误
echo 📋 实时监控日志 (仅显示错误)...
echo.
powershell -NoProfile -Command "Get-Content '%LOG_DIR%\backend.log','%LOG_DIR%\admin.log' -Wait -ErrorAction SilentlyContinue | Where-Object { $_ -match 'error|exception|fail|warn' } | ForEach-Object { Write-Host \"[$(Get-Date -Format 'HH:mm:ss')] $_\" -ForegroundColor Red }"

pause
