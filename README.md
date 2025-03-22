# ESP32-CAM WebCam

[![Android](https://img.shields.io/badge/Platform-Android-green.svg)](https://developer.android.com/)
[![Kotlin](https://img.shields.io/badge/Language-Kotlin-orange.svg)](https://kotlinlang.org/)
[![ESP32](https://img.shields.io/badge/Hardware-ESP32--CAM-blue.svg)](https://www.espressif.com/)

一个适用于ESP32-CAM的Android客户端应用，提供简单易用的界面来控制和查看ESP32-CAM摄像头。

## 📸 功能特性

- **实时视频流**：通过WiFi连接直接查看ESP32-CAM的实时视频流
- **拍摄照片**：捕获高质量静态图像并保存至相册
- **摄像头控制**：调整分辨率、亮度、对比度等参数
- **灯光控制**：控制摄像头的LED灯
- **多种连接方式**：支持直接连接ESP32-CAM的AP或通过局域网连接
- **摄像头信息**：查看ESP32-CAM的详细系统信息
- **设置保存**：保存和恢复摄像头设置

## 🚀 快速开始

### 系统要求

- Android 6.0 (API 23)或更高版本
- 支持Android 14
- ESP32-CAM运行最新的[ESP32-CAM Webserver](https://github.com/easytarget/esp32-cam-webserver)固件(v4.0+)

### 安装方法

1. 从[Releases](https://github.com/你的用户名/WebCam/releases)页面下载最新APK
2. 在Android设备上安装APK
3. 启动ESP32-CAM并确保其已启动WiFi
4. 打开应用并连接到摄像头

## 💻 连接摄像头

### 接入点模式 (推荐)

1. 在应用中，进入"设置"页面
2. 配置IP地址为`192.168.4.1`(ESP32-CAM的默认AP IP)
3. 确保HTTP端口为`80`，流媒体端口为`81`
4. 点击"测试连接"
5. 连接成功后，返回主页面开始使用

### 局域网模式

1. 确保ESP32-CAM连接到与您手机相同的WiFi网络
2. 在应用中，进入"设置"页面
3. 输入ESP32-CAM在您局域网中的IP地址
4. 测试连接并开始使用

## 🔍 使用指南

### 主界面

- **启动/停止视频**：控制视频流的开启和关闭
- **拍摄照片**：捕获当前画面的静态图像
- **保存到相册**：将捕获的图像保存到手机相册
- **相机控制**：调整分辨率、亮度、对比度等参数
- **重启摄像头**：远程重启ESP32-CAM

### 设置界面

- **连接设置**：配置IP地址和端口
- **预设连接**：保存和加载常用连接配置
- **图像设置**：调整图像质量、分辨率等

### 故障排除

- 如果连接失败，请检查ESP32-CAM是否已启动并可访问
- 确认IP地址和端口设置正确
- 如果视频流不显示，请尝试刷新或重启摄像头
- 对于AP模式，确保您的手机已连接到ESP32-CAM的WiFi网络

## 🔧 开发者信息

### 技术栈

- **语言**：Kotlin
- **UI框架**：Jetpack Compose
- **网络**：Ktor客户端
- **图像处理**：OpenCV
- **异步处理**：Kotlin协程

### 项目结构

```
app/
├── src/main/
│   ├── java/com/kust/webcam/
│   │   ├── data/              # 数据层（模型、存储库）
│   │   ├── domain/            # 领域层（视图模型、用例）
│   │   ├── ui/                # UI层（界面、组件）
│   │   │   ├── components/    # 可复用UI组件
│   │   │   ├── screens/       # 主要界面
│   │   │   └── theme/         # 应用主题
│   │   └── utils/             # 工具类和扩展
│   └── res/                   # 资源文件
└── build.gradle               # 项目构建文件
```

### 构建项目

1. 克隆仓库：
   ```
   git clone https://github.com/你的用户名/WebCam.git
   ```

2. 使用Android Studio打开项目

3. 同步Gradle并构建项目

4. 运行应用到连接的设备或模拟器

## 📋 许可证

本项目采用 [MIT 许可证](LICENSE)

## 🙏 致谢

- [ESP32-CAM Webserver](https://github.com/easytarget/esp32-cam-webserver) - ESP32-CAM固件
- [OpenCV](https://opencv.org/) - 开源计算机视觉库
- [Jetpack Compose](https://developer.android.com/jetpack/compose) - Android现代UI工具包 