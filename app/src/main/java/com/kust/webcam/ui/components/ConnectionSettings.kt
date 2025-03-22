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
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
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
    onViewCameraInfo: () -> Unit = {}
) {
    var ipAddress by remember { mutableStateOf(connectionSettings.ipAddress) }
    var httpPort by remember { mutableStateOf(connectionSettings.httpPort.toString()) }
    var connectionName by remember { mutableStateOf(connectionSettings.connectionName) }
    
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
                    .padding(vertical = 8.dp)
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
                    onClick = onSavePreset,
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