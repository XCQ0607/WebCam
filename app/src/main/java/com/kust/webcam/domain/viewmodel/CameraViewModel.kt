package com.kust.webcam.domain.viewmodel

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.provider.Settings
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kust.webcam.data.model.CameraSettings
import com.kust.webcam.data.model.ConnectionSettings
import com.kust.webcam.data.model.FrameDurationLimit
import com.kust.webcam.data.model.FrameSize
import com.kust.webcam.data.model.SpecialEffect
import com.kust.webcam.data.model.WbMode
import com.kust.webcam.data.repository.CameraRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.app.Application
import android.os.Handler
import android.os.Looper

class CameraViewModel(val repository: CameraRepository = CameraRepository()) : ViewModel() {

    // 连接设置
    val connectionSettings = repository.connectionSettings
    val connectionStatus = repository.connectionStatus
    val lastError = repository.lastError
    val operationLogs = repository.operationLogs

    // 相机设置
    val cameraSettings = repository.cameraSettings

    // 捕获的图像
    private val _capturedImage = MutableStateFlow<Bitmap?>(null)
    val capturedImage = _capturedImage.asStateFlow()

    // 是否正在流式传输
    private val _isStreaming = MutableStateFlow(false)
    val isStreaming = _isStreaming.asStateFlow()

    // 最后保存的图片URI
    private val _lastSavedImageUri = MutableStateFlow<Uri?>(null)
    val lastSavedImageUri = _lastSavedImageUri.asStateFlow()
    
    // 保存目录设置
    private val _saveDirectory = MutableStateFlow(Environment.DIRECTORY_PICTURES + "/WebCam")
    val saveDirectory = _saveDirectory.asStateFlow()
    
    // 摄像头信息
    private val _cameraInfo = MutableStateFlow<String>("")
    val cameraInfo = _cameraInfo.asStateFlow()

    // 操作结果提示信息
    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage = _toastMessage.asStateFlow()

    // 预设连接
    val _savedConnections = MutableStateFlow<List<ConnectionSettings>>(
        listOf(
            ConnectionSettings(connectionName = "默认")
        )
    )
    val savedConnections = _savedConnections.asStateFlow()

    // 更新连接设置
    fun updateConnectionSettings(settings: ConnectionSettings) {
        repository.updateConnectionSettings(settings)
    }

    // 测试连接
    fun testConnection() {
        viewModelScope.launch {
            val success = repository.testConnection()
            if (success) {
                showToast("连接成功")
            } else {
                showToast("连接失败")
            }
        }
    }

    // 更新设置
    fun updateSetting(name: String, value: Any) {
        viewModelScope.launch {
            repository.updateSetting(name, value)
        }
    }

    // 捕获静态图像
    fun captureStillImage() {
        viewModelScope.launch {
            val bitmap = repository.captureStillImage()
            _capturedImage.value = bitmap
            if (bitmap != null) {
                showToast("图像捕获成功")
            } else {
                showToast("图像捕获失败")
            }
        }
    }

    // 开始/停止流
    fun toggleStream() {
        _isStreaming.value = !_isStreaming.value
        if (_isStreaming.value) {
            showToast("开始视频流")
        } else {
            // 停止视频流并调用/stop端点确保资源释放
            viewModelScope.launch {
                stopStream()
                showToast("停止视频流")
            }
        }
    }
    
    // 在离开界面时，静默停止视频流（不显示提示）
    fun stopStreamSilently() {
        _isStreaming.value = false
        viewModelScope.launch {
            stopStream()
        }
    }

    // 停止视频流并释放资源
    private suspend fun stopStream() {
        try {
            val settings = connectionSettings.value
            val url = "http://${settings.ipAddress}:${settings.httpPort}/stop"
            val success = repository.callSimpleEndpoint(url)
            if (success) {
                repository.addLog("停止视频流成功")
            } else {
                repository.addLog("停止视频流失败")
            }
        } catch (e: Exception) {
            repository.addLog("停止视频流错误: ${e.message}")
        }
    }

    // 获取流URL
    fun getStreamUrl(): String {
        return repository.getStreamUrl()
    }
    
