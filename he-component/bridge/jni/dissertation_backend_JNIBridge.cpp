#include "dissertation_backend_JNIBridge.h"
#include <simple_he.h>
#include <util.h>
#include <string>

using namespace std;

string computePoly(stringstream &cipher)
{
  static HeUtilServer helper(getEncryptionParams());
  return helper.evaluate(cipher);
}

JNIEXPORT jcharArray JNICALL Java_dissertation_backend_JNIBridge_computeSimplePoly(JNIEnv *env, jobject obj, jcharArray to_decrypt)
{
  jchar *elements = env->GetCharArrayElements(to_decrypt, 0);
  int len = env->GetArrayLength(to_decrypt);

  string cipher = getStringFromJCharArr(elements, len);
  stringstream cipherStream(cipher);
  string cipherComputedString = computePoly(cipherStream);
  jchar *cipherComputed = getJCharArrFromString(cipherComputedString);

  jcharArray j_version_array = env->NewCharArray(cipherComputedString.size());
  env->SetCharArrayRegion(j_version_array, 0, cipherComputedString.size(), cipherComputed);

  return j_version_array;
}
