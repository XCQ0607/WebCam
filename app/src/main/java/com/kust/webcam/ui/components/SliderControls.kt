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
fun QualitySlider(
    quality: Int,
    onQualityChanged: (Int) -> Unit
) {
    var sliderValue by remember { mutableStateOf(quality.toFloat()) }
    
    LaunchedEffect(quality) {
        sliderValue = quality.toFloat()
    }
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "图像质量",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "质量: ${sliderValue.toInt()}",
                modifier = Modifier.weight(1f)
            )
            
            Slider(
                value = sliderValue,
                onValueChange = { sliderValue = it },
                valueRange = 0f..63f,
                steps = 63,
                modifier = Modifier.weight(2f),
                onValueChangeFinished = {
                    onQualityChanged(sliderValue.toInt())
                }
            )
        }
    }
} 