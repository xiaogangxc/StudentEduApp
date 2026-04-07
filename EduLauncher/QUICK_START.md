# 快速开始 - APK 构建

## 方法一：一键自动安装（推荐）

双击运行 `setup-and-build.bat`，脚本会自动完成：
1. 下载并安装 JDK 17
2. 下载并安装 Android Studio
3. 配置环境变量
4. 构建 APK

**注意**：整个过程需要下载约 1.2GB 文件，请确保网络畅通。

---

## 方法二：手动安装

### 步骤 1：安装 JDK
1. 访问 https://adoptium.net/
2. 下载 Windows x64 MSI 安装包
3. 双击安装，记住安装路径

### 步骤 2：安装 Android Studio
1. 访问 https://developer.android.com/studio
2. 下载 Windows 版本
3. 双击安装，按向导完成
4. 首次启动时选择 "Standard" 安装

### 步骤 3：配置环境变量
```
JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-17
ANDROID_SDK_ROOT=C:\Users\<你的用户名>\AppData\Local\Android\Sdk
```

### 步骤 4：构建 APK
```bash
cd EduLauncher
.\gradlew assembleDebug
```

---

## 方法三：使用 Android Studio（最简单）

1. 安装 Android Studio
2. File → Open → 选择 `EduLauncher` 文件夹
3. 等待 Gradle 同步完成（首次需要下载依赖）
4. Build → Build Bundle(s) / APK(s) → Build APK(s)
5. 完成后点击右下角 "locate" 找到 APK

---

## APK 输出位置

构建成功后，APK 文件位于：
```
EduLauncher/app/build/outputs/apk/debug/app-debug.apk
```

---

## 常见问题

### 1. 构建失败，提示 "SDK location not found"
在项目根目录创建 `local.properties` 文件：
```
sdk.dir=C\:\\Users\\<用户名>\\AppData\\Local\\Android\\Sdk
```

### 2. 首次构建很慢
首次构建需要下载 Gradle 和依赖库，请耐心等待（5-15分钟）。

### 3. 内存不足错误
在 `gradle.properties` 中添加：
```
org.gradle.jvmargs=-Xmx4g
```

---

## 安装 APK 到手机

1. 将 `app-debug.apk` 复制到手机
2. 在手机上打开文件管理器，点击 APK
3. 如提示"未知来源"，进入设置 → 安全 → 允许未知来源安装
4. 完成安装
