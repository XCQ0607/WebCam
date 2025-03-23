package com.kust.webcam.ui.screens

import android.Manifest
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kust.webcam.domain.viewmodel.CameraViewModel
import com.kust.webcam.ui.components.CameraControlButtons
import com.kust.webcam.ui.components.PermissionDialog
import com.kust.webcam.ui.components.StillImageView
import com.kust.webcam.ui.components.StoragePermissionTextProvider
import com.kust.webcam.ui.components.VideoStreamView

@Composable
fun LiveStreamScreen(viewModel: CameraViewModel = viewModel()) {
    val connectionStatus by viewModel.connectionStatus.collectAsState()
    val isStreaming by viewModel.isStreaming.collectAsState()
    val capturedImage by viewModel.capturedImage.collectAsState()
    val toastMessage by viewModel.toastMessage.collectAsState()
    val context = LocalContext.current
    
    // 添加是否显示内容的状态，用于添加进入动画
    var contentVisible by remember { mutableStateOf(false) }
    
    // 权限相关
    var showPermissionDialog by remember { mutableStateOf(false) }
    var permissionDeniedPermanently by remember { mutableStateOf(false) }
    
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                viewModel.saveImageToGallery(context)
            } else {
                // 检查是否永久拒绝了权限
                permissionDeniedPermanently = !shouldShowPermissionRationale(context)
                showPermissionDialog = true
            }
        }
    )
    
    // 在组件首次渲染后，将内容设置为可见
    LaunchedEffect(Unit) {
        contentVisible = true
    }
    
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
    
    // 权限对话框
    if (showPermissionDialog) {
        PermissionDialog(
            permissionTextProvider = StoragePermissionTextProvider(),
            isPermanentlyDeclined = permissionDeniedPermanently,
            onDismiss = { showPermissionDialog = false },
            onOkClick = {
                permissionLauncher.launch(
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        Manifest.permission.READ_MEDIA_IMAGES
                    } else {
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    }
                )
                showPermissionDialog = false
            },
            onGoToAppSettingsClick = {
                openAppSettings(context)
                showPermissionDialog = false
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        AnimatedVisibility(
            visible = contentVisible,
            enter = fadeIn(animationSpec = tween(500)) + 
                   slideInVertically(
                       animationSpec = tween(500),
                       initialOffsetY = { it / 2 }
                   ),
            exit = fadeOut()
        ) {
            Card(
                modifier = Modifier
                    .fillMaxSize(),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (connectionStatus) {
                        if (isStreaming) {
                            VideoStreamView(streamUrl = viewModel.getStreamUrl())
                        } else if (capturedImage != null) {
                            StillImageView(bitmap = capturedImage!!)
                        } else {
                            Text(
                                "连接成功 - 点击\"开始视频\"按钮开始播放",
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    } else {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                "未连接到摄像头",
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "请先在连接配置页面设置连接",
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                    
                    // 控制按钮，放在画面底部
                    if (connectionStatus) {
                        Card(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 8.dp)
                                .fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
                            ),
                            shape = RoundedCornerShape(28.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceEvenly
                            ) {
                                // 视频流按钮
                                ControlButton(
                                    onClick = { viewModel.toggleStream() },
                                    icon = if (isStreaming) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                                    label = if (isStreaming) "停止视频" else "开始视频",
                                    color = if (isStreaming) 
                                        MaterialTheme.colorScheme.error 
                                    else 
                                        MaterialTheme.colorScheme.primary
                                )
                                
                                Spacer(modifier = Modifier.width(24.dp))
                                
                                // 拍照按钮
                                ControlButton(
                                    onClick = { viewModel.captureStillImage() },
                                    icon = Icons.Filled.Camera,
                                    label = "拍照",
                                    color = MaterialTheme.colorScheme.secondary
                                )
                                
                                Spacer(modifier = Modifier.width(24.dp))
                                
                                // 保存到相册按钮
                                ControlButton(
                                    onClick = { 
                                        if (hasExternalStoragePermission(context)) {
                                            viewModel.saveImageToGallery(context)
                                        } else {
                                            permissionLauncher.launch(
                                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                                    Manifest.permission.READ_MEDIA_IMAGES
                                                } else {
                                                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                                                }
                                            )
                                        }
                                    },
                                    icon = Icons.Filled.Save,
                                    label = "保存",
                                    color = MaterialTheme.colorScheme.tertiary,
                                    enabled = capturedImage != null
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ControlButton(
    onClick: () -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    color: Color,
    enabled: Boolean = true
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        IconButton(
            onClick = onClick,
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = if (enabled) 1f else 0.5f)),
            enabled = enabled
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = Color.White,
                modifier = Modifier.size(28.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = if (enabled) 1f else 0.5f)
        )
    }
}

// 辅助函数，检查是否有外部存储权限
private fun hasExternalStoragePermission(context: android.content.Context): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        androidx.core.content.ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_MEDIA_IMAGES
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
    } else {
        androidx.core.content.ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
    }
}

// 检查是否需要显示权限解释
private fun shouldShowPermissionRationale(context: android.content.Context): Boolean {
    return if (context is androidx.activity.ComponentActivity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale(
                context,
                Manifest.permission.READ_MEDIA_IMAGES
            )
        } else {
            androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale(
                context,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        }
    } else false
}

// 打开应用设置页面
private fun openAppSettings(context: android.content.Context) {
    val intent = android.content.Intent(
        android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
        android.net.Uri.fromParts("package", context.packageName, null)
    )
    intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
    context.startActivity(intent)
} 