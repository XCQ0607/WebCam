package com.kust.webcam

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.kust.webcam.ui.screens.MainScreen
import com.kust.webcam.ui.theme.WebCamTheme
import com.kust.webcam.utils.OpenCVHelper
import com.kust.webcam.domain.viewmodel.CameraViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    // 添加OpenCV助手
    private lateinit var openCVHelper: OpenCVHelper
    
    // 添加ViewModel实例
    private val viewModel: CameraViewModel by lazy { CameraViewModel() }
    
    // 添加TAG常量用于日志记录
    companion object {
        private const val TAG = "WebCam_MainActivity"
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 初始化OpenCV
        openCVHelper = OpenCVHelper(this)
        openCVHelper.initAsync()
        
        // 启动时添加明显的日志标记
        Log.d(TAG, "【应用启动】开始初始化...")
        viewModel.addLog("【应用启动】开始初始化...")
        
        // 在主线程的UI上下文中使用lifecycleScope.launch
        lifecycleScope.launch {
            // 记录更多明显的日志
            Log.d(TAG, "【主界面】开始加载应用配置...")
            viewModel.addLog("【主界面】开始加载应用配置...")
            
            // 延迟一小段时间，确保界面已初始化
            delay(500)
            
            // 加载预设配置，使用非常明显的标记
            Log.d(TAG, "【主界面】准备加载预设...")
            viewModel.addLog("⭐⭐⭐ 【主界面】准备加载预设配置...")
            viewModel.loadPresetsFromPersistentStorage()
            
            // 加载默认配置
            delay(500) // 短暂延迟确保预设加载完成
            Log.d(TAG, "【主界面】准备设置默认连接配置...")
            viewModel.addLog("⭐⭐⭐ 【主界面】准备设置默认连接配置...")
            viewModel.loadDefaultConnection()
            
            // 确认所有初始化步骤已完成
            Log.d(TAG, "【主界面】应用初始化完成")
            viewModel.addLog("⭐⭐⭐ 【主界面】应用初始化完成，请在设置页面查看日志")
        }
        
        setContent {
            WebCamTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen()
                }
            }
        }
    }
} 