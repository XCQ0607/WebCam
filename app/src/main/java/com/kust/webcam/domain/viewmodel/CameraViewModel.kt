package com.kust.webcam.domain.viewmodel

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
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

class CameraViewModel(private val repository: CameraRepository = CameraRepository()) : ViewModel() {

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

    // 摄像头信息
    private val _cameraInfo = MutableStateFlow<String>("")
    val cameraInfo = _cameraInfo.asStateFlow()

    // 操作结果提示信息
    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage = _toastMessage.asStateFlow()

    // 预设连接
    private val _savedConnections = MutableStateFlow<List<ConnectionSettings>>(
        listOf(
            ConnectionSettings(),
            ConnectionSettings(ipAddress = "192.168.1.1", connectionName = "本地网络"),
            ConnectionSettings(ipAddress = "192.168.43.1", connectionName = "热点网络")
        )
    )
    val savedConnections = _savedConnections.asStateFlow()

    // 更新连接设置
    fun updateConnectionSettings(settings: ConnectionSettings) {
        repository.updateConnectionSettings(settings)
        showToast("已更新连接设置")
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

    // 保存当前图片到相册
    fun saveImageToGallery(context: Context): Boolean {
        val bitmap = _capturedImage.value ?: return false
        
        return try {
            viewModelScope.launch {
                val imageUri = saveBitmapToGallery(context, bitmap)
                _lastSavedImageUri.value = imageUri
                if (imageUri != null) {
                    repository.addLog("图片已保存到相册")
                    showToast("图片已保存到相册: ${Environment.DIRECTORY_PICTURES}/WebCam")
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
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "WebCam_$timestamp.jpg"
        var uri: Uri? = null
        
        try {
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/WebCam")
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
                    infoMap[key]?.let { sb.appendLine("$key: $it") }
                }
                sb.appendLine()
                
                // 网络信息部分
                sb.appendLine("🌐 网络信息:")
                val networkKeys = listOf("模式", "SSID", "IP地址", "MAC地址")
                for (key in networkKeys) {
                    infoMap[key]?.let { sb.appendLine("$key: $it") }
                }
                infoMap["HTTP端口"]?.let { sb.appendLine("HTTP端口: $it") }
                sb.appendLine()
                
                // 系统信息部分
                sb.appendLine("⚙️ 系统信息:")
                val systemKeys = listOf("运行时间", "CPU频率", "MCU温度", "堆内存", "PSRAM")
                for (key in systemKeys) {
                    infoMap[key]?.let { sb.appendLine("$key: $it") }
                }
                
                // 添加视频流信息 (可能包含在一个字符串中)
                infoMap.entries.find { it.key.contains("视频流") || it.value.contains("视频流") }?.let {
                    sb.appendLine(it.key + ": " + it.value)
                }
                
                // 添加SPIFFS文件系统信息
                infoMap.entries.find { it.key.contains("SPIFFS") || it.value.contains("SPIFFS") }?.let {
                    sb.appendLine(it.key + ": " + it.value)
                }
                
                return sb.toString()
            } catch (e: Exception) {
                return "解析摄像头信息出错: ${e.message}\n\n原始信息:\n$htmlInfo"
            }
        } else {
            // 非HTML格式，直接返回
            return htmlInfo
        }
    }
    
    // 从HTML中提取纯文本，保留合理的格式
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
            val success = repository.restoreDefaultSettings()
            if (success) {
                showToast("已恢复默认设置")
            } else {
                showToast("恢复默认设置失败")
            }
        }
    }

    // 重启摄像头
    fun reboot() {
        repository.reboot()
        showToast("已发送重启命令")
    }

    // 保存偏好设置
    fun savePreferences() {
        repository.savePreferences()
        showToast("已发送保存设置命令")
    }

    // 清除偏好设置
    fun clearPreferences() {
        repository.clearPreferences()
        showToast("已发送清除设置命令")
    }

    // 清除日志
    fun clearLogs() {
        repository.clearLogs()
        showToast("已清除日志")
    }

    // 清除错误
    fun clearError() {
        repository.clearError()
    }
    
    // 显示Toast消息
    private fun showToast(message: String) {
        _toastMessage.value = message
    }
    
    // 清除Toast消息（在UI层显示后调用）
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
            // 如果预设超过3个，则替换最后一个
            if (currentPresets.size >= 3) {
                currentPresets[2] = current
            } else {
                currentPresets.add(current)
            }
        }
        _savedConnections.value = currentPresets
        showToast("已保存当前连接")
    }

    // 加载预设连接设置
    fun loadConnectionPreset(index: Int) {
        val presets = _savedConnections.value
        if (index in presets.indices) {
            repository.updateConnectionSettings(presets[index])
            showToast("已加载预设连接")
        }
    }

    // 无提示地停止视频流（用于页面切换时）
    fun stopStreamSilently() {
        if (_isStreaming.value) {
            _isStreaming.value = false
            viewModelScope.launch {
                stopStream()
            }
        }
    }
} 