package com.kust.webcam.ui.components

import com.kust.webcam.ui.components.PermissionTextProvider

class StoragePermissionTextProvider : PermissionTextProvider {
    override fun getTitle() = "需要存储权限"
    
    override fun getDescription() = "需要存储权限来保存照片到设备。请授予应用访问存储的权限，以便将拍摄的照片保存到相册中。"
    
    override fun getButtonText() = "授予权限"
} 