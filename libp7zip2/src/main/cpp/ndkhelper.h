#ifndef ANDROIDUN7ZIP_NDKHELPER_H
#define ANDROIDUN7ZIP_NDKHELPER_H

#ifdef __cplusplus
extern "C" {
#endif

#include <jni.h>

#ifdef NATIVE_LOG
#define LOG_TAG "NATIVE.LOG"
#include <android/log.h>

#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG,LOG_TAG,__VA_ARGS__)
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)
#define LOGW(...) __android_log_print(ANDROID_LOG_WARN,LOG_TAG,__VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)
#define LOGF(...) __android_log_print(ANDROID_LOG_FATAL,LOG_TAG,__VA_ARGS__)
#else
#define LOGD(...) do{}while(0)
#define LOGI(...) do{}while(0)
#define LOGW(...) do{}while(0)
#define LOGE(...) do{}while(0)
#define LOGF(...) do{}while(0)
#endif

JNIEXPORT jstring JNICALL
        Java_com_hzy_libp7zip_P7ZipApi_get7zVersionInfo(JNIEnv *env, jclass type);

JNIEXPORT jint JNICALL
        Java_com_hzy_libp7zip_P7ZipApi_executeCommand(JNIEnv *env, jclass type, jstring command_);

#ifdef __cplusplus
}
#endif

#endif

