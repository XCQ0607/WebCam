package com.kust.webcam.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.io.BufferedInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.atomic.AtomicBoolean

class MjpegStreamProcessor {
    private val isRunning = AtomicBoolean(false)
    private val tag = "MjpegStreamProcessor"

    fun startStream(streamUrl: String): Flow<Bitmap> = flow {
        isRunning.set(true)
        var connection: HttpURLConnection? = null
        var inputStream: BufferedInputStream? = null

        try {
            Log.d(tag, "Starting stream from URL: $streamUrl")
            val url = URL(streamUrl)
            connection = url.openConnection() as HttpURLConnection
            connection.connectTimeout = 10000
            connection.readTimeout = 10000
            connection.connect()

            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                Log.d(tag, "Connection OK, content type: ${connection.contentType}")
                inputStream = BufferedInputStream(connection.inputStream, 8192)
                
                while (isRunning.get()) {
                    val bitmap = readMjpegFrameSimple(inputStream)
                    if (bitmap != null) {
                        emit(bitmap)
                    } else {
                        // 如果帧读取失败，等待一下再重试
                        kotlinx.coroutines.delay(100)
                    }
                }
            } else {
                val errorMsg = "HTTP Error: ${connection.responseCode} - ${connection.responseMessage}"
                Log.e(tag, errorMsg)
                throw Exception(errorMsg)
            }
        } catch (e: Exception) {
            Log.e(tag, "Stream Error: ${e.message}", e)
            throw e
        } finally {
            inputStream?.close()
            connection?.disconnect()
            isRunning.set(false)
            Log.d(tag, "Stream stopped")
        }
    }.flowOn(Dispatchers.IO)

    // 简化版的MJPEG帧读取方法，直接处理ESP32-CAM的MJPEG流
    private fun readMjpegFrameSimple(inputStream: InputStream): Bitmap? {
        return try {
            // 查找JPEG帧的开始标记
            val startMarker = 0xFF
            val secondMarker = 0xD8
            
            var currByte = inputStream.read()
            while (currByte != -1) {
                if (currByte == startMarker) {
                    currByte = inputStream.read()
                    if (currByte == secondMarker) {
                        // 找到JPEG开始标记，读取整个JPEG帧
                        val imageData = ByteArrayOutputStream()
                        // 写入开始标记
                        imageData.write(startMarker)
                        imageData.write(secondMarker)
                        
                        // 查找JPEG结束标记 (FFD9)
                        var prev = 0
                        var curr: Int
                        while (true) {
                            curr = inputStream.read()
                            if (curr == -1) break
                            
                            imageData.write(curr)
                            
                            if (prev == 0xFF && curr == 0xD9) {
                                // 找到结束标记，解码图像
                                val jpegData = imageData.toByteArray()
                                return BitmapFactory.decodeByteArray(jpegData, 0, jpegData.size)
                            }
                            
                            prev = curr
                        }
                    }
                }
                currByte = inputStream.read()
            }
            null
        } catch (e: Exception) {
            Log.e(tag, "Frame read error: ${e.message}", e)
            null
        }
    }

    // 保留以前的方法作为备用
    private fun readMjpegFrame(inputStream: InputStream): Bitmap? {
        return try {
            val boundary = findBoundary(inputStream)
            if (boundary == null) {
                Log.e(tag, "No boundary found")
                return null
            }

            // 读取内容长度
            val contentLength = readContentLength(inputStream)
            if (contentLength <= 0) {
                Log.e(tag, "Invalid content length: $contentLength")
                return null
            }

            // 跳过剩余头部直到空行
            skipUntilEmptyLine(inputStream)

            // 读取JPEG数据
            val imageData = ByteArrayOutputStream()
            val buffer = ByteArray(1024)
            var bytesRead = 0
            var totalRead = 0

            while (totalRead < contentLength) {
                bytesRead = inputStream.read(buffer, 0, Math.min(buffer.size, contentLength - totalRead))
                if (bytesRead == -1) break
                imageData.write(buffer, 0, bytesRead)
                totalRead += bytesRead
            }

            // 解码JPEG为Bitmap
            val jpegData = imageData.toByteArray()
            BitmapFactory.decodeByteArray(jpegData, 0, jpegData.size)
        } catch (e: Exception) {
            Log.e(tag, "Frame read error: ${e.message}")
            null
        }
    }

    private fun findBoundary(inputStream: InputStream): String? {
        val buffer = ByteArray(256)
        val read = inputStream.read(buffer, 0, buffer.size)
        if (read <= 0) return null

        val data = String(buffer, 0, read)
        val boundaryIndex = data.indexOf("--")
        if (boundaryIndex == -1) return null

        val newlineIndex = data.indexOf("\r\n", boundaryIndex)
        if (newlineIndex == -1) return null

        return data.substring(boundaryIndex, newlineIndex)
    }

    private fun readContentLength(inputStream: InputStream): Int {
        val headers = StringBuilder()
        val buffer = ByteArray(128)
        var line: String
        var read: Int

        // 读取所有头部
        while (true) {
            read = inputStream.read(buffer, 0, buffer.size)
            if (read <= 0) break

            line = String(buffer, 0, read)
            headers.append(line)

            if (line.contains("\r\n\r\n")) break
        }

        // 查找Content-Length
        val headerStr = headers.toString()
        val contentLengthStr = "Content-Length:"
        val start = headerStr.indexOf(contentLengthStr)
        if (start == -1) return -1

        val end = headerStr.indexOf("\r\n", start)
        if (end == -1) return -1

        return try {
            headerStr.substring(start + contentLengthStr.length, end).trim().toInt()
        } catch (e: NumberFormatException) {
            -1
        }
    }

    private fun skipUntilEmptyLine(inputStream: InputStream) {
        val buffer = ByteArray(1)
        var lastByte = -1
        var currentByte: Int

        while (true) {
            currentByte = inputStream.read(buffer, 0, 1)
            if (currentByte == -1) break

            if (lastByte == 13 && buffer[0].toInt() == 10) { // \r\n
                currentByte = inputStream.read(buffer, 0, 1)
                if (currentByte == -1) break

                if (buffer[0].toInt() == 13) {
                    currentByte = inputStream.read(buffer, 0, 1)
                    if (currentByte == -1) break

                    if (buffer[0].toInt() == 10) {
                        // 找到了空行 (\r\n\r\n)
                        break
                    }
                }
            }

            lastByte = buffer[0].toInt()
        }
    }

    fun stopStream() {
        isRunning.set(false)
    }
} 