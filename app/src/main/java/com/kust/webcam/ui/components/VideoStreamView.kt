package com.kust.webcam.ui.components

import android.graphics.Bitmap
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import kotlinx.coroutines.delay

@Composable
fun VideoStreamView(streamUrl: String) {
    val isLoading = remember { mutableStateOf(true) }
    val errorMessage = remember { mutableStateOf<String?>(null) }
    val retryTrigger = remember { mutableStateOf(0) }
    val contentAlpha by animateFloatAsState(
        targetValue = if (isLoading.value) 0.3f else 1f,
        animationSpec = tween(durationMillis = 500),
        label = "contentAlpha"
    )
    
    // 自动在5秒后隐藏加载指示器，无论流是否实际加载完成
    LaunchedEffect(key1 = Unit) {
        delay(5000)
        isLoading.value = false
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .clip(RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center
    ) {
        // 使用WebView加载MJPEG流
        AndroidView(
            modifier = Modifier
                .fillMaxSize()
                .alpha(contentAlpha),
            factory = { context ->
                WebView(context).apply {
                    // 配置WebView
                    settings.apply {
                        // 启用JavaScript
                        javaScriptEnabled = true
                        
                        // 禁用缓存，确保实时加载
                        cacheMode = android.webkit.WebSettings.LOAD_NO_CACHE
                        
                        // 确保视频自动播放
                        mediaPlaybackRequiresUserGesture = false
                        
                        // 禁用缩放控制
                        builtInZoomControls = false
                        displayZoomControls = false
                        
                        // 设置WebView内容的显示方式
                        loadWithOverviewMode = true
                        useWideViewPort = true
                        
                        // 允许加载混合内容（HTTP和HTTPS）
                        mixedContentMode = android.webkit.WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                        
                        // 允许DOM存储API
                        domStorageEnabled = true
                        
                        // 对于一些ESP32摄像头服务器可能需要特定的UA
                        // userAgentString = "ESP32CamViewer/1.0"
                    }
                    
                    // 设置WebView背景为透明
                    setBackgroundColor(android.graphics.Color.TRANSPARENT)
                    
                    // 设置WebViewClient处理页面加载事件
                    webViewClient = object : WebViewClient() {
                        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                            super.onPageStarted(view, url, favicon)
                            isLoading.value = true
                            errorMessage.value = null
                        }
                        
                        override fun onPageFinished(view: WebView?, url: String?) {
                            super.onPageFinished(view, url)
                            // 页面加载完成后，1秒后隐藏加载指示器
                            // 这给了内容一些时间来实际显示
                            android.os.Handler(context.mainLooper).postDelayed({
                                isLoading.value = false
                            }, 1000)
                        }
                        
                        override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
                            super.onReceivedError(view, request, error)
                            if (request?.isForMainFrame == true) {
                                isLoading.value = false
                                errorMessage.value = "加载视频流失败: ${error?.description}\n请检查网络连接和摄像头状态"
                            }
                        }
                        
                        override fun onReceivedError(view: WebView?, errorCode: Int, description: String?, failingUrl: String?) {
                            super.onReceivedError(view, errorCode, description, failingUrl)
                            isLoading.value = false
                            errorMessage.value = "加载视频流失败: $description\n请检查网络连接和摄像头状态"
                            
                            // 如果直接加载失败，尝试使用iframe方式
                            val iframeHtml = """
                                <!DOCTYPE html>
                                <html style="height: 100%;">
                                <head>
                                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                                    <style>
                                        body { 
                                            margin: 0; 
                                            padding: 0; 
                                            background: #0e0e0e; 
                                            height: 100%;
                                            overflow: hidden;
                                        }
                                        iframe {
                                            border: none;
                                            width: 100%;
                                            height: 100%;
                                            display: block;
                                        }
                                        img {
                                            display: block;
                                            margin: auto;
                                            max-width: 100%;
                                            max-height: 100%;
                                        }
                                    </style>
                                </head>
                                <body>
                                    <!-- 尝试两种方式: iframe和img标签 -->
                                    <iframe id="streamFrame" src="$streamUrl" style="display:none" onerror="showImage()"></iframe>
                                    <img id="streamImg" src="$streamUrl" style="display:none">
                                    
                                    <script>
                                        // 尝试加载iframe
                                        document.getElementById('streamFrame').style.display = 'block';
                                        
                                        // 如果iframe加载失败，显示图像
                                        function showImage() {
                                            document.getElementById('streamFrame').style.display = 'none';
                                            document.getElementById('streamImg').style.display = 'block';
                                        }
                                        
                                        // 3秒后检查是否需要切换到图像模式
                                        setTimeout(function() {
                                            if (document.getElementById('streamFrame').contentDocument.body.innerHTML === '') {
                                                showImage();
                                            }
                                        }, 3000);
                                    </script>
                                </body>
                                </html>
                            """.trimIndent()
                            
                            view?.loadDataWithBaseURL(streamUrl, iframeHtml, "text/html", "UTF-8", null)
                        }
                    }
                    
                    // 直接尝试加载原始视频流URL
                    loadUrl(streamUrl)
                }
            },
            update = { webView ->
                if (retryTrigger.value > 0) {
                    // 重试加载
                    webView.reload()
                }
            }
        )
        
        // 显示加载指示器
        AnimatedVisibility(
            visible = isLoading.value,
            enter = fadeIn(animationSpec = tween(300)),
            exit = fadeOut(animationSpec = tween(300))
        ) {
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .padding(16.dp)
                    .size(48.dp)
            )
        }
        
        // 错误信息动画显示
        AnimatedVisibility(
            visible = errorMessage.value != null,
            enter = fadeIn(animationSpec = tween(300)),
            exit = fadeOut(animationSpec = tween(300))
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .padding(16.dp)
                    .background(
                        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.8f),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(16.dp)
            ) {
                Text(
                    text = errorMessage.value ?: "",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Button(
                    onClick = { 
                        retryTrigger.value = retryTrigger.value + 1
                        errorMessage.value = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text("重试")
                }
            }
        }
    }
}

@Composable
fun StillImageView(bitmap: Bitmap) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .clip(RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center
    ) {
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = "拍摄的静态图像",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit
        )
    }
} 