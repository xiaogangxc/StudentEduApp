# APK 构建指南

## 快速构建

### Windows
双击运行 `build-apk.bat`

### Linux/Mac
```bash
chmod +x build-apk.sh
./build-apk.sh
```

---

## 环境要求

### 1. 安装 JDK
- 下载地址：https://adoptium.net/
- 推荐版本：JDK 17 或更高
- 安装后配置环境变量 `JAVA_HOME`

### 2. 安装 Android SDK
**方式一：通过 Android Studio（推荐）**
1. 下载安装 Android Studio：https://developer.android.com/studio
2. 打开 Android Studio → SDK Manager
3. 安装 SDK Platforms：Android 12.0 (API 31) 或更高
4. 安装 SDK Tools：Build-Tools, Platform-Tools

**方式二：命令行安装**
1. 下载 command line tools：https://developer.android.com/studio#command-tools
2. 解压到 `C:\Users\<用户名>\AppData\Local\Android\Sdk\cmdline-tools`
3. 配置环境变量 `ANDROID_SDK_ROOT`

### 3. 配置环境变量

**Windows:**
```
JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-17
ANDROID_SDK_ROOT=C:\Users\<用户名>\AppData\Local\Android\Sdk
PATH=%JAVA_HOME%\bin;%ANDROID_SDK_ROOT%\platform-tools;%PATH%
```

**Linux/Mac:**
```bash
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk
export ANDROID_SDK_ROOT=$HOME/Android/Sdk
export PATH=$JAVA_HOME/bin:$ANDROID_SDK_ROOT/platform-tools:$PATH
```

---

## 构建输出

构建成功后，APK 文件位置：
```
app/build/outputs/apk/debug/app-debug.apk
```

---

## 常见问题

### 1. "java 不是内部或外部命令"
- 原因：Java 环境变量未配置
- 解决：安装 JDK 并配置 JAVA_HOME

### 2. "SDK location not found"
- 原因：Android SDK 路径未找到
- 解决：
  - 方式1：设置 ANDROID_SDK_ROOT 环境变量
  - 方式2：在项目根目录创建 `local.properties` 文件：
    ```
    sdk.dir=C\:\\Users\\<用户名>\\AppData\\Local\\Android\\Sdk
    ```

### 3. 构建失败，提示缺少依赖
```bash
# 尝试刷新依赖
./gradlew build --refresh-dependencies
```

### 4. 权限问题（Linux/Mac）
```bash
chmod +x gradlew
chmod +x build-apk.sh
```

---

## 手动构建命令

```bash
# 清理
./gradlew clean

# 构建 Debug 版本
./gradlew assembleDebug

# 构建 Release 版本（需要签名配置）
./gradlew assembleRelease

# 安装到连接的设备
./gradlew installDebug
```

---

## 使用 Android Studio 构建

1. 打开 Android Studio
2. File → Open → 选择 `EduLauncher` 文件夹
3. 等待 Gradle 同步完成
4. Build → Build Bundle(s) / APK(s) → Build APK(s)
5. 完成后右下角会显示 "Build Analyzer"，点击 "locate" 找到 APK
