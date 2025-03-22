package com.kust.webcam.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Arrangement

@Composable
fun CameraControlButtons(
    isStreaming: Boolean,
    onStartStopStream: () -> Unit,
    onRefreshCamera: () -> Unit,
    onCaptureImage: () -> Unit,
    onSaveToGallery: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        IconButton(
            onClick = onStartStopStream,
            modifier = Modifier
                .size(56.dp)
                .background(MaterialTheme.colorScheme.primary, shape = CircleShape)
                .padding(8.dp)
        ) {
            Icon(
                imageVector = if (isStreaming) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                contentDescription = if (isStreaming) "停止流" else "开始流",
                tint = Color.White,
                modifier = Modifier.size(36.dp)
            )
        }

        IconButton(
            onClick = onRefreshCamera,
            modifier = Modifier
                .size(56.dp)
                .background(MaterialTheme.colorScheme.primary, shape = CircleShape)
                .padding(8.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Refresh,
                contentDescription = "刷新",
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }

        IconButton(
            onClick = onCaptureImage,
            modifier = Modifier
                .size(56.dp)
                .background(MaterialTheme.colorScheme.primary, shape = CircleShape)
                .padding(8.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Camera,
                contentDescription = "拍照",
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }

        IconButton(
            onClick = onSaveToGallery,
            modifier = Modifier
                .size(56.dp)
                .background(MaterialTheme.colorScheme.primary, shape = CircleShape)
                .padding(8.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Save,
                contentDescription = "保存到相册",
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
    }
} 