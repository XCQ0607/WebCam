package com.kust.webcam.data.repository

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.kust.webcam.data.model.CameraSettings
import com.kust.webcam.data.model.ConnectionSettings
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsChannel
import io.ktor.client.statement.bodyAsText
import io.ktor.utils.io.jvm.javaio.toInputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.TimeUnit

class CameraRepository {
    private val _connectionSettings = MutableStateFlow(ConnectionSettings())
    val connectionSettings = _connectionSettings.asStateFlow()

    private val _cameraSettings = MutableStateFlow(CameraSettings())
    val cameraSettings = _cameraSettings.asStateFlow()

    private val _connectionStatus = MutableStateFlow(false)
    val connectionStatus = _connectionStatus.asStateFlow()

    private val _lastError = MutableStateFlow<String?>(null)
    val lastError = _lastError.asStateFlow()

    private val _operationLogs = MutableStateFlow<List<String>>(emptyList())
    val operationLogs = _operationLogs.asStateFlow()

    private val httpClient = HttpClient(OkHttp) {
        install(HttpTimeout) {
            requestTimeoutMillis = 15000
            connectTimeoutMillis = 15000
            socketTimeoutMillis = 15000
        }
        engine {
            config {
                followRedirects(true)
                retryOnConnectionFailure(true)
                connectTimeout(15, TimeUnit.SECONDS)
                readTimeout(15, TimeUnit.SECONDS)
                writeTimeout(15, TimeUnit.SECONDS)
            }
        }
    }

    fun updateConnectionSettings(settings: ConnectionSettings) {
        _connectionSettings.value = settings
    }

    suspend fun testConnection(): Boolean {
        return try {
            val settings = connectionSettings.value
            val url = "http://${settings.ipAddress}:${settings.httpPort}/status"
            val response = httpClient.get(url)
            val success = response.status.value in 200..299
            _connectionStatus.value = success
            if (success) {
                val jsonText = response.bodyAsText()
                updateCameraSettingsFromJson(jsonText)
                addLog("连接成功: $url")
            } else {
                addLog("连接失败: ${response.status}")
                _lastError.value = "连接失败: ${response.status}"
            }
            success
        } catch (e: Exception) {
            _connectionStatus.value = false
            _lastError.value = "连接错误: ${e.message}"
            addLog("连接错误: ${e.message}")
            false
        }
    }

    private fun updateCameraSettingsFromJson(jsonText: String) {
        try {
            val jsonObject = JSONObject(jsonText)
            val settings = _cameraSettings.value.copy(
                framesize = jsonObject.optInt("framesize", _cameraSettings.value.framesize),
                quality = jsonObject.optInt("quality", _cameraSettings.value.quality),
                brightness = jsonObject.optInt("brightness", _cameraSettings.value.brightness),
                contrast = jsonObject.optInt("contrast", _cameraSettings.value.contrast),
                saturation = jsonObject.optInt("saturation", _cameraSettings.value.saturation),
                specialEffect = jsonObject.optInt("special_effect", _cameraSettings.value.specialEffect),
                awb = jsonObject.optInt("awb", if (_cameraSettings.value.awb) 1 else 0) == 1,
                awbGain = jsonObject.optInt("awb_gain", if (_cameraSettings.value.awbGain) 1 else 0) == 1,
                wbMode = jsonObject.optInt("wb_mode", _cameraSettings.value.wbMode),
                aec = jsonObject.optInt("aec", if (_cameraSettings.value.aec) 1 else 0) == 1,
                aec2 = jsonObject.optInt("aec2", if (_cameraSettings.value.aec2) 1 else 0) == 1,
                aeLevel = jsonObject.optInt("ae_level", _cameraSettings.value.aeLevel),
                aecValue = jsonObject.optInt("aec_value", _cameraSettings.value.aecValue),
                agc = jsonObject.optInt("agc", if (_cameraSettings.value.agc) 1 else 0) == 1,
                agcGain = jsonObject.optInt("agc_gain", _cameraSettings.value.agcGain),
                gainceiling = jsonObject.optInt("gainceiling", _cameraSettings.value.gainceiling),
                bpc = jsonObject.optInt("bpc", if (_cameraSettings.value.bpc) 1 else 0) == 1,
                wpc = jsonObject.optInt("wpc", if (_cameraSettings.value.wpc) 1 else 0) == 1,
                rawGma = jsonObject.optInt("raw_gma", if (_cameraSettings.value.rawGma) 1 else 0) == 1,
                lenc = jsonObject.optInt("lenc", if (_cameraSettings.value.lenc) 1 else 0) == 1,
                hmirror = jsonObject.optInt("hmirror", if (_cameraSettings.value.hmirror) 1 else 0) == 1,
                vflip = jsonObject.optInt("vflip", if (_cameraSettings.value.vflip) 1 else 0) == 1,
                dcw = jsonObject.optInt("dcw", if (_cameraSettings.value.dcw) 1 else 0) == 1,
                colorbar = jsonObject.optInt("colorbar", if (_cameraSettings.value.colorbar) 1 else 0) == 1,
                rotate = jsonObject.optInt("rotate", _cameraSettings.value.rotate),
                minFrameTime = jsonObject.optInt("min_frame_time", _cameraSettings.value.minFrameTime),
                lamp = jsonObject.optInt("lamp", _cameraSettings.value.lamp),
                autoLamp = jsonObject.optInt("autolamp", if (_cameraSettings.value.autoLamp) 1 else 0) == 1
            )
            _cameraSettings.value = settings
            addLog("相机设置已更新")
        } catch (e: Exception) {
            _lastError.value = "解析摄像头设置错误: ${e.message}"
            addLog("解析摄像头设置错误: ${e.message}")
        }
    }

