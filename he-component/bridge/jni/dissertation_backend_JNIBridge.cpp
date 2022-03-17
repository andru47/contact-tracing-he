#include "dissertation_backend_JNIBridge.h"
#include <server_selector.h>
#include <util.h>
#include <fstream>

using namespace std;

vector<string> getCipherFromOjectArray(JNIEnv *env, jobjectArray &location)
{
    string latitudeCos = getStringFromJ(env, (jcharArray)env->GetObjectArrayElement(location, 0));
    string latitudeSin = getStringFromJ(env, (jcharArray)env->GetObjectArrayElement(location, 1));
    string longitudeCos = getStringFromJ(env, (jcharArray)env->GetObjectArrayElement(location, 2));
    string longitudeSin = getStringFromJ(env, (jcharArray)env->GetObjectArrayElement(location, 3));

    return { latitudeCos, latitudeSin, longitudeCos, longitudeSin };
}

JNIEXPORT jcharArray JNICALL Java_dissertation_backend_JNIBridge_getDistanceWithKey
  (JNIEnv *env, jobject, jobjectArray location1, jobjectArray location2, jcharArray rlk) {
    vector<string> cipher1 = getCipherFromOjectArray(env, location1);
    vector<string> cipher2 = getCipherFromOjectArray(env, location2);
    string rlkString = getStringFromJ(env, rlk);
    ofstream g("assets/relinKeySMKHE.bin", ios::binary);
    stringstream stream(rlkString);
    g << stream.rdbuf();
    g.close();

    ServerHelper *helper = getHelper();
    string res = helper->compute(cipher1, cipher2);

    jchar *cipherComputed = getJCharArrFromString(res);

    jcharArray j_version_array = env->NewCharArray(res.size());
    env->SetCharArrayRegion(j_version_array, 0, res.size(), cipherComputed);

    return j_version_array;
}

JNIEXPORT jcharArray JNICALL
Java_dissertation_backend_JNIBridge_getDistance(JNIEnv *env, jobject, jobjectArray location1, jobjectArray location2)
{
    vector<string> cipher1 = getCipherFromOjectArray(env, location1);
    vector<string> cipher2 = getCipherFromOjectArray(env, location2);

    ServerHelper *helper = getHelper();
    string returned = helper->compute(cipher1, cipher2);

    jchar *cipherComputed = getJCharArrFromString(returned);

    jcharArray j_version_array = env->NewCharArray(returned.size());
    env->SetCharArrayRegion(j_version_array, 0, returned.size(), cipherComputed);

    return j_version_array;
}

JNIEXPORT jcharArray JNICALL Java_dissertation_backend_JNIBridge_getAltitudeDifference(
    JNIEnv *env, jobject, jcharArray altitude1, jcharArray altitude2)
{
    string altitude1String = getStringFromJ(env, altitude1);
    string altitude2String = getStringFromJ(env, altitude2);

    ServerHelper *helper = getHelper();
    string difference = helper->computeAltitudeDifference(altitude1String, altitude2String);

    jchar *jDifference = getJCharArrFromString(difference);
    jcharArray j_version_array = env->NewCharArray(difference.size());
    env->SetCharArrayRegion(j_version_array, 0, difference.size(), jDifference);

    return j_version_array;
}

JNIEXPORT jobject JNICALL Java_dissertation_backend_JNIBridge_getMultiKeyDistance(
    JNIEnv *env, jobject, jobjectArray location1, jobjectArray location2, jcharArray pubKeyJ1, jcharArray rlkJ1,
    jcharArray pubKeyJ2, jcharArray rlkJ2)
{
    vector<string> cipher1 = getCipherFromOjectArray(env, location1);
    vector<string> cipher2 = getCipherFromOjectArray(env, location2);

    string pubKey1 = getStringFromJ(env, pubKeyJ1);
    string rlk1 = getStringFromJ(env, rlkJ1);
    string pubKey2 = getStringFromJ(env, pubKeyJ2);
    string rlk2 = getStringFromJ(env, rlkJ2);

    ServerHelper *helper = getHelper();
    vector<string> returned = helper->computeMulti(cipher1, cipher2, pubKey1, rlk1, pubKey2, rlk2);

    jclass cls = env->FindClass("dissertation/backend/CiphertextWrapper");
    jobject toReturn = env->AllocObject(cls);

    jcharArray computedCiphertext1 = env->NewCharArray(returned[0].size());
    env->SetCharArrayRegion(computedCiphertext1, 0, returned[0].size(), getJCharArrFromString(returned[0]));
    env->CallVoidMethod(toReturn, env->GetMethodID(cls, "setComputedCiphertext1", "([C)V"), computedCiphertext1);

    if (returned.size() > 1) {
        jcharArray computedCiphertext2 = env->NewCharArray(returned[1].size());
        env->SetCharArrayRegion(computedCiphertext2, 0, returned[1].size(), getJCharArrFromString(returned[1]));
        env->CallVoidMethod(toReturn, env->GetMethodID(cls, "setComputedCiphertext2", "([C)V"), computedCiphertext2);
    }

    return toReturn;
}

JNIEXPORT jobject JNICALL Java_dissertation_backend_JNIBridge_getMultiKeyAltitudeDifference(
    JNIEnv *env, jobject, jcharArray altitude1, jcharArray altitude2, jcharArray pubKeyJ1, jcharArray rlkJ1,
    jcharArray pubKeyJ2, jcharArray rlkJ2)
{
    string altitude1String = getStringFromJ(env, altitude1);
    string altitude2String = getStringFromJ(env, altitude2);

    string pubKey1 = getStringFromJ(env, pubKeyJ1);
    string rlk1 = getStringFromJ(env, rlkJ1);
    string pubKey2 = getStringFromJ(env, pubKeyJ2);
    string rlk2 = getStringFromJ(env, rlkJ2);

    ServerHelper *helper = getHelper();
    vector<string> returned =
        helper->computeAltitudeDifferenceMulti(altitude1String, altitude2String, pubKey1, rlk1, pubKey2, rlk2);

    jclass cls = env->FindClass("dissertation/backend/CiphertextWrapper");
    jobject toReturn = env->AllocObject(cls);

    jcharArray computedCiphertext1 = env->NewCharArray(returned[0].size());
    env->SetCharArrayRegion(computedCiphertext1, 0, returned[0].size(), getJCharArrFromString(returned[0]));
    env->CallVoidMethod(toReturn, env->GetMethodID(cls, "setComputedCiphertext1", "([C)V"), computedCiphertext1);

    if (returned.size() > 1) {
        jcharArray computedCiphertext2 = env->NewCharArray(returned[1].size());
        env->SetCharArrayRegion(computedCiphertext2, 0, returned[1].size(), getJCharArrFromString(returned[1]));
        env->CallVoidMethod(toReturn, env->GetMethodID(cls, "setComputedCiphertext2", "([C)V"), computedCiphertext2);
    }

    return toReturn;
}
