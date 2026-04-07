@echo off
chcp 65001 >nul
echo ==========================================
echo   学生教育系统 APK 自动构建脚本
echo ==========================================
echo.

REM 检查Java环境
java -version >nul 2>&1
if errorlevel 1 (
    echo [错误] 未检测到Java环境，请先安装JDK
    echo 下载地址: https://adoptium.net/
    pause
    exit /b 1
)

echo [1/4] 检测到Java环境
echo.

REM 检查Android SDK
if "%ANDROID_SDK_ROOT%"=="" (
    if "%ANDROID_HOME%"=="" (
        echo [警告] 未检测到Android SDK环境变量
        echo 请设置 ANDROID_SDK_ROOT 或 ANDROID_HOME 环境变量
        echo.
        echo 临时设置SDK路径...
        set ANDROID_SDK_ROOT=C:\Users\%USERNAME%\AppData\Local\Android\Sdk
    )
)

echo [2/4] Android SDK路径: %ANDROID_SDK_ROOT%
echo.

REM 清理旧构建
echo [3/4] 清理旧构建文件...
call gradlew clean
if errorlevel 1 (
    echo [错误] 清理失败
    pause
    exit /b 1
)
echo 清理完成
echo.

REM 构建Debug APK
echo [4/4] 开始构建Debug APK...
call gradlew assembleDebug
if errorlevel 1 (
    echo [错误] 构建失败
    pause
    exit /b 1
)

echo.
echo ==========================================
echo   构建成功！
echo ==========================================
echo.
echo APK文件位置:
echo   app\build\outputs\apk\debug\app-debug.apk
echo.
echo 文件大小:
for %%I in ("app\build\outputs\apk\debug\app-debug.apk") do (
    echo   %%~zI 字节 (约 %%~zI / 1024 / 1024 MB)
)
echo.
echo 按任意键退出...
pause >nul