    // 更新保存目录设置
    fun updateSaveDirectory(directory: String) {
        val oldDirectory = _saveDirectory.value
        _saveDirectory.value = directory
        repository.addLog("目录更新操作 - 原目录: $oldDirectory, 新目录: $directory")
        repository.addLog("目录更新后检查 - 当前保存目录值: ${_saveDirectory.value}")
        showToast("保存目录已更新: $directory")
        
        // 记录日志到控制台以便调试
        println("WebCam日志: 目录已更新 - 从 $oldDirectory 到 $directory")
    }

    // 打开系统应用设置
    fun openAppSettings(context: Context) {
        try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", context.packageName, null)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            repository.addLog("打开应用设置")
        } catch (e: Exception) {
            showToast("无法打开应用设置")
            repository.addLog("打开应用设置失败: ${e.message}")
        }
    }

    // 保存当前图片到相册
    fun saveImageToGallery(context: Context): Boolean {
        val bitmap = _capturedImage.value ?: return false
        
        return try {
            viewModelScope.launch {
                val imageUri = saveBitmapToGallery(context, bitmap)
                _lastSavedImageUri.value = imageUri
                if (imageUri != null) {
                    repository.addLog("图片已保存到相册")
                    showToast("图片已保存到相册: ${saveDirectory.value}")
                } else {
                    repository.addLog("保存图片失败")
                    showToast("保存图片失败")
                }
            }
            true
        } catch (e: Exception) {
            repository.addLog("保存图片错误: ${e.message}")
            showToast("保存图片错误: ${e.message}")
            false
        }
    }
    
    private suspend fun saveBitmapToGallery(context: Context, bitmap: Bitmap): Uri? = withContext(Dispatchers.IO) {
        val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
        val timestamp = dateFormat.format(Date())
        
        // 获取当前IP地址
        val ipAddress = connectionSettings.value.ipAddress
        
        // 生成文件名: WebCam_日期_时间_IP地址
        val fileName = "WebCam_${timestamp}_${ipAddress}.jpg"
        var uri: Uri? = null
        
        try {
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(MediaStore.MediaColumns.RELATIVE_PATH, saveDirectory.value)
                    put(MediaStore.MediaColumns.IS_PENDING, 1)
                }
            }
            
            val resolver = context.contentResolver
            uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            
            uri?.let {
                val outputStream: OutputStream = resolver.openOutputStream(it) ?: throw RuntimeException("无法打开输出流")
                
                if (!bitmap.compress(Bitmap.CompressFormat.JPEG, 95, outputStream)) {
                    throw RuntimeException("无法保存位图")
                }
                
                outputStream.close()
                
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    contentValues.clear()
                    contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
                    resolver.update(it, contentValues, null, null)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            uri = null
        }
        
        return@withContext uri
    }

    // 获取摄像头信息
    fun fetchCameraInfo() {
        viewModelScope.launch {
            val info = repository.getCameraInfo()
            _cameraInfo.value = parseCameraInfo(info)
            showToast("获取摄像头信息成功")
        }
    }
    
    // 解析HTML格式的摄像头信息
    private fun parseCameraInfo(htmlInfo: String): String {
        if (htmlInfo.isEmpty()) {
            return "未获取到摄像头信息"
        }
        
        // 检查是否是HTML格式
        if (htmlInfo.contains("<html>") || htmlInfo.contains("<body>")) {
            try {
                val sb = StringBuilder()
                
                // 从HTML中提取文本内容，并处理<br>标签
                val content = extractTextFromHtml(htmlInfo)
                
                // 提取标题
                val titleMatch = Regex("<h1>(.*?)</h1>").find(htmlInfo)
                val title = titleMatch?.groupValues?.getOrNull(1)?.trim() ?: "ESP32 摄像头信息"
                
                sb.appendLine("📸 $title")
                sb.appendLine()
                
                // 从内容中提取信息的键值对
                val infoMap = mutableMapOf<String, String>()
                val lines = content.split('\n')
                
                for (line in lines) {
                    val trimmed = line.trim()
                    if (trimmed.isEmpty()) continue
                    
                    // 查找冒号分隔的键值对
                    val colonIndex = trimmed.indexOf(':')
                    if (colonIndex > 0) {
                        val key = trimmed.substring(0, colonIndex).trim()
                        val value = trimmed.substring(colonIndex + 1).trim()
                        infoMap[key] = value
                    }
                }
                
                // 设备信息部分
                sb.appendLine("📱 设备信息:")
                val deviceKeys = listOf("名称", "固件版本", "程序大小", "MD5校验", "ESP SDK版本")
                for (key in deviceKeys) {
                    infoMap[key]?.let {
                        sb.appendLine("• $key: $it")
                    }
                }
                
                sb.appendLine()
                
                // 相机参数部分
                sb.appendLine("🔍 相机参数:")
                val cameraKeys = listOf("分辨率", "图像质量", "帧率", "亮度", "对比度", "饱和度", "特效", "白平衡", "曝光控制")
                for (key in cameraKeys) {
                    infoMap[key]?.let {
                        sb.appendLine("• $key: $it")
                    }
                }
                
                sb.appendLine()
                
                // 网络信息部分
                sb.appendLine("🌐 网络信息:")
                val networkKeys = listOf("IP地址", "网关", "子网掩码", "MAC地址", "主机名", "SSID")
                for (key in networkKeys) {
                    infoMap[key]?.let {
                        sb.appendLine("• $key: $it")
                    }
                }
                
                return sb.toString()
            } catch (e: Exception) {
                return "解析摄像头信息失败: ${e.message}"
            }
        } else {
            // 非HTML内容，直接返回
            return htmlInfo
        }
    }
    
    // 从HTML中提取文本
    private fun extractTextFromHtml(html: String): String {
        return html
            // 移除脚本和样式
            .replace(Regex("<script[^>]*>.*?</script>", RegexOption.DOT_MATCHES_ALL), "")
            .replace(Regex("<style[^>]*>.*?</style>", RegexOption.DOT_MATCHES_ALL), "")
            // 处理特定标签为换行
            .replace("<br>", "\n")
            .replace("<br/>", "\n")
            .replace("<br />", "\n")
            .replace("<p>", "\n")
            .replace("</p>", "\n")
            .replace("<h1>", "\n")
            .replace("</h1>", "\n")
            .replace("<h2>", "\n")
            .replace("</h2>", "\n")
            .replace("<div>", "\n")
            .replace("</div>", "\n")
            // 移除其他HTML标签
            .replace(Regex("<[^>]*>"), " ")
            // 处理HTML实体
            .replace("&nbsp;", " ")
            .replace("&deg;", "°")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&amp;", "&")
            .replace("&quot;", "\"")
            // 处理连续空格和换行
            .replace(Regex(" {2,}"), " ")
            .replace(Regex("\n{3,}"), "\n\n")
            .trim()
    }
    
    // 提取HTML中特定信息的辅助方法
    private fun extractInfoFromHtml(html: String, pattern: String): String? {
        val regex = Regex(pattern)
        val matchResult = regex.find(html)
        return matchResult?.groupValues?.getOrNull(1)?.trim()
    }

    // 刷新摄像头设置
    fun refreshSettings() {
        viewModelScope.launch {
            val success = repository.refreshCameraSettings()
            if (success) {
                showToast("摄像头设置已刷新")
            } else {
                showToast("刷新摄像头设置失败")
            }
        }
    }
    
    // 恢复默认设置
    fun restoreDefaultSettings() {
        viewModelScope.launch {
            repository.restoreDefaultSettings()
            showToast("恢复默认设置命令已发送")
        }
    }

    // 重启摄像头
    fun reboot() {
        viewModelScope.launch {
            repository.reboot()
            showToast("重启摄像头命令已发送")
        }
    }

    // 保存偏好设置
    fun savePreferences() {
        viewModelScope.launch {
            repository.savePreferences()
            showToast("保存设置命令已发送")
        }
    }

    // 清除偏好设置
    fun clearPreferences() {
        viewModelScope.launch {
            repository.clearPreferences()
            showToast("清除设置命令已发送")
        }
    }

    // 清除日志
    fun clearLogs() {
        repository.clearLogs()
    }

    // 清除错误
    fun clearError() {
        repository.clearError()
    }
    
    // 显示Toast消息
    fun showToast(message: String) {
        _toastMessage.value = message
        // 确保200ms后清空消息，避免被其他系统消息替换
        Handler(Looper.getMainLooper()).postDelayed({
            if (_toastMessage.value == message) {
                _toastMessage.value = null
            }
        }, 3000) // 3秒后清空，确保用户有足够时间看到
    }
    
    // 清除Toast消息
    fun clearToastMessage() {
        _toastMessage.value = null
    }

    // 保存当前连接设置到预设
    fun saveCurrentConnectionToPresets() {
        val current = connectionSettings.value
        val currentPresets = _savedConnections.value.toMutableList()
        // 检查是否已经存在，如果存在则更新
        val existingIndex = currentPresets.indexOfFirst { it.connectionName == current.connectionName }
        if (existingIndex >= 0) {
            currentPresets[existingIndex] = current
        } else {
            // 如果预设超过5个，则移除最后一个
            if (currentPresets.size >= 5) {
                currentPresets.removeAt(currentPresets.size - 1)
            }
            currentPresets.add(current)
        }
        _savedConnections.value = currentPresets
        showToast("已保存当前连接")
        
        // 保存预设到本地存储
        savePresetsToPersistentStorage()
    }

    // 保存预设到持久化存储
    fun savePresetsToPersistentStorage() {
        viewModelScope.launch {
            repository.savePresetsToStorage(_savedConnections.value)
        }
    }

    // 加载预设连接设置
    fun loadConnectionPreset(index: Int) {
        val presets = _savedConnections.value
        if (index in presets.indices) {
            val selectedPreset = presets[index]
            // 添加详细日志
            repository.addLog("开始加载预设[${index}]: ${selectedPreset.connectionName}")
            
            // 这里直接更新repository中的connectionSettings
            repository.updateConnectionSettings(selectedPreset)
            
            // 添加更多日志，标记加载完成
            repository.addLog("已加载预设: ${selectedPreset.connectionName}, IP: ${selectedPreset.ipAddress}")
            
            // 显示确定的Toast提示
            showToast("已加载预设: ${selectedPreset.connectionName}")
        } else {
            repository.addLog("加载预设失败: 索引${index}超出范围[0-${presets.size-1}]")
        }
    }

    // 加载默认连接配置
    fun loadDefaultConnection() {
        viewModelScope.launch {
            addLog("▶▶▶ 正在加载默认连接配置...")
            
            if (_savedConnections.value.isEmpty()) {
                addLog("▶▶▶ 没有可用的预设配置，使用硬编码的默认配置")
                // 如果没有预设，就使用系统默认的
                return@launch
            }
            
            // 使用第一个预设作为默认配置
            val defaultPreset = _savedConnections.value.first()
            
            addLog("▶▶▶ 使用第一个预设作为默认配置: ${defaultPreset.connectionName}, IP: ${defaultPreset.ipAddress}")
            
            // 更新连接设置
            updateConnectionSettings(defaultPreset)
            
            // 清除当前画面
            _capturedImage.value = null
            
            addLog("▶▶▶ 默认连接配置已加载")
            
            showToast("已加载默认配置: ${defaultPreset.connectionName}")
        }
    }
    
    // 从持久化存储加载预设
    fun loadPresetsFromPersistentStorage() {
        viewModelScope.launch {
            addLog("▶▶▶ 开始从持久化存储加载预设配置...")
            
            try {
                val loadedPresets = repository.loadPresetsFromStorage()
                
                if (loadedPresets.isNotEmpty()) {
                    _savedConnections.value = loadedPresets
                    addLog("▶▶▶ 成功加载 ${loadedPresets.size} 个预设配置")
                    
                    // 记录每个预设的名称
                    loadedPresets.forEachIndexed { index, preset ->
                        addLog("▶▶▶ 预设 ${index+1}: '${preset.connectionName}', IP: ${preset.ipAddress}")
                    }
                    
                    // 显示成功提示
                    showToast("成功加载 ${loadedPresets.size} 个预设配置")
                } else {
                    addLog("▶▶▶ 未找到预设配置，使用默认配置")
                    _savedConnections.value = listOf(ConnectionSettings(connectionName = "默认"))
                    showToast("未找到预设配置，使用默认配置")
                }
            } catch (e: Exception) {
                addLog("▶▶▶ 加载预设配置失败: ${e.message}")
                showToast("加载预设配置失败")
                _savedConnections.value = listOf(ConnectionSettings(connectionName = "默认"))
            }
        }
    }

    // 添加日志的便捷方法
    fun addLog(message: String) {
        // 同时输出到Android日志系统
        android.util.Log.d("WebCam_ViewModel", message)
        repository.addLog(message)
    }
}