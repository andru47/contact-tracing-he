/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class com_example_phone_app_JNIBridge */

#ifndef _Included_com_example_phone_app_JNIBridge
#define _Included_com_example_phone_app_JNIBridge
#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     com_example_phone_app_JNIBridge
 * Method:    encrypt
 * Signature: (Ljava/lang/String;)[C
 */
JNIEXPORT jcharArray JNICALL Java_com_example_phone_1app_JNIBridge_encrypt
  (JNIEnv *, jobject, jstring);

/*
 * Class:     com_example_phone_app_JNIBridge
 * Method:    decrypt
 * Signature: ([C)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_com_example_phone_1app_JNIBridge_decrypt
  (JNIEnv *, jobject, jcharArray);

#ifdef __cplusplus
}
#endif
#endif
