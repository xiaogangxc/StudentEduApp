@echo off
chcp 65001 >nul
echo ==========================================
echo   学生教育系统 - APK打包脚本
echo ==========================================
echo.

REM 检查Java环境
java -version >nul 2>&1
if errorlevel 1 (
    echo [错误] 未检测到Java环境，请先安装JDK
    echo 下载地址: https://www.oracle.com/java/technologies/downloads/
    pause
    exit /b 1
)

echo [1/4] Java环境检测通过
echo.

REM 进入项目目录
set LAUNCHER_DIR=%~dp0EduLauncher
set POSTURE_DIR=%~dp0PostureManager
set OUTPUT_DIR=%~dp0output

REM 创建输出目录
if not exist "%OUTPUT_DIR%" mkdir "%OUTPUT_DIR%"

echo [2/4] 开始打包 EduLauncher...
cd /d "%LAUNCHER_DIR%"

REM 下载gradle wrapper jar（如果不存在）
if not exist "gradle\wrapper\gradle-wrapper.jar" (
    echo 正在下载Gradle Wrapper...
    powershell -Command "& {Invoke-WebRequest -Uri 'https://raw.githubusercontent.com/gradle/gradle/v8.2.0/gradle/wrapper/gradle-wrapper.jar' -OutFile 'gradle/wrapper/gradle-wrapper.jar'}"
    if errorlevel 1 (
        echo [警告] 自动下载失败，请手动下载 gradle-wrapper.jar 放到 gradle/wrapper/ 目录
        echo 下载地址: https://services.gradle.org/distributions/gradle-8.2-bin.zip
    )
)

REM 执行打包
call gradlew.bat assembleDebug
if errorlevel 1 (
    echo [错误] EduLauncher 打包失败
    pause
    exit /b 1
)

REM 复制APK
copy /Y "app\build\outputs\apk\debug\app-debug.apk" "%OUTPUT_DIR%\EduLauncher-debug.apk" >nul
echo [✓] EduLauncher 打包完成
echo.

echo [3/4] 开始打包 PostureManager...
cd /d "%POSTURE_DIR%"

REM 下载gradle wrapper jar（如果不存在）
if not exist "gradle\wrapper\gradle-wrapper.jar" (
    echo 正在下载Gradle Wrapper...
    powershell -Command "& {Invoke-WebRequest -Uri 'https://raw.githubusercontent.com/gradle/gradle/v8.2.0/gradle/wrapper/gradle-wrapper.jar' -OutFile 'gradle/wrapper/gradle-wrapper.jar'}"
)

REM 执行打包
call gradlew.bat assembleDebug
if errorlevel 1 (
    echo [错误] PostureManager 打包失败
    pause
    exit /b 1
)

REM 复制APK
copy /Y "app\build\outputs\apk\debug\app-debug.apk" "%OUTPUT_DIR%\PostureManager-debug.apk" >nul
echo [✓] PostureManager 打包完成
echo.

echo [4/4] 打包完成！
echo ==========================================
echo 输出文件:
echo   - %OUTPUT_DIR%\EduLauncher-debug.apk
echo   - %OUTPUT_DIR%\PostureManager-debug.apk
echo ==========================================
echo.
pause