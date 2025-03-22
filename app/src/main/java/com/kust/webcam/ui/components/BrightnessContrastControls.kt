package com.kust.webcam.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun BrightnessContrastControls(
    brightness: Int,
    contrast: Int,
    saturation: Int,
    onBrightnessChanged: (Int) -> Unit,
    onContrastChanged: (Int) -> Unit,
    onSaturationChanged: (Int) -> Unit
) {
    var brightnessValue by remember { mutableStateOf(brightness.toFloat()) }
    var contrastValue by remember { mutableStateOf(contrast.toFloat()) }
    var saturationValue by remember { mutableStateOf(saturation.toFloat()) }
    
    LaunchedEffect(brightness) {
        brightnessValue = brightness.toFloat()
    }
    
    LaunchedEffect(contrast) {
        contrastValue = contrast.toFloat()
    }
    
    LaunchedEffect(saturation) {
        saturationValue = saturation.toFloat()
    }
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "图像调整",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        // 亮度滑块
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "亮度: ${brightnessValue.toInt()}",
                modifier = Modifier.weight(1f)
            )
            
            Slider(
                value = brightnessValue,
                onValueChange = { brightnessValue = it },
                valueRange = -2f..2f,
                steps = 4,
                modifier = Modifier.weight(2f),
                onValueChangeFinished = {
                    onBrightnessChanged(brightnessValue.toInt())
                }
            )
        }
        
        // 对比度滑块
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "对比度: ${contrastValue.toInt()}",
                modifier = Modifier.weight(1f)
            )
            
            Slider(
                value = contrastValue,
                onValueChange = { contrastValue = it },
                valueRange = -2f..2f,
                steps = 4,
                modifier = Modifier.weight(2f),
                onValueChangeFinished = {
                    onContrastChanged(contrastValue.toInt())
                }
            )
        }
        
        // 饱和度滑块
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "饱和度: ${saturationValue.toInt()}",
                modifier = Modifier.weight(1f)
            )
            
            Slider(
                value = saturationValue,
                onValueChange = { saturationValue = it },
                valueRange = -2f..2f,
                steps = 4,
                modifier = Modifier.weight(2f),
                onValueChangeFinished = {
                    onSaturationChanged(saturationValue.toInt())
                }
            )
        }
    }
} 