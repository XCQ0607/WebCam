package com.kust.webcam.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kust.webcam.domain.viewmodel.CameraViewModel
import com.kust.webcam.ui.components.BrightnessContrastControls
import com.kust.webcam.ui.components.FrameDurationLimitSelector
import com.kust.webcam.ui.components.LampControl
import com.kust.webcam.ui.components.QualitySlider
import com.kust.webcam.ui.components.ResolutionSelector
import com.kust.webcam.ui.components.SettingsDivider
import com.kust.webcam.ui.components.SpecialEffectSelector
import com.kust.webcam.ui.components.SwitchSetting
import com.kust.webcam.ui.components.WhiteBalanceModeSelector
import com.kust.webcam.ui.components.CameraInfoDialog
import com.kust.webcam.ui.components.ConnectionSettingsCard
import com.kust.webcam.ui.components.ConnectionStatusCard
import com.kust.webcam.ui.components.LogDisplay

@Composable
fun SettingsScreen(viewModel: CameraViewModel = viewModel()) {
    val connectionSettings by viewModel.connectionSettings.collectAsState()
    val connectionStatus by viewModel.connectionStatus.collectAsState()
    val cameraInfo by viewModel.cameraInfo.collectAsState()
    val cameraSettings by viewModel.cameraSettings.collectAsState()
    val lastError by viewModel.lastError.collectAsState()
    val operationLogs by viewModel.operationLogs.collectAsState()
    
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
            .verticalScroll(rememberScrollState())
    ) {
        ConnectionSettingsCard(
            connectionSettings = connectionSettings,
            onUpdateSettings = { viewModel.updateConnectionSettings(it) },
            onTestConnection = { viewModel.testConnection() },
            connectionStatus = connectionStatus,
            onRestoreDefaults = { viewModel.restoreDefaultSettings() },
            onViewCameraInfo = {
                viewModel.fetchCameraInfo()
                showCameraInfoDialog.value = true
            }
        )
        
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