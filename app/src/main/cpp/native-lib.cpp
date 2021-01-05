#include <jni.h>
#include <string>

#pragma clang diagnostic push
#pragma ide diagnostic ignored "OCUnusedGlobalDeclarationInspection"

extern "C"
JNIEXPORT jstring
JNICALL Java_mutnemom_android_kotlindemo_services_JNIProvider_getApiBinanceUrl(
        JNIEnv *env,
        jobject) {
    std::string apiEndpoint = "https://api.binance.com/";
    return env->NewStringUTF(apiEndpoint.c_str());
}

#pragma clang diagnostic pop
