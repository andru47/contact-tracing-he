#include "dissertation_backend_JNIBridge.h"
#include <ckks_he_server.h>
#include <util.h>

using namespace std;

string getStringFromJ(JNIEnv *env, jcharArray arr)
{
    int len = env->GetArrayLength(arr);
    jchar *elements = env->GetCharArrayElements(arr, 0);

    return getStringFromJCharArr(elements, len);
}

JNIEXPORT jcharArray JNICALL Java_dissertation_backend_JNIBridge_getDistance(
    JNIEnv *env, jobject, jcharArray latitudeCosJ, jcharArray latitudeSinJ, jcharArray longitudeCosJ,
    jcharArray longitudeSinJ, jcharArray relinJ, jcharArray privateJ)
{
    string latitudeCos = getStringFromJ(env, latitudeCosJ);
    string latitudeSin = getStringFromJ(env, latitudeSinJ);
    string longitudeCos = getStringFromJ(env, longitudeCosJ);
    string longitudeSin = getStringFromJ(env, longitudeSinJ);
    vector<string> cipher = { latitudeCos, latitudeSin, longitudeCos, longitudeSin };
    string relin = getStringFromJ(env, relinJ);
    string privateKey = getStringFromJ(env, privateJ);

    CKKSServerHelper helper(getCKKSParams());
    string returned = helper.compute(cipher, relin, privateKey);

    jchar *cipherComputed = getJCharArrFromString(returned);

    jcharArray j_version_array = env->NewCharArray(returned.size());
    env->SetCharArrayRegion(j_version_array, 0, returned.size(), cipherComputed);

    return j_version_array;
}
