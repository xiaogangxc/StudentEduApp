@echo off
chcp 65001 >nul
echo ==========================================
echo   Android Studio + APK 自动安装构建脚本
echo ==========================================
echo.
echo 此脚本将自动完成以下任务：
echo   1. 下载并安装 JDK 17
echo   2. 下载并安装 Android Studio
echo   3. 配置环境变量
echo   4. 构建 APK
echo.
echo 按任意键开始安装...
pause >nul
cls

set "SETUP_DIR=%USERPROFILE%\Downloads\AndroidSetup"
set "PROJECT_DIR=%cd%"

REM 创建下载目录
if not exist "%SETUP_DIR%" mkdir "%SETUP_DIR%"

REM ==========================================
REM 步骤 1: 安装 JDK
REM ==========================================
echo [步骤 1/4] 检查和安装 JDK 17...

java -version >nul 2>&1
if %errorlevel% == 0 (
    echo [OK] JDK 已安装
    java -version 2>&1 | findstr "version"
    goto :jdk_done
)

echo [下载] 正在下载 JDK 17 (约 160MB)...
powershell -Command "& {Invoke-WebRequest -Uri 'https://aka.ms/download-jdk/microsoft-jdk-17-windows-x64.msi' -OutFile '%SETUP_DIR%\jdk-17.msi' -UseBasicParsing}"
if errorlevel 1 (
    echo [错误] JDK 下载失败
    pause
    exit /b 1
)

echo [安装] 正在安装 JDK 17...
msiexec /i "%SETUP_DIR%\jdk-17.msi" /quiet INSTALLDIR="C:\Program Files\Java\jdk-17" ADDLOCAL=FeatureMain
if errorlevel 1 (
    echo [错误] JDK 安装失败，请手动安装
    echo 下载地址: https://adoptium.net/
    pause
    exit /b 1
)

set "JAVA_HOME=C:\Program Files\Java\jdk-17"
set "PATH=%JAVA_HOME%\bin;%PATH%"
echo [OK] JDK 17 安装完成

:jdk_done
echo.

REM ==========================================
REM 步骤 2: 安装 Android Studio
REM ==========================================
echo [步骤 2/4] 检查和安装 Android Studio...

if exist "%LOCALAPPDATA%\Android\Sdk" (
    echo [OK] Android SDK 已存在
    goto :sdk_done
)

if exist "C:\Program Files\Android\Android Studio" (
    echo [OK] Android Studio 已安装
    goto :sdk_done
)

echo [下载] 正在下载 Android Studio (约 1GB)...
echo 下载时间较长，请耐心等待...
powershell -Command "& {Invoke-WebRequest -Uri 'https://redirector.gvt1.com/edgedl/android/studio/install/2024.1.1.12/android-studio-2024.1.1.12-windows.exe' -OutFile '%SETUP_DIR%\android-studio.exe' -UseBasicParsing}"
if errorlevel 1 (
    echo [错误] Android Studio 下载失败
    echo 请手动下载: https://developer.android.com/studio
    pause
    exit /b 1
)

echo [安装] 正在安装 Android Studio...
echo 安装向导即将启动，请按提示完成安装：
echo   - 点击 "Next" 继续
echo   - 选择安装路径（建议默认）
echo   - 点击 "Install" 开始安装
echo   - 安装完成后点击 "Finish"
echo.
"%SETUP_DIR%\android-studio.exe"
echo.
echo 请完成 Android Studio 安装后按任意键继续...
pause >nul

:sdk_done
echo.

REM ==========================================
REM 步骤 3: 配置环境变量
REM ==========================================
echo [步骤 3/4] 配置环境变量...

REM 设置 JAVA_HOME
if not defined JAVA_HOME (
    if exist "C:\Program Files\Java\jdk-17" (
        setx JAVA_HOME "C:\Program Files\Java\jdk-17" /M >nul 2>&1
        set "JAVA_HOME=C:\Program Files\Java\jdk-17"
    )
)

REM 设置 ANDROID_SDK_ROOT
if not defined ANDROID_SDK_ROOT (
    if exist "%LOCALAPPDATA%\Android\Sdk" (
        setx ANDROID_SDK_ROOT "%LOCALAPPDATA%\Android\Sdk" /M >nul 2>&1
        set "ANDROID_SDK_ROOT=%LOCALAPPDATA%\Android\Sdk"
    ) else if exist "C:\Users\%USERNAME%\AppData\Local\Android\Sdk" (
        setx ANDROID_SDK_ROOT "C:\Users\%USERNAME%\AppData\Local\Android\Sdk" /M >nul 2>&1
        set "ANDROID_SDK_ROOT=C:\Users\%USERNAME%\AppData\Local\Android\Sdk"
    )
)

REM 更新 PATH
if defined JAVA_HOME (
    set "PATH=%JAVA_HOME%\bin;%PATH%"
)
if defined ANDROID_SDK_ROOT (
    set "PATH=%ANDROID_SDK_ROOT%\platform-tools;%ANDROID_SDK_ROOT%\cmdline-tools\latest\bin;%PATH%"
)

echo [OK] 环境变量配置完成
echo   JAVA_HOME=%JAVA_HOME%
echo   ANDROID_SDK_ROOT=%ANDROID_SDK_ROOT%
echo.

REM ==========================================
REM 步骤 4: 构建 APK
REM ==========================================
echo [步骤 4/4] 开始构建 APK...
echo.

cd /d "%PROJECT_DIR%"

REM 检查 gradlew
if not exist "gradlew.bat" (
    echo [错误] 未找到 gradlew.bat，请确保在项目根目录运行此脚本
    pause
    exit /b 1
)

echo [清理] 清理旧构建...
call gradlew clean
if errorlevel 1 (
    echo [警告] 清理失败，继续构建...
)

echo.
echo [构建] 开始构建 Debug APK...
echo 首次构建需要下载依赖，可能需要 5-10 分钟...
call gradlew assembleDebug

if errorlevel 1 (
    echo.
    echo [错误] 构建失败！
    echo 可能的原因：
    echo   1. Android SDK 未正确安装
    echo   2. 网络问题导致依赖下载失败
    echo   3. 项目配置问题
    echo.
    echo 请尝试以下解决方案：
    echo   - 打开 Android Studio，导入项目，等待同步完成
    echo   - 检查网络连接，重新运行此脚本
    echo.
    pause
    exit /b 1
)

echo.
echo ==========================================
echo   构建成功！
echo ==========================================
echo.
echo APK 文件位置:
echo   %PROJECT_DIR%\app\build\outputs\apk\debug\app-debug.apk
echo.
echo 文件大小:
for %%I in ("%PROJECT_DIR%\app\build\outputs\apk\debug\app-debug.apk") do (
    set "size=%%~zI"
    set /a "mb=size/1024/1024"
    echo   %%~zI 字节 (约 !mb! MB)
)
echo.
echo 安装到手机：
echo   1. 将 APK 复制到手机
echo   2. 在手机上点击安装
echo   3. 如提示"未知来源"，请在设置中允许
echo.
echo 按任意键退出...
pause >nul
