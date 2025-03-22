#include <jni.h>
#include <string>
#include <android/log.h>

// OpenCV头文件
#include <opencv2/core.hpp>
#include <opencv2/imgproc.hpp>

#define TAG "WebCamNative"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, TAG, __VA_ARGS__)

extern "C" {

// 简单的JNI测试函数
JNIEXPORT jstring JNICALL
Java_com_kust_webcam_utils_OpenCVHelper_getOpenCVVersion(JNIEnv *env, jobject thiz) {
    std::string version = cv::getVersionString();
    LOGI("OpenCV版本: %s", version.c_str());
    return env->NewStringUTF(version.c_str());
}

} // extern "C" 