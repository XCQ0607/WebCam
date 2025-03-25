package com.kust.webcam.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Videocam
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kust.webcam.domain.viewmodel.CameraViewModel
import com.kust.webcam.ui.components.ConnectionSettingsCard
import com.kust.webcam.ui.components.ConnectionStatusCard
import com.kust.webcam.ui.components.LogDisplay
import kotlinx.coroutines.delay

data class TabItem(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val screen: @Composable () -> Unit
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun MainScreen(viewModel: CameraViewModel = viewModel()) {
    var selectedTabIndex by remember { mutableIntStateOf(1) }
    val operationLogs by viewModel.repository.operationLogs.collectAsState()
    
    // 日志通知状态
    var showLogNotification by remember { mutableStateOf(true) }
    
    // 定时隐藏日志通知
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(5000) // 5秒后隐藏
        showLogNotification = false
    }
    
    val tabs = listOf(
        TabItem(
            title = "视频流",
            selectedIcon = Icons.Filled.Videocam,
            unselectedIcon = Icons.Outlined.Videocam,
            screen = { LiveStreamScreen() }
        ),
        TabItem(
            title = "设置",
            selectedIcon = Icons.Filled.Settings,
            unselectedIcon = Icons.Outlined.Settings,
            screen = { SettingsScreen() }
        ),
        TabItem(
            title = "关于",
            selectedIcon = Icons.Filled.Info,
            unselectedIcon = Icons.Outlined.Info,
            screen = { AboutScreen() }
        )
    )
    
    val elevation by animateDpAsState(
        targetValue = if (selectedTabIndex == 0) 0.dp else 4.dp,
        label = "appBarElevation"
    )
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("ESP32-CAM WebCam") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                modifier = Modifier.shadow(elevation)
            )
        },
        bottomBar = {
            Column {
                // 显示日志通知
                AnimatedVisibility(
                    visible = showLogNotification && operationLogs.isNotEmpty() && selectedTabIndex != 1,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.secondaryContainer)
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "应用日志已记录，请在设置页面查看",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
                
                NavigationBar {
                    tabs.forEachIndexed { index, tab ->
                        NavigationBarItem(
                            selected = selectedTabIndex == index,
                            onClick = { selectedTabIndex = index },
                            icon = { 
                                Icon(
                                    imageVector = if (selectedTabIndex == index) tab.selectedIcon else tab.unselectedIcon,
                                    contentDescription = tab.title
                                ) 
                            },
                            label = { Text(tab.title) }
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // 使用AnimatedContent实现平滑的屏幕切换效果
            AnimatedContent(
                targetState = selectedTabIndex,
                transitionSpec = {
                    if (targetState > initialState) {
                        // 向左滑动
                        (slideInHorizontally { width -> width } + fadeIn()) togetherWith
                        (slideOutHorizontally { width -> -width } + fadeOut())
                    } else {
                        // 向右滑动
                        (slideInHorizontally { width -> -width } + fadeIn()) togetherWith
                        (slideOutHorizontally { width -> width } + fadeOut())
                    }
                },
                label = "screenTransition"
            ) { targetIndex ->
                tabs[targetIndex].screen()
            }
        }
    }
} 