#include <jni.h>
#include <seal/seal.h>

using namespace std;
using namespace seal;

EncryptionParameters getEncryptionParams();
EncryptionParameters getCKKSParams();
string getStringFromJCharArr(jchar *arr, int lenght);
string uint64_to_hex_string(uint64_t value);
jchar *getJCharArrFromString(string givenString);
string getStringFromJ(JNIEnv *env, jcharArray arr);
