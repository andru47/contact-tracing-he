#include "dissertation_backend_JNIBridge.h"
#include <ckks_he_server.h>
#include <util.h>

using namespace std;

string getStringFromJ(JNIEnv *env, jcharArray arr)
{
    int len = env->GetArrayLength(arr);
    jchar *elements = env->GetCharArrayElements(arr, 0);
    string toRet = getStringFromJCharArr(elements, len);

    env->ReleaseCharArrayElements(arr, elements, 0);

    return toRet;
}

vector<string> getCipherFromOjectArray(JNIEnv *env, jobjectArray &location)
{
    string latitudeCos = getStringFromJ(env, (jcharArray)env->GetObjectArrayElement(location, 0));
    string latitudeSin = getStringFromJ(env, (jcharArray)env->GetObjectArrayElement(location, 1));
    string longitudeCos = getStringFromJ(env, (jcharArray)env->GetObjectArrayElement(location, 2));
    string longitudeSin = getStringFromJ(env, (jcharArray)env->GetObjectArrayElement(location, 3));

    return { latitudeCos, latitudeSin, longitudeCos, longitudeSin };
}

JNIEXPORT jcharArray JNICALL
Java_dissertation_backend_JNIBridge_getDistance(JNIEnv *env, jobject, jobjectArray location1, jobjectArray location2)
{
    vector<string> cipher1 = getCipherFromOjectArray(env, location1);
    vector<string> cipher2 = getCipherFromOjectArray(env, location2);

    CKKSServerHelper helper(getCKKSParams());
    string returned = helper.compute(cipher1, cipher2);

    jchar *cipherComputed = getJCharArrFromString(returned);

    jcharArray j_version_array = env->NewCharArray(returned.size());
    env->SetCharArrayRegion(j_version_array, 0, returned.size(), cipherComputed);

    return j_version_array;
}

JNIEXPORT jcharArray JNICALL
Java_dissertation_backend_JNIBridge_getAltitudeDifference(JNIEnv *env, jobject, jcharArray altitude1, jcharArray altitude2)
{
    string altitude1String = getStringFromJ(env, altitude1);
    string altitude2String = getStringFromJ(env, altitude2);

    CKKSServerHelper helper(getCKKSParams());
    string difference = helper.computeAltitudeDifference(altitude1String, altitude2String);

    jchar* jDifference = getJCharArrFromString(difference);
    jcharArray j_version_array = env->NewCharArray(difference.size());
    env->SetCharArrayRegion(j_version_array, 0, difference.size(), jDifference);

    return j_version_array;
}
