#!/bin/bash

echo "=========================================="
echo "  学生教育系统 APK 自动构建脚本"
echo "=========================================="
echo ""

# 检查Java环境
if ! command -v java &> /dev/null; then
    echo "[错误] 未检测到Java环境，请先安装JDK"
    echo "下载地址: https://adoptium.net/"
    exit 1
fi

echo "[1/4] 检测到Java环境:"
java -version
echo ""

# 检查Android SDK
if [ -z "$ANDROID_SDK_ROOT" ] && [ -z "$ANDROID_HOME" ]; then
    echo "[警告] 未检测到Android SDK环境变量"
    echo "请设置 ANDROID_SDK_ROOT 或 ANDROID_HOME 环境变量"
    echo ""
fi

echo "[2/4] Android SDK检查完成"
echo ""

# 清理旧构建
echo "[3/4] 清理旧构建文件..."
./gradlew clean
if [ $? -ne 0 ]; then
    echo "[错误] 清理失败"
    exit 1
fi
echo "清理完成"
echo ""

# 构建Debug APK
echo "[4/4] 开始构建Debug APK..."
./gradlew assembleDebug
if [ $? -ne 0 ]; then
    echo "[错误] 构建失败"
    exit 1
fi

echo ""
echo "=========================================="
echo "  构建成功！"
echo "=========================================="
echo ""
echo "APK文件位置:"
echo "  app/build/outputs/apk/debug/app-debug.apk"
echo ""
echo "文件大小:"
ls -lh app/build/outputs/apk/debug/app-debug.apk | awk '{print "  " $5}'
echo ""
