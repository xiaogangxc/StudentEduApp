# 在另一台电脑上构建 APK

由于当前环境限制，建议在配置好的电脑上构建 APK。

## 方案一：使用已安装 Android Studio 的电脑

### 步骤 1：复制项目文件
将以下文件夹复制到另一台电脑：
```
StudentEducationSystem/
├── EduLauncher/
└── PostureDetection/
```

### 步骤 2：打开项目
1. 启动 Android Studio
2. File → Open → 选择 `EduLauncher` 文件夹
3. 等待 Gradle 同步完成

### 步骤 3：构建 APK
1. Build → Build Bundle(s) / APK(s) → Build APK(s)
2. 等待构建完成
3. 点击右下角 "locate" 找到 APK

### 步骤 4：获取 APK
APK 文件位置：
```
EduLauncher/app/build/outputs/apk/debug/app-debug.apk
```

---

## 方案二：命令行构建（无需 Android Studio GUI）

### 前提条件
目标电脑需要：
- JDK 17+ 已安装
- Android SDK 已安装

### 构建步骤
```bash
cd EduLauncher

# 配置 SDK 路径（如果未设置环境变量）
echo "sdk.dir=C:\\\\Users\\\\<用户名>\\\\AppData\\\\Local\\\\Android\\\\Sdk" > local.properties

# 构建 Debug APK
.\gradlew assembleDebug
```

---

## 方案三：GitHub Actions 自动构建（推荐）

我为你创建了 GitHub Actions 工作流，可以自动构建 APK。

### 步骤 1：创建 GitHub 仓库
1. 访问 https://github.com/new
2. 创建新仓库，命名为 `StudentEducationSystem`
3. 上传项目代码

### 步骤 2：添加 GitHub Actions 配置

创建文件 `.github/workflows/build-apk.yml`：

```yaml
name: Build APK

on:
  push:
    branches: [ main, master ]
  pull_request:
    branches: [ main, master ]
  workflow_dispatch:

jobs:
  build:
    runs-on: ubuntu-latest
    
    steps:
    - uses: actions/checkout@v3
    
    - name: Set up JDK 17
      uses: actions/setup-java@v3
      with:
        java-version: '17'
        distribution: 'temurin'
        
    - name: Setup Android SDK
      uses: android-actions/setup-android@v2
      
    - name: Cache Gradle packages
      uses: actions/cache@v3
      with:
        path: |
          ~/.gradle/caches
          ~/.gradle/wrapper
        key: ${{ runner.os }}-gradle-${{ hashFiles('**/*.gradle*', '**/gradle-wrapper.properties') }}
        
    - name: Grant execute permission for gradlew
      run: chmod +x EduLauncher/gradlew
      
    - name: Build with Gradle
      run: |
        cd EduLauncher
        ./gradlew assembleDebug
        
    - name: Upload APK
      uses: actions/upload-artifact@v3
      with:
        name: app-debug
        path: EduLauncher/app/build/outputs/apk/debug/app-debug.apk
```

### 步骤 3：触发构建
1. 提交代码到 GitHub
2. 进入仓库 → Actions 标签
3. 点击 "Build APK" → "Run workflow"
4. 等待构建完成（约 5-10 分钟）
5. 下载构建好的 APK

---

## 快速获取 APK 的最简单方法

### 方法：找一台已配置好的电脑

如果你有朋友或同事的电脑已经安装了 Android Studio：

1. **复制项目文件夹**到该电脑
2. **打开 Android Studio** → Open → 选择项目
3. **点击绿色锤子图标**（Build）或按 Ctrl+F9
4. **等待 2-5 分钟**
5. **APK 生成在**：`app/build/outputs/apk/debug/app-debug.apk`

---

## 项目结构说明

构建时需要的关键文件：
```
EduLauncher/
├── app/
│   ├── src/                    # 源代码
│   ├── build.gradle            # 模块构建配置
│   └── proguard-rules.pro      # 混淆规则
├── build.gradle                # 项目构建配置
├── gradle.properties           # Gradle 配置
├── gradlew                     # Gradle Wrapper (Unix)
├── gradlew.bat                 # Gradle Wrapper (Windows)
└── settings.gradle             # 项目设置
```

---

## 常见问题

### Q: 构建失败，提示 "SDK location not found"
**A**: 创建 `local.properties` 文件，内容：
```
sdk.dir=C\:\\Users\\<你的用户名>\\AppData\\Local\\Android\\Sdk
```

### Q: 构建失败，提示 "Could not find com.android.tools.build:gradle"
**A**: 检查网络连接，需要下载 Gradle 插件

### Q: 构建成功但找不到 APK
**A**: APK 在 `app/build/outputs/apk/debug/` 目录下

---

## 需要帮助？

如果以上方法都无法使用，可以：
1. 使用在线 APK 构建服务
2. 寻找有 Android 开发环境的朋友帮忙
3. 在网吧或学校机房的电脑上安装 Android Studio 构建
