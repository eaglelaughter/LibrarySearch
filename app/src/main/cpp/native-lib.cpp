#include <jni.h>
#include <string>


extern "C"
JNIEXPORT jstring JNICALL
Java_com_example_machenike_wifi_Mode_1Select_stringFromJNI(JNIEnv *env, jobject instance) {

    // TODO


    return env->NewStringUTF("Hello from C++");
}