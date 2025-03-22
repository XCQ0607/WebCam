pluginManagement {
    repositories {
        // 阿里云Maven镜像源置顶，提高下载速度
        maven(url = "https://maven.aliyun.com/repository/public")
        maven(url = "https://maven.aliyun.com/nexus/content/groups/public/")
        
        // 针对Kotlin相关依赖的镜像源
        maven(url = "https://maven.aliyun.com/repository/google")
        maven(url = "https://maven.aliyun.com/repository/gradle-plugin")
        maven(url = "https://maven.aliyun.com/repository/central")
        maven(url = "https://maven.aliyun.com/repository/jcenter")
        
        // 原有的仓库保留为备选
        google()
        mavenCentral()
        maven(url = "https://jitpack.io")
        maven(url = "https://dl.google.com/dl/android/maven2/")
        maven(url = "https://plugins.gradle.org/m2/")
        maven(url = "https://androidx.dev/storage/compose-compiler/repository/")
        maven(url = "https://jcenter.bintray.com/")
        
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        // 阿里云Maven镜像源置顶，提高下载速度
        maven(url = "https://maven.aliyun.com/repository/public")
        maven(url = "https://maven.aliyun.com/nexus/content/groups/public/")
        
        // 针对Kotlin相关依赖的镜像源
        maven(url = "https://maven.aliyun.com/repository/google")
        maven(url = "https://maven.aliyun.com/repository/gradle-plugin")
        maven(url = "https://maven.aliyun.com/repository/central")
        maven(url = "https://maven.aliyun.com/repository/jcenter")
        
        // 原有的仓库保留为备选
        google()
        mavenCentral()
        maven(url = "https://jitpack.io")
        maven(url = "https://dl.google.com/dl/android/maven2/")
        maven(url = "https://plugins.gradle.org/m2/")
        maven(url = "https://androidx.dev/storage/compose-compiler/repository/")
        maven(url = "https://jcenter.bintray.com/")
    }
}

rootProject.name = "WebCam"
include(":app")
// 根据需要选择是否保留OpenCV模块
// include(":opencv")
// project(":opencv").projectDir = File("D:/Data/Android_Studio/opencv-4.11.0-android-sdk/OpenCV-android-sdk/sdk")