    suspend fun updateSetting(name: String, value: Any): Boolean {
        return try {
            val settings = connectionSettings.value
            val stringValue = value.toString()
            val url = "http://${settings.ipAddress}:${settings.httpPort}/control?var=$name&val=$stringValue"
            val response = httpClient.get(url)
            val success = response.status.value in 200..299
            if (success) {
                addLog("更新设置成功: $name = $stringValue")
                // 更新本地设置
                updateLocalSetting(name, stringValue)
            } else {
                addLog("更新设置失败: ${response.status}")
                _lastError.value = "更新设置失败: ${response.status}"
            }
            success
        } catch (e: Exception) {
            _lastError.value = "更新设置错误: ${e.message}"
            addLog("更新设置错误: ${e.message}")
            false
        }
    }

    private fun updateLocalSetting(name: String, value: Any) {
        val currentSettings = _cameraSettings.value
        val newSettings = when (name) {
            "framesize" -> currentSettings.copy(framesize = value.toString().toInt())
            "quality" -> currentSettings.copy(quality = value.toString().toInt())
            "brightness" -> currentSettings.copy(brightness = value.toString().toInt())
            "contrast" -> currentSettings.copy(contrast = value.toString().toInt())
            "saturation" -> currentSettings.copy(saturation = value.toString().toInt())
            "special_effect" -> currentSettings.copy(specialEffect = value.toString().toInt())
            "awb" -> currentSettings.copy(awb = value.toString() == "1")
            "awb_gain" -> currentSettings.copy(awbGain = value.toString() == "1")
            "wb_mode" -> currentSettings.copy(wbMode = value.toString().toInt())
            "aec" -> currentSettings.copy(aec = value.toString() == "1")
            "aec2" -> currentSettings.copy(aec2 = value.toString() == "1")
            "ae_level" -> currentSettings.copy(aeLevel = value.toString().toInt())
            "aec_value" -> currentSettings.copy(aecValue = value.toString().toInt())
            "agc" -> currentSettings.copy(agc = value.toString() == "1")
            "agc_gain" -> currentSettings.copy(agcGain = value.toString().toInt())
            "gainceiling" -> currentSettings.copy(gainceiling = value.toString().toInt())
            "bpc" -> currentSettings.copy(bpc = value.toString() == "1")
            "wpc" -> currentSettings.copy(wpc = value.toString() == "1")
            "raw_gma" -> currentSettings.copy(rawGma = value.toString() == "1")
            "lenc" -> currentSettings.copy(lenc = value.toString() == "1")
            "hmirror" -> currentSettings.copy(hmirror = value.toString() == "1")
            "vflip" -> currentSettings.copy(vflip = value.toString() == "1")
            "dcw" -> currentSettings.copy(dcw = value.toString() == "1")
            "colorbar" -> currentSettings.copy(colorbar = value.toString() == "1")
            "rotate" -> currentSettings.copy(rotate = value.toString().toInt())
            "min_frame_time" -> currentSettings.copy(minFrameTime = value.toString().toInt())
            "lamp" -> currentSettings.copy(lamp = value.toString().toInt())
            "autolamp" -> currentSettings.copy(autoLamp = value.toString() == "1")
            else -> currentSettings
        }
        _cameraSettings.value = newSettings
    }

