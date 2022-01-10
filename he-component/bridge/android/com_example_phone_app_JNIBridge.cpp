#include "com_example_phone_app_JNIBridge.h"
#include <client_selector.h>
#include <string>

static ClientHelper *helper = getHelper();
static bool loadedPrivate = false, loadedPublic = false;
static mutex publicKeyMtx, privateKeyMtx;

JNIEXPORT jobject JNICALL Java_com_example_phone_1app_JNIBridge_encrypt(
    JNIEnv *env, jobject, jdouble latitudeCosJ, jdouble latitudeSinJ, jdouble longitudeCosJ, jdouble longitudeSinJ,
    jdouble altitudeJ, jcharArray givenPublicKey)
{
    double latitudeCos = (double)latitudeCosJ;
    double latitudeSin = (double)latitudeSinJ;
    double longitudeCos = (double)longitudeCosJ;
    double longitudeSin = (double)longitudeSinJ;
    double altitude = (double)altitudeJ;
    publicKeyMtx.lock();
    if (!loadedPublic)
    {
        loadedPublic = true;
        jchar *elements = env->GetCharArrayElements(givenPublicKey, 0);
        int len = env->GetArrayLength(givenPublicKey);

        string pubKeyString = getStringFromJCharArr(elements, len);
        helper->loadPublicKeyFromClient(pubKeyString);
        env->ReleaseCharArrayElements(givenPublicKey, elements, 0);
    }
    publicKeyMtx.unlock();

    vector<string> encrypted = helper->encrypt(latitudeCos, latitudeSin, longitudeCos, longitudeSin, altitude);

    jcharArray latitudeCosJarray = env->NewCharArray(encrypted[0].size());
    env->SetCharArrayRegion(latitudeCosJarray, 0, encrypted[0].size(), getJCharArrFromString(encrypted[0]));

    jcharArray latitudeSinJarray = env->NewCharArray(encrypted[1].size());
    env->SetCharArrayRegion(latitudeSinJarray, 0, encrypted[1].size(), getJCharArrFromString(encrypted[1]));

    jcharArray longitudeCosJarray = env->NewCharArray(encrypted[2].size());
    env->SetCharArrayRegion(longitudeCosJarray, 0, encrypted[2].size(), getJCharArrFromString(encrypted[2]));

    jcharArray longitudeSinJarray = env->NewCharArray(encrypted[3].size());
    env->SetCharArrayRegion(longitudeSinJarray, 0, encrypted[3].size(), getJCharArrFromString(encrypted[3]));

    jcharArray altitudeJarray = env->NewCharArray(encrypted[4].size());
    env->SetCharArrayRegion(altitudeJarray, 0, encrypted[4].size(), getJCharArrFromString(encrypted[4]));

    jclass cls = env->FindClass("com/example/phone_app/CiphertextWrapper");
    jobject toReturn = env->AllocObject(cls);

    env->CallVoidMethod(toReturn, env->GetMethodID(cls, "setLatitudeCos", "([C)V"), latitudeCosJarray);
    env->CallVoidMethod(toReturn, env->GetMethodID(cls, "setLatitudeSin", "([C)V"), latitudeSinJarray);
    env->CallVoidMethod(toReturn, env->GetMethodID(cls, "setLongitudeCos", "([C)V"), longitudeCosJarray);
    env->CallVoidMethod(toReturn, env->GetMethodID(cls, "setLongitudeSin", "([C)V"), longitudeSinJarray);
    env->CallVoidMethod(toReturn, env->GetMethodID(cls, "setAltitude", "([C)V"), altitudeJarray);

    return toReturn;
}

JNIEXPORT jdouble JNICALL
Java_com_example_phone_1app_JNIBridge_decrypt(JNIEnv *env, jobject, jcharArray to_decrypt, jcharArray givenPrivateKey)
{
    jchar *elements = env->GetCharArrayElements(to_decrypt, 0);
    int len = env->GetArrayLength(to_decrypt);
    privateKeyMtx.lock();
    if (!loadedPrivate)
    {
        loadedPrivate = true;
        jchar *privateKeyElements = env->GetCharArrayElements(givenPrivateKey, 0);
        int privateKeyLen = env->GetArrayLength(givenPrivateKey);
        string privateKey = getStringFromJCharArr(privateKeyElements, privateKeyLen);
        helper->loadPrivateKeyFromClient(privateKey);

        env->ReleaseCharArrayElements(givenPrivateKey, privateKeyElements, 0);
    }
    privateKeyMtx.unlock();

    string cipherString = getStringFromJCharArr(elements, len);

    env->ReleaseCharArrayElements(to_decrypt, elements, 0);

    return (jdouble)helper->decrypt(cipherString);
}

