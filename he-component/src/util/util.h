#include <seal/seal.h>
#include <jni.h>

using namespace std;
using namespace seal;

EncryptionParameters getEncryptionParams();
string getStringFromJCharArr(jchar *arr, int lenght);
string uint64_to_hex_string(uint64_t value);
jchar *getJCharArrFromString(string givenString);
