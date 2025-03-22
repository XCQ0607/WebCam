package com.kust.webcam.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kust.webcam.domain.viewmodel.CameraViewModel
import com.kust.webcam.ui.components.CameraControlButtons
import com.kust.webcam.ui.components.StillImageView
import com.kust.webcam.ui.components.VideoStreamView
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.DisposableEffect

@Composable
fun LiveStreamScreen(viewModel: CameraViewModel = viewModel()) {
    val connectionStatus by viewModel.connectionStatus.collectAsState()
    val isStreaming by viewModel.isStreaming.collectAsState()
    val capturedImage by viewModel.capturedImage.collectAsState()
    val toastMessage by viewModel.toastMessage.collectAsState()
    val context = LocalContext.current
    
    // 添加DisposableEffect用于在用户离开屏幕时停止视频流
    DisposableEffect(key1 = viewModel) {
        onDispose {
            if (isStreaming) {
                viewModel.stopStreamSilently()
            }
        }
    }
    
    LaunchedEffect(toastMessage) {
        toastMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearToastMessage()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            if (connectionStatus) {
                if (isStreaming) {
                    VideoStreamView(streamUrl = viewModel.getStreamUrl())
                } else if (capturedImage != null) {
                    StillImageView(bitmap = capturedImage!!)
                } else {
                    Text("连接成功 - 点击\"开始视频\"按钮开始播放")
                }
            } else {
                Text("未连接到摄像头 - 请先在连接配置页面设置连接")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        CameraControlButtons(
            onCapture = { viewModel.captureStillImage() },
            onToggleStream = { viewModel.toggleStream() },
            isStreaming = isStreaming,
            onReboot = { viewModel.reboot() },
            onSavePreferences = { viewModel.savePreferences() },
            onClearPreferences = { viewModel.clearPreferences() },
            onSaveToGallery = { viewModel.saveImageToGallery(context) }
        )
    }
} 