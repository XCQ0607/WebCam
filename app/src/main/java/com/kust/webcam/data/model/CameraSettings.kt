package com.kust.webcam.data.model

data class CameraSettings(
    val framesize: Int = 8, // 默认VGA (640x480)
    val quality: Int = 10,
    val brightness: Int = 0,
    val contrast: Int = 0,
    val saturation: Int = 0,
    val specialEffect: Int = 0,
    val awb: Boolean = true,
    val awbGain: Boolean = true,
    val wbMode: Int = 0,
    val aec: Boolean = true,
    val aec2: Boolean = true,
    val aeLevel: Int = 0,
    val aecValue: Int = 204,
    val agc: Boolean = true,
    val agcGain: Int = 5,
    val gainceiling: Int = 0,
    val bpc: Boolean = false,
    val wpc: Boolean = true,
    val rawGma: Boolean = true,
    val lenc: Boolean = true,
    val hmirror: Boolean = true,
    val vflip: Boolean = true,
    val dcw: Boolean = true,
    val colorbar: Boolean = false,
    val rotate: Int = 0,
    val minFrameTime: Int = 0,
    // 灯光相关
    val lamp: Int = 0,
    val autoLamp: Boolean = false
)

enum class FrameSize(val value: Int, val label: String, val resolution: String) {
    UXGA(13, "UXGA", "1600x1200"),
    SXGA(12, "SXGA", "1280x1024"),
    HD(11, "HD", "1280x720"),
    XGA(10, "XGA", "1024x768"),
    SVGA(9, "SVGA", "800x600"),
    VGA(8, "VGA", "640x480"),
    HVGA(7, "HVGA", "480x320"),
    CIF(6, "CIF", "400x296"),
    QVGA(5, "QVGA", "320x240"),
    HQVGA(3, "HQVGA", "240x176"),
    QQVGA(1, "QQVGA", "160x120"),
    THUMB(0, "THUMB", "96x96");

    companion object {
        fun fromValue(value: Int): FrameSize {
            return values().find { it.value == value } ?: VGA
        }
    }
}

enum class SpecialEffect(val value: Int, val label: String) {
    NO_EFFECT(0, "无特效"),
    NEGATIVE(1, "负片"),
    GRAYSCALE(2, "灰度"),
    RED_TINT(3, "红色调"),
    GREEN_TINT(4, "绿色调"),
    BLUE_TINT(5, "蓝色调"),
    SEPIA(6, "复古");

    companion object {
        fun fromValue(value: Int): SpecialEffect {
            return values().find { it.value == value } ?: NO_EFFECT
        }
    }
}

enum class WbMode(val value: Int, val label: String) {
    AUTO(0, "自动"),
    SUNNY(1, "晴天"),
    CLOUDY(2, "阴天"),
    OFFICE(3, "办公室"),
    HOME(4, "家庭");

    companion object {
        fun fromValue(value: Int): WbMode {
            return values().find { it.value == value } ?: AUTO
        }
    }
}

enum class FrameDurationLimit(val value: Int, val label: String) {
    FPS_0_3(3333, "0.3fps (3.3秒)"),
    FPS_0_5(2000, "0.5fps (2秒)"),
    FPS_1(1000, "1fps (1秒)"),
    FPS_2(500, "2fps (500毫秒)"),
    FPS_3(333, "3fps (333毫秒)"),
    FPS_5(200, "5fps (200毫秒)"),
    FPS_10(100, "10fps (100毫秒)"),
    FPS_20(50, "20fps (50毫秒)"),
    DISABLED(0, "禁用");

    companion object {
        fun fromValue(value: Int): FrameDurationLimit {
            return values().find { it.value == value } ?: DISABLED
        }
    }
} 