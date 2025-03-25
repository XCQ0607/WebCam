package com.kust.webcam.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kust.webcam.domain.viewmodel.CameraViewModel
import com.kust.webcam.ui.components.BrightnessContrastControls
import com.kust.webcam.ui.components.CameraInfoDialog
import com.kust.webcam.ui.components.ConnectionSettingsCard
import com.kust.webcam.ui.components.ConnectionStatusCard
import com.kust.webcam.ui.components.FrameDurationLimitSelector
import com.kust.webcam.ui.components.LampControl
import com.kust.webcam.ui.components.LogDisplay
import com.kust.webcam.ui.components.QualitySlider
import com.kust.webcam.ui.components.ResolutionSelector
import com.kust.webcam.ui.components.SettingsDivider
import com.kust.webcam.ui.components.SpecialEffectSelector
import com.kust.webcam.ui.components.SwitchSetting
import com.kust.webcam.ui.components.WhiteBalanceModeSelector
import androidx.documentfile.provider.DocumentFile
import com.kust.webcam.ui.components.FolderPickerDialog
import android.Manifest
import android.provider.Settings
import android.content.pm.PackageManager

@Composable
fun SettingsScreen(viewModel: CameraViewModel = viewModel()) {
    val connectionSettings by viewModel.connectionSettings.collectAsState()
    val connectionStatus by viewModel.connectionStatus.collectAsState()
    val cameraInfo by viewModel.cameraInfo.collectAsState()
    val cameraSettings by viewModel.cameraSettings.collectAsState()
    val lastError by viewModel.lastError.collectAsState()
    val operationLogs by viewModel.operationLogs.collectAsState()
    val saveDirectoryState by viewModel.saveDirectory.collectAsState()
    
    // 滚动位置记忆
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    var scrollPosition by rememberSaveable { mutableStateOf(0) }
    
    // 使用rememberSaveable保存目录信息，这样在配置更改时不会丢失
    var displayedDirectory by rememberSaveable { mutableStateOf(saveDirectoryState) }
    
    // 当离开页面时保存滚动位置
    LaunchedEffect(scrollState.value) {
        scrollPosition = scrollState.value
    }
    
    // 当重新进入页面时恢复滚动位置
    LaunchedEffect(Unit) {
        scrollState.scrollTo(scrollPosition)
    }
    
    // 监听saveDirectoryState的变化并更新UI
    LaunchedEffect(saveDirectoryState) {
        displayedDirectory = saveDirectoryState
    }
    
    // 不需要使用registerForActivityResult，因为我们已经在MainActivity中处理了结果
    // 这里只是显示UI并在点击时调用viewModel的方法
    
    val showCameraInfoDialog = remember { mutableStateOf(false) }
    
    if (showCameraInfoDialog.value && cameraInfo.isNotEmpty()) {
        CameraInfoDialog(
            cameraInfo = cameraInfo,
            onDismiss = { showCameraInfoDialog.value = false }
        )
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState)
    ) {
        // 连接信息与状态
        val savedConnectionsState by viewModel.savedConnections.collectAsState()
        ConnectionSettingsCard(
            connectionSettings = connectionSettings,
            onUpdateSettings = { viewModel.updateConnectionSettings(it) },
            onTestConnection = { viewModel.testConnection() },
            connectionStatus = connectionStatus,
            savedConnections = savedConnectionsState,
            onLoadPreset = { index -> 
                viewModel.loadConnectionPreset(index)
                // 不需要额外的操作，loadConnectionPreset会更新connectionSettings
                // UI会自动响应StateFlow的变化
            },
            onSavePreset = { viewModel.saveCurrentConnectionToPresets() },
            onRestoreDefaults = { viewModel.restoreDefaultSettings() },
            onViewCameraInfo = {
                viewModel.fetchCameraInfo()
                showCameraInfoDialog.value = true
            },
            onDeletePreset = { index ->
                // 创建一个新的预设列表，移除指定索引的预设
                val currentPresets = savedConnectionsState.toMutableList()
                if (index in currentPresets.indices && index > 0) { // 确保不删除默认预设
                    currentPresets.removeAt(index)
                    // 更新ViewModel中的预设列表
                    viewModel._savedConnections.value = currentPresets
                    // 保存更改到本地存储
                    viewModel.savePresetsToPersistentStorage()
                    viewModel.showToast("已删除预设")
                }
            }
        )
        
        // 添加手动加载配置按钮
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "配置加载",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "如果配置未自动加载，请使用下方按钮手动加载配置",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Button(
                    onClick = {
                        // 清空当前日志以便更清晰地查看加载过程
                        viewModel.clearLogs()
                        
                        // 添加明显的日志标记
                        viewModel.addLog("⭐⭐⭐ 开始手动加载配置...")
                        
                        // 先加载预设
                        viewModel.loadPresetsFromPersistentStorage()
                        
                        // 立即加载默认连接
                        viewModel.loadDefaultConnection()
                        
                        // 添加完成日志
                        viewModel.addLog("⭐⭐⭐ 配置手动加载完成")
                        
                        // 显示Toast提示
                        viewModel.showToast("正在加载配置，请查看日志")
                    },
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .padding(horizontal = 16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "加载配置",
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(
                        text = "手动加载配置",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        
        // 控制按钮区域
        if (connectionStatus) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "摄像头控制",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Button(
                            onClick = { viewModel.reboot() },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "重启摄像头",
                                modifier = Modifier.padding(end = 4.dp)
                            )
                            Text("重启摄像头")
                        }
                        
                        Spacer(modifier = Modifier.padding(8.dp))
                        
                        OutlinedButton(
                            onClick = { viewModel.clearPreferences() },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "清除设置",
                                modifier = Modifier.padding(end = 4.dp)
                            )
                            Text("清除设置")
                        }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 添加权限管理卡片到Card展示区域
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "权限管理",
                    style = MaterialTheme.typography.titleMedium
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 检查存储权限
                val hasAllFilesPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    Environment.isExternalStorageManager()
                } else {
                    context.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "文件系统访问权限",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        
                        Text(
                            text = if (hasAllFilesPermission) "已授权" else "未授权",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (hasAllFilesPermission) 
                                MaterialTheme.colorScheme.primary 
                            else 
                                MaterialTheme.colorScheme.error
                        )
                    }
                    
                    Button(
                        onClick = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                                try {
                                    val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                                    intent.addCategory("android.intent.category.DEFAULT")
                                    intent.data = Uri.parse("package:${context.packageName}")
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    // 如果上面的意图失败，打开应用通用设置页面
                                    viewModel.openAppSettings(context)
                                }
                            } else {
                                viewModel.openAppSettings(context)
                            }
                        }
                    ) {
                        Text(if (hasAllFilesPermission) "查看权限" else "请求权限")
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) 
                        "Android 11及以上需要管理所有文件权限来访问自定义目录" 
                    else 
                        "需要存储权限来保存照片到自定义目录",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 保存目录设置
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "存储设置",
                    style = MaterialTheme.typography.titleMedium
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "照片保存目录",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        
                        Text(
                            text = displayedDirectory,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    // 修改这里，使用自定义文件夹选择器
                    var showFolderPicker by remember { mutableStateOf(false) }
                    
                    Button(
                        onClick = { showFolderPicker = true }
                    ) {
                        Text("选择目录")
                    }
                    
                    // 显示自定义文件夹选择器对话框
                    if (showFolderPicker) {
                        FolderPickerDialog(
                            initialPath = displayedDirectory,
                            onFolderSelected = { selectedPath ->
                                // 更新UI显示
                                displayedDirectory = selectedPath
                                // 更新ViewModel
                                viewModel.updateSaveDirectory(selectedPath)
                                // 关闭对话框
                                showFolderPicker = false
                            },
                            onDismiss = { showFolderPicker = false }
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    Text(
                        text = "注意: Android 10及以上版本会将照片保存到应用专用目录",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 添加连接状态卡片
        ConnectionStatusCard(
            connectionStatus = connectionStatus,
            lastError = lastError
        )
        
        // 添加日志显示
        LogDisplay(
            logs = operationLogs,
            onClearLogs = { viewModel.clearLogs() }
        )
        
        if (connectionStatus) {
            // 分辨率选择器
            ResolutionSelector(
                selectedResolution = cameraSettings.framesize,
                onResolutionSelected = { viewModel.updateSetting("framesize", it) }
            )
            
            SettingsDivider()
            
            // 画质控制
            QualitySlider(
                quality = cameraSettings.quality,
                onQualityChanged = { viewModel.updateSetting("quality", it) }
            )
            
            SettingsDivider()
            
            // 灯光控制
            LampControl(
                lampValue = cameraSettings.lamp,
                isAutoLamp = cameraSettings.autoLamp,
                onLampValueChanged = { viewModel.updateSetting("lamp", it) },
                onAutoLampChanged = { viewModel.updateSetting("autolamp", if (it) 1 else 0) }
            )
            
            SettingsDivider()
            
            // 亮度、对比度、饱和度控制
            BrightnessContrastControls(
                brightness = cameraSettings.brightness,
                contrast = cameraSettings.contrast,
                saturation = cameraSettings.saturation,
                onBrightnessChanged = { viewModel.updateSetting("brightness", it) },
                onContrastChanged = { viewModel.updateSetting("contrast", it) },
                onSaturationChanged = { viewModel.updateSetting("saturation", it) }
            )
            
            SettingsDivider()
            
            // 特效选择器
            SpecialEffectSelector(
                selectedEffect = cameraSettings.specialEffect,
                onEffectSelected = { viewModel.updateSetting("special_effect", it) }
            )
            
            SettingsDivider()
            
            // 白平衡
            SwitchSetting(
                title = "自动白平衡",
                isChecked = cameraSettings.awb,
                onCheckedChange = { viewModel.updateSetting("awb", if (it) 1 else 0) }
            )
            
            SwitchSetting(
                title = "手动白平衡增益",
                isChecked = cameraSettings.awbGain,
                onCheckedChange = { viewModel.updateSetting("awb_gain", if (it) 1 else 0) }
            )
            
            WhiteBalanceModeSelector(
                selectedMode = cameraSettings.wbMode,
                isEnabled = cameraSettings.awbGain,
                onModeSelected = { viewModel.updateSetting("wb_mode", it) }
            )
            
            SettingsDivider()
            
            // 曝光控制
            SwitchSetting(
                title = "AEC传感器",
                isChecked = cameraSettings.aec,
                onCheckedChange = { viewModel.updateSetting("aec", if (it) 1 else 0) }
            )
            
            SwitchSetting(
                title = "AEC DSP",
                isChecked = cameraSettings.aec2,
                onCheckedChange = { viewModel.updateSetting("aec2", if (it) 1 else 0) }
            )
            
            SettingsDivider()
            
            // 增益控制
            SwitchSetting(
                title = "AGC",
                isChecked = cameraSettings.agc,
                onCheckedChange = { viewModel.updateSetting("agc", if (it) 1 else 0) }
            )
            
            SettingsDivider()
            
            // 图像处理
            SwitchSetting(
                title = "BPC",
                isChecked = cameraSettings.bpc,
                onCheckedChange = { viewModel.updateSetting("bpc", if (it) 1 else 0) }
            )
            
            SwitchSetting(
                title = "WPC",
                isChecked = cameraSettings.wpc,
                onCheckedChange = { viewModel.updateSetting("wpc", if (it) 1 else 0) }
            )
            
            SwitchSetting(
                title = "Raw GMA",
                isChecked = cameraSettings.rawGma,
                onCheckedChange = { viewModel.updateSetting("raw_gma", if (it) 1 else 0) }
            )
            
            SwitchSetting(
                title = "镜头校正",
                isChecked = cameraSettings.lenc,
                onCheckedChange = { viewModel.updateSetting("lenc", if (it) 1 else 0) }
            )
            
            SettingsDivider()
            
            // 镜像翻转
            SwitchSetting(
                title = "水平镜像",
                isChecked = cameraSettings.hmirror,
                onCheckedChange = { viewModel.updateSetting("hmirror", if (it) 1 else 0) }
            )
            
            SwitchSetting(
                title = "垂直翻转",
                isChecked = cameraSettings.vflip,
                onCheckedChange = { viewModel.updateSetting("vflip", if (it) 1 else 0) }
            )
            
            SettingsDivider()
            
            // 其他设置
            SwitchSetting(
                title = "DCW (缩小启用)",
                isChecked = cameraSettings.dcw,
                onCheckedChange = { viewModel.updateSetting("dcw", if (it) 1 else 0) }
            )
            
            SwitchSetting(
                title = "测试模式",
                isChecked = cameraSettings.colorbar,
                onCheckedChange = { viewModel.updateSetting("colorbar", if (it) 1 else 0) }
            )
            
            SettingsDivider()
            
            // 帧率限制
            FrameDurationLimitSelector(
                selectedDuration = cameraSettings.minFrameTime,
                onDurationSelected = { viewModel.updateSetting("min_frame_time", it) }
            )
        }
    }
}