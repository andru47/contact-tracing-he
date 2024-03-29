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
 * Signature: (DDDDD[C)Lcom/example/phone_app/CiphertextWrapper;
 */
JNIEXPORT jobject JNICALL Java_com_example_phone_1app_JNIBridge_encrypt
  (JNIEnv *, jobject, jdouble, jdouble, jdouble, jdouble, jdouble, jcharArray);

/*
 * Class:     com_example_phone_app_JNIBridge
 * Method:    getRelinKeys
 * Signature: ()[C
 */
JNIEXPORT jcharArray JNICALL Java_com_example_phone_1app_JNIBridge_getRelinKeys
  (JNIEnv *, jobject);

/*
 * Class:     com_example_phone_app_JNIBridge
 * Method:    getPrivateKey
 * Signature: ()[C
 */
JNIEXPORT jcharArray JNICALL Java_com_example_phone_1app_JNIBridge_getPrivateKey
  (JNIEnv *, jobject);

/*
 * Class:     com_example_phone_app_JNIBridge
 * Method:    getPublicKey
 * Signature: ()[C
 */
JNIEXPORT jcharArray JNICALL Java_com_example_phone_1app_JNIBridge_getPublicKey
  (JNIEnv *, jobject);

/*
 * Class:     com_example_phone_app_JNIBridge
 * Method:    getMKPublicKey
 * Signature: ()[C
 */
JNIEXPORT jcharArray JNICALL Java_com_example_phone_1app_JNIBridge_getMKPublicKey
  (JNIEnv *, jobject);

/*
 * Class:     com_example_phone_app_JNIBridge
 * Method:    generateKeys
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_com_example_phone_1app_JNIBridge_generateKeys
  (JNIEnv *, jobject);

/*
 * Class:     com_example_phone_app_JNIBridge
 * Method:    decrypt
 * Signature: ([C[C)D
 */
JNIEXPORT jdouble JNICALL Java_com_example_phone_1app_JNIBridge_decrypt
  (JNIEnv *, jobject, jcharArray, jcharArray);

/*
 * Class:     com_example_phone_app_JNIBridge
 * Method:    decryptMulti
 * Signature: ([C[C[CZ)Ljava/lang/Object;
 */
JNIEXPORT jobject JNICALL Java_com_example_phone_1app_JNIBridge_decryptMulti
  (JNIEnv *, jobject, jcharArray, jcharArray, jcharArray, jboolean);

#ifdef __cplusplus
}
#endif
#endif
