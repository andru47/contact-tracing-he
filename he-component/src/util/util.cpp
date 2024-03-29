#include "util.h"

EncryptionParameters getEncryptionParams()
{
    EncryptionParameters parms(scheme_type::bfv);
    size_t poly_modulus_degree = 4096;
    parms.set_poly_modulus_degree(poly_modulus_degree);
    parms.set_coeff_modulus(CoeffModulus::BFVDefault(poly_modulus_degree));
    parms.set_plain_modulus(1024);

    return parms;
}

EncryptionParameters getCKKSParams()
{
    EncryptionParameters parms(scheme_type::ckks);
    size_t poly_modulus_degree = 16384;
    parms.set_poly_modulus_degree(poly_modulus_degree);
    parms.set_coeff_modulus(CoeffModulus::Create(poly_modulus_degree, { 60, 60, 60, 60, 60 }));

    return parms;
}

string getStringFromJCharArr(jchar *arr, int length)
{
    stringstream stream;

    for (int i = 0; i < length; i++)
    {
        stream << (char)arr[i];
    }

    return stream.str();
}

string uint64_to_hex_string(uint64_t value)
{
    return util::uint_to_hex_string(&value, size_t(1));
}

jchar *getJCharArrFromString(string givenString)
{
    int length = givenString.size();
    jchar *arr = (jchar *)calloc(sizeof(jchar), length);

    for (int i = 0; i < length; ++i)
    {
        arr[i] = (jchar)givenString[i];
    }

    return arr;
}

string getStringFromJ(JNIEnv *env, jcharArray arr)
{
    int len = env->GetArrayLength(arr);
    jchar *elements = env->GetCharArrayElements(arr, 0);
    string toRet = getStringFromJCharArr(elements, len);

    env->ReleaseCharArrayElements(arr, elements, 0);

    return toRet;
}