JNIEXPORT jcharArray JNICALL Java_com_example_phone_1app_JNIBridge_getRelinKeys(JNIEnv *env, jobject)
{
    string relinString = helper->getRelinKeys();

    jcharArray j_version_array = env->NewCharArray(relinString.size());
    env->SetCharArrayRegion(j_version_array, 0, relinString.size(), getJCharArrFromString(relinString));
    return j_version_array;
}

JNIEXPORT jcharArray JNICALL Java_com_example_phone_1app_JNIBridge_getPrivateKey(JNIEnv *env, jobject)
{
    string privateKey = helper->getPrivateKey();

    jcharArray j_version_array = env->NewCharArray(privateKey.size());
    env->SetCharArrayRegion(j_version_array, 0, privateKey.size(), getJCharArrFromString(privateKey));
    return j_version_array;
}

JNIEXPORT jcharArray JNICALL Java_com_example_phone_1app_JNIBridge_getMKPublicKey(JNIEnv *env, jobject)
{
    string mkPubKey = helper->getMKPubKey();

    jcharArray j_version_array = env->NewCharArray(mkPubKey.size());
    env->SetCharArrayRegion(j_version_array, 0, mkPubKey.size(), getJCharArrFromString(mkPubKey));
    return j_version_array;
}

JNIEXPORT jcharArray JNICALL Java_com_example_phone_1app_JNIBridge_getPublicKey(JNIEnv *env, jobject)
{
    string publicKey = helper->getPublicKey();

    jcharArray j_version_array = env->NewCharArray(publicKey.size());
    env->SetCharArrayRegion(j_version_array, 0, publicKey.size(), getJCharArrFromString(publicKey));
    return j_version_array;
}

JNIEXPORT void JNICALL Java_com_example_phone_1app_JNIBridge_generateKeys(JNIEnv *, jobject)
{
    helper->generateKeys();
}

JNIEXPORT jobject JNICALL Java_com_example_phone_1app_JNIBridge_decryptMulti(
    JNIEnv *env, jobject, jcharArray to_decrypt, jcharArray partial, jcharArray givenPrivateKey,
    jboolean finalDecryption)
{
    jchar *elements = env->GetCharArrayElements(to_decrypt, 0);
    int len = env->GetArrayLength(to_decrypt);
    privateKeyMtx.lock();
    if (!loadedPrivate)
    {
        loadedPrivate = true;
        jchar *privateKeyElements = env->GetCharArrayElements(givenPrivateKey, 0);
        int privateKeyLen = env->GetArrayLength(givenPrivateKey);
        string privateKey = getStringFromJCharArr(privateKeyElements, privateKeyLen);
        helper->loadPrivateKeyFromClient(privateKey);

        env->ReleaseCharArrayElements(givenPrivateKey, privateKeyElements, 0);
    }
    privateKeyMtx.unlock();

    string cipherString = getStringFromJCharArr(elements, len);
    string partialString = getStringFromJ(env, partial);

    env->ReleaseCharArrayElements(to_decrypt, elements, 0);
    bool finalDecryptionBool = (bool)finalDecryption;
    jobject toReturn;

    if (finalDecryptionBool)
    {
        double result = (helper->decryptMulti(cipherString, partialString)).result;
        jclass cls = env->FindClass("java/lang/Double");
        jmethodID methodId = env->GetMethodID(cls, "<init>", "(D)V");
        toReturn = env->NewObject(cls, methodId, result);
    }
    else
    {
        string result = (helper->decryptMulti(cipherString, partialString)).halfCipher;
        jclass cls = env->FindClass("com/example/phone_app/CiphertextWrapper");
        toReturn = env->AllocObject(cls);
        jcharArray resultJarray = env->NewCharArray(result.size());
        env->SetCharArrayRegion(resultJarray, 0, result.size(), getJCharArrFromString(result));
        env->CallVoidMethod(toReturn, env->GetMethodID(cls, "setLatitudeCos", "([C)V"), resultJarray);
    }

    return toReturn;
}
