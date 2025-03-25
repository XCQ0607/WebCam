package com.kust.webcam.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.kust.webcam.domain.viewmodel.CameraViewModel
import com.kust.webcam.ui.theme.WebCamTheme
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    
    private lateinit var viewModel: CameraViewModel
    
    // 注册权限请求回调
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            viewModel.showToast("存储权限已授予")
            // 如果是Android 11及以上，引导用户授予所有文件访问权限
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                requestAllFilesPermission()
            }
        } else {
            viewModel.showToast("存储权限被拒绝，部分功能可能无法正常使用")
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        viewModel = ViewModelProvider(this)[CameraViewModel::class.java]
        
        // 记录启动日志
        viewModel.repository.addLog("应用启动，开始初始化...")
        
        // 1. 首先加载已保存的预设
        viewModel.repository.addLog("第1步: 从存储加载预设列表")
        viewModel.loadPresetsFromPersistentStorage()
        
        // 让预设加载完成有时间处理
        viewModel.repository.addLog("短暂等待预设加载完成...")
        
        // 2. 设置默认连接配置 - 使用Activity的lifecycleScope
        lifecycleScope.launch {
            // 稍微延迟一下，确保预设加载已完成
            delay(500)
            viewModel.repository.addLog("第2步: 将默认预设加载到当前连接设置")
            viewModel.loadDefaultConnection()
        }
        
        // 3. 创建UI
        viewModel.repository.addLog("第3步: 初始化用户界面")
        setContent {
            WebCamTheme {
                MainScreen(viewModel = viewModel)
            }
        }
        
        // 4. 请求所需权限
        viewModel.repository.addLog("第4步: 请求所需权限")
        requestBasicStoragePermission()
        
        viewModel.repository.addLog("应用启动初始化完成")
    }
    
    // 请求基本存储权限
    private fun requestBasicStoragePermission() {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) {
            // Android 10及以下，请求WRITE_EXTERNAL_STORAGE权限
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) 
                != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        } else {
            // Android 11+，请求READ_MEDIA_IMAGES权限
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) 
                != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
            } else {
                // 已有基本权限，检查是否需要请求全部文件访问权限
                if (!Environment.isExternalStorageManager()) {
                    // 给用户提示，不自动跳转
                    viewModel.showToast("如需保存到自定义目录，请在设置中授予管理所有文件权限")
                }
            }
        }
    }
    
    // 请求管理所有文件的权限（Android 11+）
    private fun requestAllFilesPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                try {
                    viewModel.showToast("正在引导授权《管理所有文件》权限...")
                    val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                    intent.addCategory("android.intent.category.DEFAULT")
                    intent.data = Uri.parse("package:$packageName")
                    startActivity(intent)
                } catch (e: Exception) {
                    viewModel.showToast("打开权限设置失败: ${e.message}")
                    try {
                        // 如果上面的意图失败，打开应用通用设置页面
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                        intent.data = Uri.parse("package:$packageName")
                        startActivity(intent)
                    } catch (e2: Exception) {
                        viewModel.showToast("无法打开应用设置: ${e2.message}")
                    }
                }
            } else {
                viewModel.showToast("已有管理所有文件权限")
            }
        } else {
            viewModel.showToast("当前Android版本不需要《管理所有文件》权限")
        }
    }
} 