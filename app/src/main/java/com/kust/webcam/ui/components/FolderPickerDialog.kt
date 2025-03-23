package com.kust.webcam.ui.components

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.kust.webcam.utils.hasPermission
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import android.content.pm.PackageManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FolderPickerDialog(
    initialPath: String = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).absolutePath
    } else {
        Environment.getExternalStorageDirectory().absolutePath
    },
    onFolderSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    // 状态
    var currentPath by remember { mutableStateOf(initialPath) }
    var folders by remember { mutableStateOf<List<File>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var showNewFolderDialog by remember { mutableStateOf(false) }
    var newFolderName by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var debugInfo by remember { mutableStateOf("") }
    
    // 定义常用路径列表
    val commonPaths = remember {
        listOf(
            Pair("照片", Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).absolutePath),
            Pair("下载", Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath),
            Pair("DCIM", Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).absolutePath),
            Pair("存储根目录", Environment.getExternalStorageDirectory().absolutePath)
        )
    }
    
    // 加载当前路径的文件夹
    LaunchedEffect(currentPath) {
        isLoading = true
        try {
            debugInfo = "尝试访问: $currentPath"
            val foldersList = withContext(Dispatchers.IO) {
                val directory = File(currentPath)
                if (directory.exists() && directory.isDirectory) {
                    val dirFiles = directory.listFiles()
                    val dirs = dirFiles?.filter { it.isDirectory }?.sortedBy { it.name.lowercase() } ?: emptyList()
                    debugInfo += "\n文件夹数: ${dirs.size}"
                    dirs
                } else {
                    debugInfo += "\n目录不存在或不是目录"
                    emptyList()
                }
            }
            folders = foldersList
        } catch (e: Exception) {
            errorMessage = "无法访问目录: ${e.message}"
            debugInfo += "\n访问错误: ${e.message}"
            folders = emptyList()
        }
        isLoading = false
    }
    
    // 检查存储权限
    val hasStoragePermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        Environment.isExternalStorageManager()
    } else {
        context.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
    }
    
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.8f),
            shape = MaterialTheme.shapes.large
        ) {
            Column {
                // 标题栏
                TopAppBar(
                    title = { Text("选择文件夹") },
                    navigationIcon = {
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "关闭")
                        }
                    },
                    actions = {
                        // 新建文件夹按钮
                        if (hasStoragePermission) {
                            IconButton(onClick = { showNewFolderDialog = true }) {
                                Icon(Icons.Default.CreateNewFolder, contentDescription = "新建文件夹")
                            }
                        }
                        
                        // 选择当前文件夹按钮
                        IconButton(onClick = { onFolderSelected(currentPath) }) {
                            Icon(Icons.Default.Check, contentDescription = "选择此文件夹")
                        }
                    }
                )
                
                // 当前路径显示
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Folder,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = currentPath,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                // 添加常用位置快捷访问
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    commonPaths.forEach { (name, path) ->
                        FilledTonalButton(
                            onClick = { currentPath = path },
                            modifier = Modifier.padding(horizontal = 4.dp)
                        ) {
                            Text(name, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
                
                // 错误信息
                errorMessage?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    )
                }
                
                // 调试信息
                if (debugInfo.isNotEmpty()) {
                    Text(
                        text = debugInfo,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                }
                
                // 权限提示
                if (!hasStoragePermission) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "需要存储权限才能访问文件系统",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                            // Android 11+ 需要特殊权限
                            Button(
                                onClick = {
                                    try {
                                        val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                                        intent.addCategory("android.intent.category.DEFAULT")
                                        intent.data = Uri.parse("package:${context.packageName}")
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        // 如果上面的意图失败，打开应用通用设置页面
                                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                                        intent.data = Uri.parse("package:${context.packageName}")
                                        context.startActivity(intent)
                                    }
                                }
                            ) {
                                Text("前往设置授权")
                            }
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Text(
                                text = "请在设置中开启管理所有文件权限",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            // Android 10及以下处理常规权限
                            Button(
                                onClick = {
                                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                                    intent.data = Uri.parse("package:${context.packageName}")
                                    context.startActivity(intent)
                                }
                            ) {
                                Text("前往设置授权")
                            }
                        }
                    }
                }
                
                // 加载指示器
                if (isLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else {
                    // 上一级目录按钮
                    if (currentPath != "/") {
                        ListItem(
                            headlineContent = { Text("..") },
                            leadingContent = {
                                Icon(
                                    Icons.Default.FolderOpen,
                                    contentDescription = "上一级目录",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            },
                            modifier = Modifier.clickable {
                                val parentFile = File(currentPath).parentFile
                                if (parentFile != null) {
                                    currentPath = parentFile.absolutePath
                                }
                            }
                        )
                        Divider()
                    }
                    
                    // 文件夹列表
                    LazyColumn(
                        modifier = Modifier.weight(1f)
                    ) {
                        items(folders) { folder ->
                            ListItem(
                                headlineContent = { Text(folder.name) },
                                leadingContent = {
                                    Icon(
                                        Icons.Default.Folder,
                                        contentDescription = "文件夹",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                },
                                modifier = Modifier.clickable {
                                    currentPath = folder.absolutePath
                                }
                            )
                            Divider()
                        }
                    }
                    
                    // 底部按钮
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Text("取消")
                        }
                        
                        Button(onClick = { onFolderSelected(currentPath) }) {
                            Text("选择此文件夹")
                        }
                    }
                }
            }
        }
    }
    
    // 新建文件夹对话框
    if (showNewFolderDialog) {
        AlertDialog(
            onDismissRequest = { showNewFolderDialog = false },
            title = { Text("新建文件夹") },
            text = {
                OutlinedTextField(
                    value = newFolderName,
                    onValueChange = { newFolderName = it },
                    label = { Text("文件夹名称") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newFolderName.isNotBlank()) {
                            coroutineScope.launch {
                                try {
                                    val newFolder = File(currentPath, newFolderName)
                                    val success = withContext(Dispatchers.IO) {
                                        newFolder.mkdir()
                                    }
                                    if (success) {
                                        // 刷新文件夹列表
                                        currentPath = currentPath
                                    } else {
                                        errorMessage = "无法创建文件夹"
                                    }
                                } catch (e: Exception) {
                                    errorMessage = "创建文件夹失败: ${e.message}"
                                }
                                newFolderName = ""
                                showNewFolderDialog = false
                            }
                        }
                    }
                ) {
                    Text("创建")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showNewFolderDialog = false }) {
                    Text("取消")
                }
            }
        )
    }
} 