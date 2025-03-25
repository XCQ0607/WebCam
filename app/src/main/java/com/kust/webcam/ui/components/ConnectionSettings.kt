package com.kust.webcam.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kust.webcam.data.model.ConnectionSettings
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConnectionSettingsCard(
    connectionSettings: ConnectionSettings,
    onUpdateSettings: (ConnectionSettings) -> Unit,
    onTestConnection: () -> Unit,
    connectionStatus: Boolean,
    savedConnections: List<ConnectionSettings> = emptyList(),
    onLoadPreset: (ConnectionSettings) -> Unit = {},
    onSavePreset: () -> Unit = {},
    onRestoreDefaults: () -> Unit = {},
    onViewCameraInfo: () -> Unit = {},
    onDeletePreset: (Int) -> Unit = {}
) {
    var ipAddress by remember { mutableStateOf(connectionSettings.ipAddress) }
    var httpPort by remember { mutableStateOf(connectionSettings.httpPort.toString()) }
    var streamPort by remember { mutableStateOf(connectionSettings.streamPort.toString()) }
    var connectionName by remember { mutableStateOf(connectionSettings.connectionName) }
    
    // 确认删除对话框状态
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var presetIndexToDelete by remember { mutableStateOf(-1) }
    
    // 确认覆盖对话框状态
    var showOverwriteConfirmDialog by remember { mutableStateOf(false) }
    var presetIndexToOverwrite by remember { mutableStateOf(-1) }
    
    // 默认连接名称警告对话框
    var showDefaultNameWarning by remember { mutableStateOf(false) }
    
    // 删除确认对话框
    if (showDeleteConfirmDialog && presetIndexToDelete >= 0) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text("确认删除") },
            text = { Text("确定要删除预设 '${savedConnections[presetIndexToDelete].connectionName}' 吗？") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeletePreset(presetIndexToDelete)
                        showDeleteConfirmDialog = false
                    }
                ) {
                    Text("确定")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteConfirmDialog = false }
                ) {
                    Text("取消")
                }
            }
        )
    }
    
    // 覆盖确认对话框
    if (showOverwriteConfirmDialog && presetIndexToOverwrite >= 0) {
        AlertDialog(
            onDismissRequest = { showOverwriteConfirmDialog = false },
            title = { Text("确认覆盖") },
            text = { Text("已存在名为 '${connectionSettings.connectionName}' 的预设，是否覆盖？") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onSavePreset()
                        showOverwriteConfirmDialog = false
                    }
                ) {
                    Text("确定")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showOverwriteConfirmDialog = false }
                ) {
                    Text("取消")
                }
            }
        )
    }
    
    // 默认连接名称警告对话框
    if (showDefaultNameWarning) {
        AlertDialog(
            onDismissRequest = { showDefaultNameWarning = false },
            title = { Text("提示") },
            text = { Text("请更改连接名称后再保存预设") },
            confirmButton = {
                TextButton(onClick = { showDefaultNameWarning = false }) {
                    Text("确定")
                }
            }
        )
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "连接设置",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            // 预设列表
            if (savedConnections.isNotEmpty()) {
                Text(
                    text = "预设连接",
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                
                savedConnections.forEachIndexed { index, preset ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = preset.connectionName,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "IP: ${preset.ipAddress}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontStyle = FontStyle.Italic
                                )
                            }
                            
                            // 加载按钮
                            TextButton(
                                onClick = { onLoadPreset(preset) }
                            ) {
                                Text("加载")
                            }
                            
                            // 删除按钮 (默认预设不可删除)
                            if (index > 0) { // 假设第一个是默认预设
                                IconButton(
                                    onClick = {
                                        presetIndexToDelete = index
                                        showDeleteConfirmDialog = true
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "删除预设",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
            }
            
            OutlinedTextField(
                value = ipAddress,
                onValueChange = { 
                    ipAddress = it
                    onUpdateSettings(connectionSettings.copy(ipAddress = it))
                },
                label = { Text("摄像头IP地址") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )
            
            OutlinedTextField(
                value = httpPort,
                onValueChange = { 
                    httpPort = it
                    val port = it.toIntOrNull() ?: connectionSettings.httpPort
                    onUpdateSettings(connectionSettings.copy(httpPort = port))
                },
                label = { Text("HTTP端口") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            
            OutlinedTextField(
                value = streamPort,
                onValueChange = { 
                    streamPort = it
                    val port = it.toIntOrNull() ?: connectionSettings.streamPort
                    onUpdateSettings(connectionSettings.copy(streamPort = port))
                },
                label = { Text("视频流端口") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            
            OutlinedTextField(
                value = connectionName,
                onValueChange = { 
                    connectionName = it
                    onUpdateSettings(connectionSettings.copy(connectionName = it))
                },
                label = { Text("连接名称") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onTestConnection,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("测试连接")
                }
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Button(
                    onClick = {
                        // 检查是否是默认连接名称
                        if (connectionSettings.connectionName == "ESP32摄像头") {
                            // 提示用户更改连接名称
                            showDefaultNameWarning = true
                        } else {
                            // 检查是否存在同名预设
                            val existingIndex = savedConnections.indexOfFirst { it.connectionName == connectionSettings.connectionName }
                            if (existingIndex >= 0) {
                                // 显示覆盖确认对话框
                                presetIndexToOverwrite = existingIndex
                                showOverwriteConfirmDialog = true
                            } else {
                                // 直接保存
                                onSavePreset()
                            }
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("保存预设")
                }
            }
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onRestoreDefaults,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("恢复默认")
                }
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Button(
                    onClick = onViewCameraInfo,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("查看摄像头信息")
                }
            }
        }
    }
}