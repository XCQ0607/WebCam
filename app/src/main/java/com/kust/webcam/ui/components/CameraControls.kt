package com.kust.webcam.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kust.webcam.data.model.CameraSettings
import com.kust.webcam.data.model.FrameDurationLimit
import com.kust.webcam.data.model.FrameSize
import com.kust.webcam.data.model.SpecialEffect
import com.kust.webcam.data.model.WbMode

@Composable
fun CameraControlButtons(
    onCapture: () -> Unit,
    onToggleStream: () -> Unit,
    isStreaming: Boolean,
    onReboot: () -> Unit,
    onSavePreferences: () -> Unit,
    onClearPreferences: () -> Unit,
    onSaveToGallery: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Button(
            onClick = onCapture,
            modifier = Modifier.weight(1f)
        ) {
            Text("拍照")
        }
        
        Spacer(modifier = Modifier.width(8.dp))
        
        Button(
            onClick = onToggleStream,
            modifier = Modifier.weight(1f)
        ) {
            Text(if (isStreaming) "停止视频" else "开始视频")
        }
    }
    
    Spacer(modifier = Modifier.height(8.dp))
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Button(
            onClick = onSaveToGallery,
            modifier = Modifier.weight(1f)
        ) {
            Text("保存到相册")
        }
        
        Spacer(modifier = Modifier.width(8.dp))
        
        Button(
            onClick = onReboot,
            modifier = Modifier.weight(1f)
        ) {
            Text("重启摄像头")
        }
    }

    Spacer(modifier = Modifier.height(8.dp))
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Button(
            onClick = onSavePreferences,
            modifier = Modifier.weight(1f)
        ) {
            Text("保存设置")
        }
        
        Spacer(modifier = Modifier.width(8.dp))
        
        Button(
            onClick = onClearPreferences,
            modifier = Modifier.weight(1f)
        ) {
            Text("清除设置")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResolutionSelector(
    selectedResolution: Int,
    onResolutionSelected: (Int) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val currentFrameSize = FrameSize.fromValue(selectedResolution)
    
    Column(modifier = Modifier.padding(8.dp)) {
        Text("分辨率")
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it }
        ) {
            TextField(
                value = "${currentFrameSize.label} (${currentFrameSize.resolution})",
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
            )
            
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                FrameSize.values().forEach { frameSize ->
                    DropdownMenuItem(
                        text = { Text("${frameSize.label} (${frameSize.resolution})") },
                        onClick = {
                            onResolutionSelected(frameSize.value)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpecialEffectSelector(
    selectedEffect: Int,
    onEffectSelected: (Int) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val currentEffect = SpecialEffect.fromValue(selectedEffect)
    
    Column(modifier = Modifier.padding(8.dp)) {
        Text("特效")
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it }
        ) {
            TextField(
                value = currentEffect.label,
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
            )
            
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                SpecialEffect.values().forEach { effect ->
                    DropdownMenuItem(
                        text = { Text(effect.label) },
                        onClick = {
                            onEffectSelected(effect.value)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun SwitchSetting(
    title: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, modifier = Modifier.weight(1f))
        Switch(
            checked = isChecked,
            onCheckedChange = onCheckedChange
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WhiteBalanceModeSelector(
    selectedMode: Int,
    isEnabled: Boolean,
    onModeSelected: (Int) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val currentMode = WbMode.fromValue(selectedMode)
    
    Column(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
    ) {
        Text("白平衡模式")
        ExposedDropdownMenuBox(
            expanded = expanded && isEnabled,
            onExpandedChange = { if (isEnabled) expanded = it }
        ) {
            TextField(
                value = currentMode.label,
                onValueChange = {},
                readOnly = true,
                enabled = isEnabled,
                trailingIcon = { if (isEnabled) ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
            )
            
            if (isEnabled) {
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    WbMode.values().forEach { mode ->
                        DropdownMenuItem(
                            text = { Text(mode.label) },
                            onClick = {
                                onModeSelected(mode.value)
                                expanded = false
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun LampControl(
    lampValue: Int,
    isAutoLamp: Boolean,
    onLampValueChanged: (Int) -> Unit,
    onAutoLampChanged: (Boolean) -> Unit
) {
    var lampSliderValue by remember { mutableStateOf(lampValue.toFloat()) }
    var autoLampChecked by remember { mutableStateOf(isAutoLamp) }
    
    LaunchedEffect(lampValue) {
        lampSliderValue = lampValue.toFloat()
    }
    
    LaunchedEffect(isAutoLamp) {
        autoLampChecked = isAutoLamp
    }
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "灯光控制",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        // 灯光强度滑块
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "灯光强度: ${lampSliderValue.toInt()}%",
                modifier = Modifier.weight(1f)
            )
            
            Slider(
                value = lampSliderValue,
                onValueChange = { lampSliderValue = it },
                valueRange = 0f..100f,
                steps = 100,
                modifier = Modifier.weight(2f),
                onValueChangeFinished = {
                    onLampValueChanged(lampSliderValue.toInt())
                }
            )
        }
        
        // 自动灯光开关
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "只在摄像头活动时开灯",
                modifier = Modifier.weight(1f)
            )
            
            Switch(
                checked = autoLampChecked,
                onCheckedChange = { 
                    autoLampChecked = it
                    onAutoLampChanged(it)
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FrameDurationLimitSelector(
    selectedDuration: Int,
    onDurationSelected: (Int) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val currentDuration = FrameDurationLimit.fromValue(selectedDuration)
    
    Column(modifier = Modifier.padding(8.dp)) {
        Text("帧率限制")
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it }
        ) {
            TextField(
                value = currentDuration.label,
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
            )
            
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                FrameDurationLimit.values().forEach { duration ->
                    DropdownMenuItem(
                        text = { Text(duration.label) },
                        onClick = {
                            onDurationSelected(duration.value)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun SettingsDivider() {
    Divider(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    )
} 