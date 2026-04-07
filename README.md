# 学生教育系统 (Student Education System)

## 项目概述

这是一个学生作业辅导系统，包含三个核心组件：
1. **启动器 (EduLauncher)** - 主控界面、档案管理、同时启动豆包和坐姿管理
2. **豆包APP** - 作业辅导（第三方应用，通过Intent调用）
3. **坐姿管理程序 (PostureManager)** - 坐姿监控、语音提示

## 项目结构

```
StudentEducationSystem/
├── EduLauncher/          # 启动器模块
│   ├── app/src/main/
│   │   ├── java/com/student/education/
│   │   │   ├── ControlActivity.kt      # 控制界面（启动/关闭）
│   │   │   ├── MainActivity.kt         # 主界面
│   │   │   ├── ArchiveActivity.kt      # 档案查看
│   │   │   ├── model/
│   │   │   │   └── StudentArchive.kt   # 数据模型
│   │   │   ├── util/
│   │   │   │   ├── ArchiveManager.kt   # 档案管理工具
│   │   │   │   └── EncryptionUtil.kt   # AES加密工具
│   │   │   ├── report/
│   │   │   │   └── ReportGenerator.kt  # 报告生成器
│   │   │   └── service/
│   │   │       ├── WechatPushService.kt    # 微信推送服务
│   │   │       └── ReportScheduler.kt      # 报告定时调度
│   │   └── res/
│   │       ├── layout/
│   │       │   ├── activity_control.xml
│   │       │   ├── activity_main.xml
│   │       │   └── activity_archive.xml
│   │       └── values/
│   │           ├── strings.xml
│   │           ├── colors.xml
│   │           └── themes.xml
│   ├── build.gradle
│   ├── settings.gradle
│   └── gradle.properties
│
└── PostureManager/       # 坐姿管理模块
    ├── app/src/main/
    │   ├── java/com/student/posture/
    │   │   └── MainActivity.kt         # 姿态检测主界面
    │   └── res/
    │       ├── layout/
    │       │   └── activity_main.xml
    │       └── values/
    │           ├── strings.xml
    │           ├── colors.xml
    │           └── themes.xml
    ├── build.gradle
    ├── settings.gradle
    └── gradle.properties
```

## 功能说明

### 1. 启动器 (EduLauncher)

#### 核心功能
- **启动/关闭系统**：密码验证（默认123456）
- **档案管理**：AES-256加密存储学生档案
- **同时启动**：一键启动豆包APP和坐姿管理程序
- **U盘拷贝**：档案导入/导出
- **家长报告推送**：日报/周报/月报自动推送到家长微信

#### 学生档案字段
- 基础信息（姓名、性别、年级、学段等）
- 学科状态（教材版本、当前章节、掌握度）
- 性格评估（性格类型、沟通偏好、抗压能力）
- 薄弱知识点
- 家长偏好（辅导风格、学习时长、报告推送方式）

#### 家长报告推送功能
- **日报**：每天21:00自动推送，包含学习时长、科目、坐姿提醒次数
- **周报**：每周日20:00自动推送，包含本周学习统计、强弱科目分析
- **月报**：每月1号19:00自动推送，包含月度学习总结、进步情况
- **手动推送**：点击【报告推送】按钮可立即推送
- **配置方式**：点击【报告推送】→【配置家长微信】输入OpenID

#### 技术要点
- 档案存储路径：`/Android/data/com.student.education/files/`
- 加密方式：AES-256-CBC
- 密码：123456（可修改）
- 豆包包名：com.example.doubao
- 微信推送：使用微信小程序订阅消息（需配置appId和templateId）

### 2. 坐姿管理程序 (PostureManager)

#### 核心功能
- **姿态检测**：使用前置摄像头检测坐姿
- **检测指标**：
  - 头部前倾角度（阈值：>30度）
  - 脊柱弯曲角度（阈值：>20度）
  - 双肩水平度（阈值：>15度）
- **语音提示**：异常时播报"请坐好"
- **音频焦点处理**：豆包说话时静默

#### 技术要点
- 使用CameraX进行相机预览
- MediaPipe Pose进行姿态检测（需要进一步完善）
- 音频焦点监听避免与豆包语音冲突
- TTS语音合成

## 构建说明

### 环境要求
- Android Studio Arctic Fox (2020.3.1) 或更高版本
- JDK 11 或更高版本
- Android SDK API 21+ (Android 5.0+)

### 构建步骤

#### 1. 构建启动器
```bash
cd StudentEducationSystem/EduLauncher
./gradlew assembleDebug
```

#### 2. 构建坐姿管理程序
```bash
cd StudentEducationSystem/PostureManager
./gradlew assembleDebug
```

### 安装说明

1. 先安装豆包APP（需要自行获取）
2. 安装EduLauncher.apk
3. 安装PostureManager.apk

## 使用流程

1. 双击EduLauncher图标
2. 点击【启动系统】
3. 输入密码（默认123456）
4. 进入主界面
5. 点击【作业辅导】同时启动豆包和坐姿管理
6. 豆包使用外置USB摄像头，坐姿管理使用前置摄像头
7. 辅导结束后，豆包输出总结，启动器接收并更新档案

## 注意事项

1. **权限**：需要相机权限、存储权限
2. **设备**：需要支持前置摄像头的平板设备
3. **豆包APP**：需要单独安装，包名必须为com.example.doubao
4. **档案**：首次使用会自动创建默认档案用于测试

## 待完善功能

1. MediaPipe Pose姿态检测算法优化
2. 豆包APP返回总结数据的解析
3. U盘自动检测功能
4. 多设备适配
5. 设置界面功能完善

## 微信推送配置

在使用家长报告推送功能前，需要配置微信小程序参数：

1. **申请微信小程序**：在微信公众平台注册小程序
2. **开通订阅消息**：在小程序后台开通订阅消息功能
3. **创建消息模板**：创建日报、周报、月报的消息模板
4. **配置参数**：修改 `WechatPushService.kt` 中的配置：
   ```kotlin
   private const val APP_ID = "your_app_id_here"
   private const val APP_SECRET = "your_app_secret_here"
   private const val TEMPLATE_ID_DAILY = "your_daily_template_id"
   private const val TEMPLATE_ID_WEEKLY = "your_weekly_template_id"
   private const val TEMPLATE_ID_MONTHLY = "your_monthly_template_id"
   ```
5. **获取家长OpenID**：家长使用微信登录小程序后获取OpenID

## 开发团队

- 方案设计：AI Assistant
- 开发时间：约5-6天（并行开发）

## 许可证

MIT License