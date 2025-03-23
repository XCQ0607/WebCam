# ESP32-CAM WebCam

[![Android](https://img.shields.io/badge/Platform-Android-green.svg)](https://developer.android.com/)
[![Kotlin](https://img.shields.io/badge/Language-Kotlin-orange.svg)](https://kotlinlang.org/)
[![ESP32](https://img.shields.io/badge/Hardware-ESP32--CAM-blue.svg)](https://www.espressif.com/)
[![Version](https://img.shields.io/badge/Version-1.0.1-brightgreen.svg)](https://github.com/XCQ0607/WebCam/releases)

An Android client application for ESP32-CAM with a complete interface, for controlling and viewing ESP32-CAM cameras.

[中文版 (Chinese Version)](README.md)

<div align="center">
  <img src="https://github.com/user-attachments/assets/05ba4ac0-875b-4078-804d-57612562aed4" width="300" alt="ESP32-CAM Hardware">
</div>

## 📸 Features

- **Chinese Interface**: Fully localized user interface for Chinese users
- **Live Video Stream**: Directly view ESP32-CAM's real-time video stream via WiFi
- **Take Photos**: Capture high-quality static images and save to gallery
- **Custom Storage**: Choose where to save photos, with support for creating new folders
- **Camera Control**: Adjust resolution, brightness, contrast and other parameters
- **Light Control**: Control the camera's LED light
- **Permission Management**: Intuitive permission status display and one-click request functionality
- **Multiple Connection Methods**: Support direct connection to ESP32-CAM AP or via LAN
- **Camera Information**: View detailed system information of ESP32-CAM
- **Save Settings**: Save and restore camera settings

## 📱 App Screenshots

### v1.0.0
<div align="center">
  <img src="https://github.com/user-attachments/assets/7d14357d-a84a-4a0d-b09f-f575e7b40ee4" width="300" alt="Video Stream Interface v1.0.0">
  <img src="https://github.com/user-attachments/assets/e7d4858c-9236-410d-a4fd-446426dbd8ad" width="300" alt="Settings Interface v1.0.0">
</div>

### v1.0.1
<div align="center">
  <img src="https://github.com/user-attachments/assets/323e89de-0ae2-438d-98fb-9ef7389a73bd" width="300" alt="Video Stream Interface v1.0.1">
  <img src="https://github.com/user-attachments/assets/116daf2d-ee16-4e02-a958-ce3a6ccee061" width="300" alt="Settings Interface v1.0.1">
</div>

## 🚀 Quick Start

### System Requirements

- Android 6.0 (API 23) or higher
- Supports Android 14
- ESP32-CAM hardware
- ESP32-CAM flashed with Chinese firmware: [XCQ0607/esp32-cam-webserver-HAN](https://github.com/XCQ0607/esp32-cam-webserver-HAN)

### Installation

1. Download the latest APK from the [Releases](https://github.com/XCQ0607/WebCam/releases) page
   - Multiple CPU architecture APK files are provided, please choose the appropriate version for your device:
     - **arm64-v8a**: For most modern Android phones
     - **armeabi-v7a**: For older Android devices
     - **x86/x86_64**: For Intel processor-based devices (such as some tablets)
     - **Universal version**: Contains all architectures, suitable for any device but larger file size
   - If you are unsure of your device's architecture, you can install the universal version

2. Install the APK on your Android device
3. Start ESP32-CAM and ensure it has started WiFi
4. Open the app and connect to the camera

### Firmware Installation (Required)

This application needs to be used with the Chinese version of ESP32-CAM firmware:

1. Clone the firmware repository: `git clone https://github.com/XCQ0607/esp32-cam-webserver-HAN.git`
2. Open the project with PlatformIO and compile
3. Flash the compiled firmware to ESP32-CAM hardware
4. After flashing, ESP32-CAM will automatically start and create a WiFi hotspot

## 💻 Connecting to the Camera

### Access Point Mode (Recommended)

1. In the app, go to the "Settings" page
2. Configure the IP address to `192.168.4.1` (ESP32-CAM's default AP IP)
3. Ensure HTTP port is `80` and streaming port is `81`
4. Click "Test Connection"
5. After successful connection, return to the main page to start using

### LAN Mode

1. Ensure ESP32-CAM is connected to the same WiFi network as your phone
2. In the app, go to the "Settings" page
3. Enter the ESP32-CAM's IP address in your local network
4. Test the connection and start using

## 🔍 Usage Guide

### Main Interface

- **Start/Stop Video**: Control the video stream on and off
- **Take Photo**: Capture a static image of the current view
- **Save to Gallery**: Save the captured image to your phone's gallery
- **Camera Controls**: Adjust resolution, brightness, contrast, and other parameters
- **Restart Camera**: Remotely restart ESP32-CAM

### Settings Interface

- **Connection Settings**: Configure IP address and ports
- **Preset Connections**: Save and load common connection configurations
- **Image Settings**: Adjust image quality, resolution, etc.

### Troubleshooting

- If connection fails, check if ESP32-CAM is started and accessible
- Confirm that the IP address and port settings are correct
- If the video stream does not display, try refreshing or restarting the camera
- For AP mode, make sure your phone is connected to ESP32-CAM's WiFi network

## 🔧 Developer Information

### Tech Stack

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose
- **Network**: Ktor client
- **Asynchronous Processing**: Kotlin coroutines

### Project Structure

```
app/
├── src/main/
│   ├── java/com/kust/webcam/
│   │   ├── data/              # Data layer (models, repositories)
│   │   ├── domain/            # Domain layer (view models, use cases)
│   │   ├── ui/                # UI layer (interfaces, components)
│   │   │   ├── components/    # Reusable UI components
│   │   │   ├── screens/       # Main screens
│   │   │   └── theme/         # App theme
│   │   └── utils/             # Utility classes and extensions
│   └── res/                   # Resource files
└── build.gradle               # Project build file
```