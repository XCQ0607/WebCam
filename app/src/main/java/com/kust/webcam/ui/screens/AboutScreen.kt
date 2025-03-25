package com.kust.webcam.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kust.webcam.domain.viewmodel.CameraViewModel
import com.kust.webcam.ui.components.ConnectionStatusCard

@Composable
fun AboutScreen(viewModel: CameraViewModel = viewModel()) {
    val connectionStatus by viewModel.connectionStatus.collectAsState()
    val lastError by viewModel.lastError.collectAsState()
    val cameraInfo by viewModel.cameraInfo.collectAsState()
    val context = LocalContext.current
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 标题
        Text(
            text = "关于ESP32-CAM WebCam",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        // 作者信息卡片
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "作者信息",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(text = "作者：XCQ")
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Button(
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/XCQ0607"))
                        context.startActivity(intent)
                    }
                ) {
                    Text("访问GitHub主页")
                }
            }
        }
        
        // 软件说明卡片
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "关于此软件",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "我是一名大一学生，对单片机非常感兴趣，因此开发了这款APP。" +
                        "这个项目是我在学习Android开发的过程中完成的，旨在提供一个简单易用的ESP32-CAM控制界面。" +
                        "希望这个应用能够帮助到其他对物联网和嵌入式系统有兴趣的同学。",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Justify
                )
            }
        }
        
        // 摄像头信息卡片（仅在连接成功时显示）
        if (connectionStatus) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "摄像头信息",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    if (cameraInfo.isNotEmpty()) {
                        Text(
                            text = cameraInfo,
                            style = MaterialTheme.typography.bodySmall
                        )
                    } else {
                        Button(
                            onClick = { viewModel.fetchCameraInfo() }
                        ) {
                            Text("获取摄像头信息")
                        }
                    }
                }
            }
        }
        
        // 连接状态卡片
        ConnectionStatusCard(
            connectionStatus = connectionStatus,
            lastError = lastError
        )
        
        // 版本信息
        Spacer(modifier = Modifier.height(16.dp))
        Divider()
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "版本：1.0.2",
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center
        )
        
        Text(
            text = "© 2025 XCQ. 保留所有权利。",
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
} 