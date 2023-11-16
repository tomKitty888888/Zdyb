#include <ndkhelper.h>
#include <7zip/MyVersion.h>
#include <cmd/command.h>

#define MY_P7ZIP_VERSION_INFO "7zVersion: "MY_VERSION"\n"MY_COPYRIGHT"\nDate: "MY_DATE

JNIEXPORT jstring JNICALL
Java_com_hzy_libp7zip_P7ZipApi_get7zVersionInfo(JNIEnv *env, jclass type) {
    return env->NewStringUTF(MY_P7ZIP_VERSION_INFO);
}


extern "C"
JNIEXPORT jint JNICALL
Java_com_zdeps_app_utils_P7ZipApi_executeCommand(JNIEnv *env, jclass type, jstring command_) {
    const char *command = env->GetStringUTFChars(command_, 0);
    LOGI("CMD:[%s]", command);
    int ret = executeCommand(command);
    env->ReleaseStringUTFChars(command_, command);
    return ret;
}
extern "C"
JNIEXPORT jstring JNICALL
Java_com_zdeps_app_utils_P7ZipApi_get7zVersionInfo(JNIEnv *env, jclass type) {

    return env->NewStringUTF(MY_P7ZIP_VERSION_INFO);
}