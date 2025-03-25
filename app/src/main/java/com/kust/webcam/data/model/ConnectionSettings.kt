package com.kust.webcam.data.model

data class ConnectionSettings(
    val ipAddress: String = "192.168.4.1",
    val httpPort: Int = 80,
    val streamPort: Int = 81,
    val connectionName: String = "ESP32摄像头"
)