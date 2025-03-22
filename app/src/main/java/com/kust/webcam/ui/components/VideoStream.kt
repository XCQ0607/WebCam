package com.kust.webcam.ui.components

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kust.webcam.utils.MjpegStreamProcessor
import com.kust.webcam.utils.OpenCVHelper
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest

@Composable
fun VideoStreamView(
    streamUrl: String,
    isStreaming: Boolean,
    rotation: Int,
    modifier: Modifier = Modifier
) {
    val streamProcessor = remember { MjpegStreamProcessor() }
    var currentFrame by remember { mutableStateOf<Bitmap?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    
    // 获取上下文用于OpenCV处理
    val context = LocalContext.current
    val openCVHelper = remember { OpenCVHelper(context) }
    
    LaunchedEffect(isStreaming, streamUrl) {
        if (isStreaming && streamUrl.isNotEmpty()) {
            error = null
            isLoading = true
            
            streamProcessor.startStream(streamUrl)
                .catch { e ->
                    error = e.message
                    isLoading = false
                }
                .collectLatest { frame ->
                    currentFrame = frame
                    isLoading = false
                }
        } else {
            streamProcessor.stopStream()
            if (isStreaming) {
                error = "流URL为空"
            }
        }
    }
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(4f / 3f)
            .clip(RoundedCornerShape(8.dp))
            .background(Color.Black)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.Center
    ) {
        if (isStreaming) {
            if (isLoading) {
                CircularProgressIndicator(color = Color.White)
            } else if (error != null) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "视频流错误",
                        color = Color.White,
                        fontSize = 18.sp
                    )
                    Text(
                        error ?: "未知错误",
                        color = Color.Red,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            } else if (currentFrame != null) {
                val displayBitmap = if (rotation != 0 && openCVHelper.isInitialized()) {
                    // 使用OpenCV进行图像旋转
                    openCVHelper.rotateImage(currentFrame!!, rotation)
                } else {
                    // 回退到普通的Bitmap显示
                    currentFrame
                }
                
                displayBitmap?.let {
                    Image(
                        bitmap = it.asImageBitmap(),
                        contentDescription = "视频流",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.Center)
                    )
                }
            }
        } else {
            Text(
                "视频流未启动",
                color = Color.White,
                fontSize = 18.sp
            )
        }
    }
}

@Composable
fun StillImageView(
    bitmap: Bitmap?,
    rotation: Int,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(4f / 3f)
            .clip(RoundedCornerShape(8.dp))
            .background(Color.Black)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.Center
    ) {
        if (bitmap != null) {
            val rotationDegrees = when (rotation) {
                90 -> 90f
                -90 -> 270f
                else -> 0f
            }
            
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = "静态图像",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.Center)
            )
        } else {
            Text(
                "没有捕获的图像",
                color = Color.White,
                fontSize = 18.sp
            )
        }
    }
} 