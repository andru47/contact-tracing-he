#include "com_example_phone_app_JNIBridge.h"
#include <seal/seal.h>
#include <simple_he.h>
#include <util.h>
#include <string>

static HeUtilClient helper(getEncryptionParams());

JNIEXPORT jcharArray JNICALL Java_com_example_phone_1app_JNIBridge_encrypt(JNIEnv *env, jobject, jstring plain)
{
  string plaintTextString(env->GetStringUTFChars(plain, nullptr));
  uint64_t plainLong = 0;
  for (auto &chr : plaintTextString)
  {
    plainLong = plainLong * 10 + (chr - '0');
  }
  string cipher = helper.encrypt(plainLong);

  jcharArray j_version_array = env->NewCharArray(cipher.size());
  env->SetCharArrayRegion(j_version_array, 0, cipher.size(), getJCharArrFromString(cipher));
  return j_version_array;
}

JNIEXPORT jstring JNICALL Java_com_example_phone_1app_JNIBridge_decrypt(JNIEnv *env, jobject, jcharArray to_decrypt)
{
  jchar *elements = env->GetCharArrayElements(to_decrypt, 0);
  int len = env->GetArrayLength(to_decrypt);

  return env->NewStringUTF(helper.decrypt(getStringFromJCharArr(elements, len)).c_str());
}
