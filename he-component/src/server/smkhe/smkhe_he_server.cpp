#include "smkhe_he_server.h"
#include "fstream"

SMKHEServerHelper::SMKHEServerHelper()
    : params(
          (1ULL << 60), 16384, { 1152921504605962241, 1152921504606584833, 1152921504606683137 },
          { 0x7fffffffe0001, 0x80000001c0001, 0x80000002c0001, 0x7ffffffd20001 })
{}

void loadEvaluationKey(smkhe::EvaluationKey &key)
{
    ifstream f("assets/relinKeySMKHE.bin", ios::binary);
    stringstream stream;
    stream << f.rdbuf();
    f.close();

    string readString = stream.str();
    key.deserialize(readString);
}

void saveEvaluationKeys(smkhe::EvaluationKey &evk)
{
    string serializedString;
    evk.serialize(serializedString);

    ofstream g("assets/relinKeySMKHE.bin", ios::binary);
    stringstream stream(serializedString);

    g << stream.rdbuf();

    g.close();
}

void loadCipherToValues(
    smkhe::Ciphertext &cosLat, smkhe::Ciphertext &sinLat, smkhe::Ciphertext &cosLong, smkhe::Ciphertext &sinLong,
    vector<string> &cipher)
{
    cosLat.deserialize(cipher[0]);
    sinLat.deserialize(cipher[1]);
    cosLong.deserialize(cipher[2]);
    sinLong.deserialize(cipher[3]);
}

void loadAltitudesToCiphers(
    string &altitude1, string &altitude2, smkhe::Ciphertext &cipher1, smkhe::Ciphertext &cipher2)
{
    cipher1.deserialize(altitude1);
    cipher2.deserialize(altitude2);
}

string SMKHEServerHelper::compute(vector<string> &cipher1, vector<string> &cipher2)
{
    smkhe::Ciphertext cosLat1, cosLat2;
    smkhe::Ciphertext sinLat1, sinLat2;
    smkhe::Ciphertext cosLong1, cosLong2;
    smkhe::Ciphertext sinLong1, sinLong2;

    loadCipherToValues(cosLat1, sinLat1, cosLong1, sinLong1, cipher1);
    loadCipherToValues(cosLat2, sinLat2, cosLong2, sinLong2, cipher2);

    smkhe::EvaluationKey evk;
    loadEvaluationKey(evk);

    smkhe::Encoder enc(params);
    smkhe::Plaintext one = enc.encode(vector<double>{ 1.0 });
    smkhe::Evaluator eval(params, evk);

    smkhe::Ciphertext cosLatProd = cosLat1;
    eval.multiplyRelinInPlace(cosLatProd, cosLat2);
    smkhe::Ciphertext sinLatProd = sinLat1;
    eval.multiplyRelinInPlace(sinLatProd, sinLat2);

    smkhe::Ciphertext havLat = cosLatProd;
    eval.addCipherInPlace(havLat, sinLatProd);
    eval.negateInPlace(havLat);
    eval.addPlainInPlace(havLat, one);

    smkhe::Ciphertext cosLongProd = cosLong1;
    eval.multiplyRelinInPlace(cosLongProd, cosLong2);

    smkhe::Ciphertext sinLongProd = sinLong1;
    eval.multiplyRelinInPlace(sinLongProd, sinLong2);

    smkhe::Ciphertext havLong = cosLongProd;
    eval.addCipherInPlace(havLong, sinLongProd);
    eval.negateInPlace(havLong);
    eval.addPlainInPlace(havLong, one);

    eval.multiplyRelinInPlace(havLong, cosLatProd);

    eval.addCipherInPlace(havLat, havLong);

    string result;
    havLat.serialize(result);

    return result;
}

vector<string> SMKHEServerHelper::computeMulti(
    vector<string> &cipher1, vector<string> &cipher2, string &pubKey1, string &rlk1, string &pubKey2, string &rlk2)
{
    throw("Not implemented");
}
string SMKHEServerHelper::computeAltitudeDifference(string &altitude1, string &altitude2)
{
    smkhe::Ciphertext altitude1Cipher, altitude2Cipher;
    loadAltitudesToCiphers(altitude1, altitude2, altitude1Cipher, altitude2Cipher);
    smkhe::EvaluationKey evk;
    smkhe::Evaluator eval(params, evk);
    eval.negateInPlace(altitude2Cipher);
    eval.addCipherInPlace(altitude1Cipher, altitude2Cipher);
    string result;
    altitude1Cipher.serialize(result);

    return result;
}
vector<string> SMKHEServerHelper::computeAltitudeDifferenceMulti(
    string &altitude1, string &altitude2, string &pubKey1, string &rlk1, string &pubKey2, string &rlk2)
{
    throw("Not implemented");
}
