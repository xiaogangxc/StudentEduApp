# GitHub Actions 自动构建 APK 指南

## 快速开始

### 1. 创建 GitHub 仓库

1. 访问 https://github.com/new
2. 仓库名称：`StudentEducationSystem`（或其他你喜欢的名字）
3. 选择 **Public**（免费）或 **Private**（需要付费账户才能使用 Actions）
4. 点击 **Create repository**

### 2. 上传代码到 GitHub

在 `StudentEducationSystem` 文件夹所在目录打开命令行，执行：

```bash
# 初始化 git
cd StudentEducationSystem
git init

# 添加所有文件
git add .

# 提交
git commit -m "Initial commit"

# 添加远程仓库（将 YOUR_USERNAME 替换为你的 GitHub 用户名）
git remote add origin https://github.com/YOUR_USERNAME/StudentEducationSystem.git

# 推送代码
git branch -M main
git push -u origin main
```

### 3. 触发自动构建

推送代码后，GitHub Actions 会自动开始构建：

1. 打开你的 GitHub 仓库页面
2. 点击 **Actions** 标签
3. 你会看到 "Build APK" 工作流正在运行
4. 等待约 5-10 分钟

### 4. 下载 APK

构建完成后：

1. 进入 **Actions** 标签
2. 点击最新的工作流运行记录
3. 滚动到页面底部的 **Artifacts**
4. 下载：
   - `debug-apk` - 调试版本（推荐测试用）
   - `release-apk` - 发布版本

下载的文件是 ZIP 压缩包，解压后即可获得 APK 文件。

---

## 手动触发构建

如果你想手动重新构建（不修改代码）：

1. 进入 **Actions** 标签
2. 点击 **Build APK**
3. 点击右侧的 **Run workflow**
4. 选择分支（通常是 `main`）
5. 点击 **Run workflow**

---

## 常见问题

### Q: 构建失败怎么办？

A: 点击失败的构建记录，查看日志。常见问题：
- 代码语法错误
- 依赖版本不兼容
- 网络问题导致依赖下载失败

### Q: 如何修改构建配置？

A: 编辑 `.github/workflows/build-apk.yml` 文件，可以：
- 修改 Java 版本
- 添加签名配置
- 添加测试步骤

### Q: 私有仓库可以用吗？

A: 可以，但 GitHub 对私有仓库的 Actions 有免费额度限制（每月 2000 分钟）。

### Q: 构建的 APK 可以直接安装吗？

A: 
- Debug 版本：可以直接安装到任何 Android 设备
- Release 版本：需要签名才能安装，当前构建的是未签名版本

---

## 下一步：添加签名（可选）

如果你想构建可直接发布的 APK，需要添加签名配置：

1. 生成签名密钥：
```bash
keytool -genkey -v -keystore my-key.jks -keyalg RSA -keysize 2048 -validity 10000 -alias my-alias
```

2. 在 GitHub 仓库设置中添加 Secrets：
   - `KEYSTORE_BASE64`: 密钥文件的 Base64 编码
   - `KEYSTORE_PASSWORD`: 密钥库密码
   - `KEY_ALIAS`: 密钥别名
   - `KEY_PASSWORD`: 密钥密码

3. 联系我更新工作流配置以支持签名

---

## 需要帮助？

如果在构建过程中遇到问题，请提供：
1. GitHub 仓库链接
2. 构建失败的日志截图
3. 错误信息
