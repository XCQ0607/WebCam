package com.kust.webcam.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Matrix
import android.graphics.Paint
import android.util.Log
import androidx.core.graphics.applyCanvas
import kotlin.math.roundToInt

/**
 * 图像处理工具类，使用Android原生API代替OpenCV
 */
class OpenCVHelper(private val context: Context) {
    private val tag = "ImageProcessor"
    
    // 图像处理器始终可用
    private var isInitialized = true
    
    init {
        Log.d(tag, "图像处理器初始化")
    }
    
    // 初始化图像处理器
    fun initAsync() {
        Log.i(tag, "图像处理器已初始化")
    }
    
    // 获取版本信息
    fun getOpenCVVersion(): String {
        return "使用Android原生图像处理"
    }
    
    // 检查是否初始化
    fun isInitialized(): Boolean = isInitialized
    
    // 图像处理功能：旋转图像
    fun rotateImage(bitmap: Bitmap, angle: Int): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(angle.toFloat())
        
        return Bitmap.createBitmap(
            bitmap, 0, 0,
            bitmap.width, bitmap.height,
            matrix, true
        )
    }
    
    // 图像处理功能：调整对比度和亮度
    fun adjustBrightnessContrast(bitmap: Bitmap, brightness: Double, contrast: Double): Bitmap {
        // 创建一个可变位图
        val resultBitmap = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(resultBitmap)
        
        // 创建色彩矩阵
        val colorMatrix = ColorMatrix()
        
        // 设置对比度
        colorMatrix.set(floatArrayOf(
            contrast.toFloat(), 0f, 0f, 0f, 0f,
            0f, contrast.toFloat(), 0f, 0f, 0f,
            0f, 0f, contrast.toFloat(), 0f, 0f,
            0f, 0f, 0f, 1f, 0f
        ))
        
        // 设置亮度
        val brightnessMatrix = ColorMatrix()
        brightnessMatrix.set(floatArrayOf(
            1f, 0f, 0f, 0f, brightness.toFloat() * 255,
            0f, 1f, 0f, 0f, brightness.toFloat() * 255,
            0f, 0f, 1f, 0f, brightness.toFloat() * 255,
            0f, 0f, 0f, 1f, 0f
        ))
        
        // 合并两个矩阵效果
        colorMatrix.postConcat(brightnessMatrix)
        
        // 应用颜色矩阵
        val paint = Paint()
        paint.colorFilter = ColorMatrixColorFilter(colorMatrix)
        
        // 绘制到新位图
        canvas.drawBitmap(bitmap, 0f, 0f, paint)
        
        return resultBitmap
    }
    
    // 图像处理功能：锐化图像
    fun sharpenImage(bitmap: Bitmap): Bitmap {
        // 创建锐化卷积核
        val kernel = floatArrayOf(
            0f, -1f, 0f,
            -1f, 5f, -1f,
            0f, -1f, 0f
        )
        
        // 从原始位图获取像素
        val width = bitmap.width
        val height = bitmap.height
        val pixelCount = width * height
        val pixels = IntArray(pixelCount)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)
        
        // 创建结果位图
        val result = IntArray(pixelCount)
        
        // 应用卷积操作
        for (y in 1 until height - 1) {
            for (x in 1 until width - 1) {
                val index = y * width + x
                
                // 对当前像素的RGB分量应用卷积操作
                var red = 0f
                var green = 0f
                var blue = 0f
                
                for (ky in -1..1) {
                    for (kx in -1..1) {
                        val pixelIndex = (y + ky) * width + (x + kx)
                        val kernelIndex = (ky + 1) * 3 + (kx + 1)
                        
                        val pixel = pixels[pixelIndex]
                        val kernelValue = kernel[kernelIndex]
                        
                        red += ((pixel shr 16) and 0xFF) * kernelValue
                        green += ((pixel shr 8) and 0xFF) * kernelValue
                        blue += (pixel and 0xFF) * kernelValue
                    }
                }
                
                // 将结果限制在0-255范围内
                val r = red.coerceIn(0f, 255f).roundToInt()
                val g = green.coerceIn(0f, 255f).roundToInt()
                val b = blue.coerceIn(0f, 255f).roundToInt()
                
                // 保持原来的alpha值
                val alpha = pixels[index] and 0xFF000000.toInt()
                
                // 组合成ARGB格式
                result[index] = alpha or (r shl 16) or (g shl 8) or b
            }
        }
        
        // 创建结果位图
        val resultBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        resultBitmap.setPixels(result, 0, width, 0, 0, width, height)
        
        return resultBitmap
    }
} 