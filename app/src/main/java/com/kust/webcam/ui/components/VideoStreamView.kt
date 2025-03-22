package com.kust.webcam.ui.components

import android.graphics.Bitmap
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView

@Composable
fun VideoStreamView(streamUrl: String) {
    val isLoading = remember { mutableStateOf(true) }
    val errorMessage = remember { mutableStateOf<String?>(null) }
    val retryTrigger = remember { mutableStateOf(0) }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        // 使用WebView加载MJPEG流
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { context ->
                WebView(context).apply {
                    settings.apply {
                        javaScriptEnabled = true
                        useWideViewPort = true
                        loadWithOverviewMode = true
                        domStorageEnabled = true
                        allowFileAccess = true
                        // 设置WebView的UA为移动设备UA
                        userAgentString = "Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/119.0.0.0 Mobile Safari/537.36"
                    }
                    
                    setWebViewClient(object : WebViewClient() {
                        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                            super.onPageStarted(view, url, favicon)
                            isLoading.value = true
                            errorMessage.value = null
                        }
                        
                        override fun onPageFinished(view: WebView?, url: String?) {
                            super.onPageFinished(view, url)
                            isLoading.value = false
                            
                            // 注入JavaScript来隐藏加载指示器
                            view?.evaluateJavascript(
                                """
                                (function() {
                                    // 立即隐藏所有加载指示器
                                    var hideLoaders = function() {
                                        // 隐藏ID为wait-settings的加载器
                                        var waitSettings = document.getElementById('wait-settings');
                                        if (waitSettings) {
                                            waitSettings.style.display = 'none';
                                        }
                                        
                                        // 隐藏所有.loader类的元素
                                        var loaders = document.getElementsByClassName('loader');
                                        for (var i = 0; i < loaders.length; i++) {
                                            loaders[i].style.display = 'none';
                                        }
                                    };
                                    
                                    // 立即尝试隐藏
                                    hideLoaders();
                                    
                                    // 监听stream元素加载完成
                                    var streamImg = document.getElementById('stream');
                                    if (streamImg) {
                                        streamImg.onload = hideLoaders;
                                    }
                                    
                                    // 定时检查并隐藏加载器，确保一定会被隐藏
                                    var checkInterval = setInterval(function() {
                                        hideLoaders();
                                        // 5秒后停止检查
                                        setTimeout(function() {
                                            clearInterval(checkInterval);
                                        }, 5000);
                                    }, 500);
                                })();
                                """,
                                null
                            )
                            
                            // 延迟2秒后再次检查并隐藏所有加载指示器，确保处理了延迟加载的情况
                            view?.postDelayed({
                                view.evaluateJavascript(
                                    """
                                    (function() {
                                        var waitSettings = document.getElementById('wait-settings');
                                        if (waitSettings) {
                                            waitSettings.style.display = 'none';
                                        }
                                        
                                        var loaders = document.getElementsByClassName('loader');
                                        for (var i = 0; i < loaders.length; i++) {
                                            loaders[i].style.display = 'none';
                                        }
                                    })();
                                    """,
                                    null
                                )
                            }, 2000)
                        }
                        
                        @Deprecated("Deprecated in Java")
                        @Suppress("DEPRECATION")
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
                    })
                    
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
        
        // 显示加载指示器或错误信息
        if (isLoading.value) {
            // 移除加载指示器(转圈)，让页面直接加载视频
            // 此处不再显示CircularProgressIndicator
        } else if (errorMessage.value != null) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .background(Color.Black.copy(alpha = 0.7f))
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = errorMessage.value ?: "",
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Button(
                    onClick = {
                        isLoading.value = true
                        errorMessage.value = null
                        retryTrigger.value += 1 // 触发重载
                    }
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
            .background(Color.Black),
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