package com.kust.webcam.ui.screens

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kust.webcam.domain.viewmodel.CameraViewModel
import com.kust.webcam.ui.components.PermissionDialog
import com.kust.webcam.ui.components.StillImageView
import com.kust.webcam.ui.components.StoragePermissionTextProvider
import com.kust.webcam.ui.components.VideoStreamView
import kotlinx.coroutines.delay

@Composable
fun LiveStreamScreen(viewModel: CameraViewModel = viewModel()) {
    val connectionStatus by viewModel.connectionStatus.collectAsState()
    val isStreaming by viewModel.isStreaming.collectAsState()
    val capturedImage by viewModel.capturedImage.collectAsState()
    val toastMessage by viewModel.toastMessage.collectAsState()
    val operationLogs by viewModel.repository.operationLogs.collectAsState()
    val context = LocalContext.current
    
    // 添加是否显示内容的状态，用于添加进入动画
    var contentVisible by remember { mutableStateOf(false) }
    
    // 添加日志提示状态
    var showLogHint by remember { mutableStateOf(true) }
    
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
        // 10秒后隐藏日志提示
        kotlinx.coroutines.delay(10000)
        showLogHint = false
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
        // 添加日志提示卡片
        AnimatedVisibility(
            visible = showLogHint && operationLogs.isNotEmpty(),
            enter = fadeIn() + slideInVertically { -it },
            exit = fadeOut() + slideOutVertically { -it },
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 8.dp)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp)
                ) {
                    Text(
                        text = "应用日志",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    
                    // 显示最新的3条日志
                    Column(
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        operationLogs.take(3).forEach { log ->
                            Text(
                                text = log,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.9f),
                                modifier = Modifier.padding(vertical = 2.dp)
                            )
                        }
                    }
                    
                    Text(
                        text = "应用日志已记录，请在设置页面查看",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
        
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
                            VideoStreamView(
                                streamUrl = viewModel.getStreamUrl(),
                                isStreaming = isStreaming,
                                rotation = 0
                            )
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
                    
                    // 支持英文和中文的控制按钮标签
                    val streamStartLabel = "开始视频"
                    val streamStopLabel = "停止视频"
                    val captureLabel = "拍照"
                    val saveLabel = "保存"

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
                                    label = if (isStreaming) streamStopLabel else streamStartLabel,
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
                                    label = captureLabel,
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
                                    label = saveLabel,
                                    color = MaterialTheme.colorScheme.tertiary
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
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(horizontal = 8.dp)
    ) {
        IconButton(
            onClick = onClick,
            enabled = enabled,
            modifier = Modifier
                .size(48.dp)
                .background(
                    color = if (enabled) color else color.copy(alpha = 0.5f),
                    shape = CircleShape
                )
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = if (enabled) MaterialTheme.colorScheme.onSurface 
                  else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        )
    }
}

// 检查是否有存储权限
private fun hasExternalStoragePermission(context: Context): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_MEDIA_IMAGES
        ) == PackageManager.PERMISSION_GRANTED
    } else {
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
    }
}

// 打开应用设置
private fun openAppSettings(context: Context) {
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
        data = Uri.fromParts("package", context.packageName, null)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    context.startActivity(intent)
}

// 检查是否应该显示权限说明
private fun shouldShowPermissionRationale(context: Context): Boolean {
    val activity = context as? ComponentActivity ?: return false
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        ActivityCompat.shouldShowRequestPermissionRationale(
            activity,
            Manifest.permission.READ_MEDIA_IMAGES
        )
    } else {
        ActivityCompat.shouldShowRequestPermissionRationale(
            activity,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
    }
} 