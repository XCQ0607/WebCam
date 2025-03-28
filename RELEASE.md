# Release Notes

## v1.0.3

### 📅 Release Date: 2024-06-19

### ✨ New Features
- **设置页面默认启动**:
  - 修改应用默认启动页面为设置页面，方便用户直接查看配置和日志
  - 用户无需手动切换即可直接在启动后看到设置和日志信息

- **手动加载配置按钮**:
  - 新增醒目的"手动加载配置"按钮，位于设置页面顶部
  - 提供一键式配置加载功能，解决自动加载配置可能失败的问题
  - 加载过程有清晰的日志提示，方便排查问题

### 🐛 Bug Fixes
- 修复了配置加载日志不显示的问题
- 改进了日志显示机制，使日志更加清晰可见
- 解决了启动时无法正确加载预设的问题

### 🔄 Improvements
- **UI布局优化**: 
  - 优化了手动加载配置按钮的位置和样式，使其更加醒目
  - 改进了按钮和文字的对齐方式，提升视觉体验
- **日志显示增强**: 
  - 添加了更明显的日志标记，方便定位关键操作
  - 优化了日志清除和加载的流程

## v1.0.2

### 📅 Release Date: 2024-03-25

### ✨ New Features
- **视频流端口修改**:
  - 增加了自定义视频流端口的选择，可根据不同网络环境需求调整
  - 支持在不同网络环境下灵活切换视频流端口
- **连接预设增强**:
  - 优化了连接预设的管理功能，更加直观地显示和操作
  - 改进了预设删除和覆盖确认的用户体验

### 🐛 Bug Fixes
- 修复了"恢复默认"按钮功能，现在可以正确地向摄像头发送恢复命令
- 解决了设置页面中Compose组件的编译错误问题
- 修复了切换界面时出现"UI更新-save"提示的问题
- 修复了连接设置组件中的状态管理问题

### 🔄 Improvements
- **用户界面优化**: 
  - 改进了对话框的显示逻辑，提高了UI一致性
  - 优化了状态管理，减少了不必要的UI更新
- **性能提升**: 
  - 减少了不必要的Toast提示信息
  - 优化了连接设置页面的组件重绘
- **构建优化**:
  - 更新了版本号标记，为后续版本迭代做准备
  - 改进了构建配置，提高了打包效率

## v1.0.1

### 📅 Release Date: 2024-03-23

### ✨ New Features
- **文件选择器功能**: 
  - 添加了自定义文件选择器，用户可以浏览文件系统选择保存照片的目录
  - 集成了快捷路径按钮，一键定位到常用目录如照片、下载和DCIM
  - 支持创建新文件夹功能
- **照片命名格式更新**: 照片现在以"WebCam_日期_时间_设备IP"的格式命名，便于识别
- **权限管理优化**: 
  - 新增权限管理卡片，清晰显示当前权限状态
  - 简化权限申请流程，自动引导用户授予所需权限
- **多语言支持**: 增加了英文版README，为非中文用户提供更好的支持

### 🐛 Bug Fixes
- 修复了选择目录后保存路径未更新的问题
- 修复了视频流加载后仍显示加载图标的问题
- 解决了Android 11及以上版本无法访问自定义目录的问题
- 优化了不同Android版本下的文件存储权限处理

### 🔄 Improvements
- **存储设置优化**: 改进了存储设置界面，更直观地展示当前选择的目录
- **权限引导**: 添加了明确的权限提示，并提供一键跳转到系统设置页面
- **调试信息**: 集成了文件系统浏览调试信息，帮助解决权限和访问问题
- **性能提升**: 减少了视频流加载时的内存占用
- **文档更新**: 补充了新功能的使用说明和截图

## v1.0.0

### 📅 Release Date: 2024-03-15

### ✨ Initial Release
- 完整的ESP32-CAM摄像头控制功能
- 实时视频流显示
- 拍照和保存功能
- 摄像头参数调整
- 中文界面支持