    suspend fun captureStillImage(): Bitmap? {
        return try {
            val settings = connectionSettings.value
            val url = "http://${settings.ipAddress}:${settings.httpPort}/capture"
            val response = httpClient.get(url)
            if (response.status.value in 200..299) {
                val inputStream = response.bodyAsChannel().toInputStream()
                val byteArrayOutputStream = ByteArrayOutputStream()
                val buffer = ByteArray(1024)
                var bytesRead: Int
                while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                    byteArrayOutputStream.write(buffer, 0, bytesRead)
                }
                val byteArray = byteArrayOutputStream.toByteArray()
                val bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
                addLog("图像捕获成功")
                bitmap
            } else {
                addLog("图像捕获失败: ${response.status}")
                _lastError.value = "图像捕获失败: ${response.status}"
                null
            }
        } catch (e: Exception) {
            _lastError.value = "图像捕获错误: ${e.message}"
            addLog("图像捕获错误: ${e.message}")
            null
        }
    }

    // 获取视频流URL
    fun getStreamUrl(): String {
        val settings = connectionSettings.value
        // 使用用户设置的视频流端口
        return "http://${settings.ipAddress}:${settings.streamPort}/"
    }

    // 获取摄像头详细信息
    suspend fun getCameraInfo(): String {
        return try {
            val settings = connectionSettings.value
            val url = "http://${settings.ipAddress}:${settings.httpPort}/dump"
            val response = httpClient.get(url)
            if (response.status.value in 200..299) {
                val info = response.bodyAsText()
                addLog("获取摄像头信息成功")
                info
            } else {
                addLog("获取摄像头信息失败: ${response.status}")
                _lastError.value = "获取摄像头信息失败: ${response.status}"
                "获取摄像头信息失败"
            }
        } catch (e: Exception) {
            _lastError.value = "获取摄像头信息错误: ${e.message}"
            addLog("获取摄像头信息错误: ${e.message}")
            "获取摄像头信息错误: ${e.message}"
        }
    }
    
    // 刷新所有摄像头设置
    suspend fun refreshCameraSettings(): Boolean {
        return testConnection()
    }
    
    // 恢复默认设置
    suspend fun restoreDefaultSettings(): Boolean {
        return try {
            val settings = connectionSettings.value
            val url = "http://${settings.ipAddress}:${settings.httpPort}/control?var=default_settings&val=1"
            val response = httpClient.get(url)
            val success = response.status.value in 200..299
            if (success) {
                addLog("恢复默认设置成功")
                // 刷新本地设置
                testConnection()
            } else {
                addLog("恢复默认设置失败: ${response.status}")
                _lastError.value = "恢复默认设置失败: ${response.status}"
            }
            success
        } catch (e: Exception) {
            _lastError.value = "恢复默认设置错误: ${e.message}"
            addLog("恢复默认设置错误: ${e.message}")
            false
        }
    }

    fun addLog(message: String) {
        val timestamp = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())
        val newLog = "[$timestamp] $message"
        _operationLogs.value = _operationLogs.value.toMutableList().apply {
            add(0, newLog)
            if (size > 100) {
                removeAt(size - 1)
            }
        }
    }

    fun reboot() {
        try {
            val settings = connectionSettings.value
            val url = URL("http://${settings.ipAddress}:${settings.httpPort}/control?var=reboot&val=1")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 5000
            connection.connect()
            
            val responseCode = connection.responseCode
            if (responseCode in 200..299) {
                addLog("重启命令已发送")
            } else {
                addLog("重启命令发送失败: $responseCode")
                _lastError.value = "重启命令发送失败: $responseCode"
            }
            connection.disconnect()
        } catch (e: Exception) {
            _lastError.value = "重启命令错误: ${e.message}"
            addLog("重启命令错误: ${e.message}")
        }
    }

    fun savePreferences() {
        try {
            val settings = connectionSettings.value
            val url = URL("http://${settings.ipAddress}:${settings.httpPort}/control?var=save_prefs&val=1")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 5000
            connection.connect()
            
            val responseCode = connection.responseCode
            if (responseCode in 200..299) {
                addLog("保存偏好设置命令已发送")
            } else {
                addLog("保存偏好设置命令发送失败: $responseCode")
                _lastError.value = "保存偏好设置命令发送失败: $responseCode"
            }
            connection.disconnect()
        } catch (e: Exception) {
            _lastError.value = "保存偏好设置命令错误: ${e.message}"
            addLog("保存偏好设置命令错误: ${e.message}")
        }
    }

    fun clearPreferences() {
        try {
            val settings = connectionSettings.value
            val url = URL("http://${settings.ipAddress}:${settings.httpPort}/control?var=clear_prefs&val=1")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 5000
            connection.connect()
            
            val responseCode = connection.responseCode
            if (responseCode in 200..299) {
                addLog("清除偏好设置命令已发送")
            } else {
                addLog("清除偏好设置命令发送失败: $responseCode")
                _lastError.value = "清除偏好设置命令发送失败: $responseCode"
            }
            connection.disconnect()
        } catch (e: Exception) {
            _lastError.value = "清除偏好设置命令错误: ${e.message}"
            addLog("清除偏好设置命令错误: ${e.message}")
        }
    }

    fun clearLogs() {
        _operationLogs.value = emptyList()
    }

    fun clearError() {
        _lastError.value = null
    }

    // 简单调用端点，返回是否成功
    suspend fun callSimpleEndpoint(url: String): Boolean {
        return try {
            val response = httpClient.get(url)
            val success = response.status.value in 200..299
            if (!success) {
                _lastError.value = "请求失败: ${response.status}"
            }
            success
        } catch (e: Exception) {
            _lastError.value = "请求错误: ${e.message}"
            false
        }
    }
}