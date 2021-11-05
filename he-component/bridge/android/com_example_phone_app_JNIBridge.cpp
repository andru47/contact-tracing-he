#include "com_example_phone_app_JNIBridge.h"
#include <ckks_he_client.h>
#include <seal/seal.h>
#include <string>
#include <util.h>

static CKKSClientHelper helper(getCKKSParams());

JNIEXPORT jobject JNICALL Java_com_example_phone_1app_JNIBridge_encrypt(
    JNIEnv *env, jobject, jdouble latitudeCosJ, jdouble latitudeSinJ, jdouble longitudeCosJ, jdouble longitudeSinJ)
{
    double latitudeCos = (double)latitudeCosJ;
    double latitudeSin = (double)latitudeSinJ;
    double longitudeCos = (double)longitudeCosJ;
    double longitudeSin = (double)longitudeSinJ;
    vector<string> encrypted = helper.encrypt(latitudeCos, latitudeSin, longitudeCos, longitudeSin);

    jcharArray latitudeCosJarray = env->NewCharArray(encrypted[0].size());
    env->SetCharArrayRegion(latitudeCosJarray, 0, encrypted[0].size(), getJCharArrFromString(encrypted[0]));

    jcharArray latitudeSinJarray = env->NewCharArray(encrypted[1].size());
    env->SetCharArrayRegion(latitudeSinJarray, 0, encrypted[1].size(), getJCharArrFromString(encrypted[1]));

    jcharArray longitudeCosJarray = env->NewCharArray(encrypted[2].size());
    env->SetCharArrayRegion(longitudeCosJarray, 0, encrypted[2].size(), getJCharArrFromString(encrypted[2]));

    jcharArray longitudeSinJarray = env->NewCharArray(encrypted[3].size());
    env->SetCharArrayRegion(longitudeSinJarray, 0, encrypted[3].size(), getJCharArrFromString(encrypted[3]));

    jclass cls = env->FindClass("com/example/phone_app/CiphertextWrapper");
    jobject toReturn = env->AllocObject(cls);

    env->CallVoidMethod(toReturn, env->GetMethodID(cls, "setLatitudeCos", "([C)V"), latitudeCosJarray);
    env->CallVoidMethod(toReturn, env->GetMethodID(cls, "setLatitudeSin", "([C)V"), latitudeSinJarray);
    env->CallVoidMethod(toReturn, env->GetMethodID(cls, "setLongitudeCos", "([C)V"), longitudeCosJarray);
    env->CallVoidMethod(toReturn, env->GetMethodID(cls, "setLongitudeSin", "([C)V"), longitudeSinJarray);

    return toReturn;
}

JNIEXPORT jdouble JNICALL Java_com_example_phone_1app_JNIBridge_decrypt(JNIEnv *env, jobject, jcharArray to_decrypt)
{
    jchar *elements = env->GetCharArrayElements(to_decrypt, 0);
    int len = env->GetArrayLength(to_decrypt);

    string cipherString = getStringFromJCharArr(elements, len);

    env->ReleaseCharArrayElements(to_decrypt, elements, 0);

    return (jdouble)helper.decrypt(cipherString);
}

JNIEXPORT jcharArray JNICALL Java_com_example_phone_1app_JNIBridge_getRelinKeys(JNIEnv *env, jobject)
{
    string relinString = helper.getRelinKeys();

    jcharArray j_version_array = env->NewCharArray(relinString.size());
    env->SetCharArrayRegion(j_version_array, 0, relinString.size(), getJCharArrFromString(relinString));
    return j_version_array;
}

JNIEXPORT jcharArray JNICALL Java_com_example_phone_1app_JNIBridge_getPrivateKey(JNIEnv *env, jobject)
{
    string privateKey = helper.getPrivateKey();

    jcharArray j_version_array = env->NewCharArray(privateKey.size());
    env->SetCharArrayRegion(j_version_array, 0, privateKey.size(), getJCharArrFromString(privateKey));
    return j_version_array;
}
