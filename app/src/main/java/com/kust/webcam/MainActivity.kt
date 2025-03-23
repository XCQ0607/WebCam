package com.kust.webcam

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.kust.webcam.ui.screens.MainScreen
import com.kust.webcam.ui.theme.WebCamTheme
import com.kust.webcam.utils.OpenCVHelper
import com.kust.webcam.domain.viewmodel.CameraViewModel

class MainActivity : ComponentActivity() {
    // 添加OpenCV助手
    private lateinit var openCVHelper: OpenCVHelper
    
    // 添加ViewModel实例
    private val viewModel: CameraViewModel by lazy { CameraViewModel() }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 初始化OpenCV
        openCVHelper = OpenCVHelper(this)
        openCVHelper.initAsync()
        
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