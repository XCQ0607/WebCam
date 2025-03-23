package com.kust.webcam.ui.theme

import android.app.Activity
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// 鲜艳现代风格的暗色主题
private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF5D9CEC),          // 明亮的蓝色
    onPrimary = Color(0xFF000000),        // 黑色文字在主色上
    primaryContainer = Color(0xFF283593), // 深蓝色容器
    onPrimaryContainer = Color(0xFFE3F2FD), // 浅蓝色文字在主色容器上
    
    secondary = Color(0xFF66BB6A),        // 绿色
    onSecondary = Color(0xFF000000),      // 黑色文字在次要色上
    secondaryContainer = Color(0xFF1B5E20), // 深绿色容器
    onSecondaryContainer = Color(0xFFE8F5E9), // 浅绿色文字
    
    tertiary = Color(0xFFFFB74D),         // 橙色
    onTertiary = Color(0xFF000000),       // 黑色文字在第三色上
    tertiaryContainer = Color(0xFFE65100), // 深橙色容器
    onTertiaryContainer = Color(0xFFFFF3E0), // 浅橙色文字
    
    error = Color(0xFFEF5350),            // 错误色（红色）
    onError = Color(0xFF000000),          // 黑色文字在错误色上
    errorContainer = Color(0xFFB71C1C),   // 深红色容器
    onErrorContainer = Color(0xFFFFEBEE), // 浅红色文字
    
    background = Color(0xFF121212),       // 深黑背景
    onBackground = Color(0xFFEEEEEE),     // 接近白色的文字
    surface = Color(0xFF1E1E1E),          // 表面颜色
    onSurface = Color(0xFFEEEEEE),        // 文字颜色
    
    surfaceVariant = Color(0xFF2D2D2D),   // 变体表面
    onSurfaceVariant = Color(0xFFBDBDBD), // 文字颜色
    outline = Color(0xFF757575)           // 轮廓线
)

// 鲜艳现代风格的亮色主题
private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF2196F3),          // 蓝色
    onPrimary = Color(0xFFFFFFFF),        // 白色文字在主色上
    primaryContainer = Color(0xFFBBDEFB), // 浅蓝色容器
    onPrimaryContainer = Color(0xFF1565C0), // 深蓝色文字
    
    secondary = Color(0xFF4CAF50),        // 绿色
    onSecondary = Color(0xFFFFFFFF),      // 白色文字在次要色上
    secondaryContainer = Color(0xFFC8E6C9), // 浅绿色容器
    onSecondaryContainer = Color(0xFF1B5E20), // 深绿色文字
    
    tertiary = Color(0xFFFF9800),         // 橙色
    onTertiary = Color(0xFFFFFFFF),       // 白色文字在第三色上
    tertiaryContainer = Color(0xFFFFE0B2), // 浅橙色容器
    onTertiaryContainer = Color(0xFFE65100), // 深橙色文字
    
    error = Color(0xFFF44336),            // 错误色（红色）
    onError = Color(0xFFFFFFFF),          // 白色文字在错误色上
    errorContainer = Color(0xFFFFCDD2),   // 浅红色容器
    onErrorContainer = Color(0xFFB71C1C), // 深红色文字
    
    background = Color(0xFFFAFAFA),       // 接近白色的背景
    onBackground = Color(0xFF212121),     // 深灰文字
    surface = Color(0xFFFFFFFF),          // 白色表面
    onSurface = Color(0xFF212121),        // 深灰文字
    
    surfaceVariant = Color(0xFFEEEEEE),   // 变体表面
    onSurfaceVariant = Color(0xFF757575), // 文字颜色
    outline = Color(0xFFBDBDBD)           // 轮廓线
)

@Composable
fun WebCamTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // 使用动画颜色，使主题切换更加平滑
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    
    // 动画化的主要颜色，可以在主题切换时提供平滑过渡
    val animatedPrimary by animateColorAsState(
        targetValue = colorScheme.primary,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "primaryColor"
    )
    
    val animatedBackground by animateColorAsState(
        targetValue = colorScheme.background,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "backgroundColor"
    )
    
    // 动态设置状态栏颜色
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primaryContainer.toArgb()
            
            // 设置状态栏图标颜色
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
            
            // 设置导航栏颜色
            window.navigationBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !darkTheme
        }
    }

    // 创建一个经过动画补间的主题
    MaterialTheme(
        colorScheme = colorScheme.copy(
            primary = animatedPrimary,
            background = animatedBackground
        ),
        typography = Typography,
        content = content
    )
} 