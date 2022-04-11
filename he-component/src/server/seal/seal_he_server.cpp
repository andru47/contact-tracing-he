#include "seal_he_server.h"
#include <fstream>

CKKSServerHelper::CKKSServerHelper(EncryptionParameters params) : context(params)
{}

void loadRelin(RelinKeys &relinKeys, SEALContext &context)
{
    ifstream f("assets/relinKey.bin", ios::binary);
    stringstream stream;
    stream << f.rdbuf();
    f.close();
    relinKeys.load(context, stream);
}

void loadSecret(SecretKey &secret, string &secretString, SEALContext &context)
{
    stringstream stream(secretString);
    secret.load(context, stream);
}

void loadCipherToValues(
    Ciphertext &cosLat, Ciphertext &sinLat, Ciphertext &cosLong, Ciphertext &sinLong, vector<string> &cipher,
    SEALContext &context)
{
    stringstream str(cipher[0]);
    cosLat.load(context, str);
    stringstream str1(cipher[1]);
    sinLat.load(context, str1);
    stringstream str2(cipher[2]);
    cosLong.load(context, str2);
    stringstream str3(cipher[3]);
    sinLong.load(context, str3);
}

void loadAltitudesToCiphers(
    string &altitude1, string &altitude2, Ciphertext &cipher1, Ciphertext &cipher2, SEALContext &context)
{
    stringstream stream1(altitude1);
    cipher1.load(context, stream1);
    stringstream stream2(altitude2);
    cipher2.load(context, stream2);
}

string CKKSServerHelper::compute(vector<string> &cipher1, vector<string> &cipher2)
{
    RelinKeys relinKeys;
    // SecretKey secret;
    loadRelin(relinKeys, context);
    // loadSecret(secret, privateKey, context);

    Evaluator eval(context);
    // Decryptor decr(context, secret);
    Ciphertext ciphertext;
    stringstream cipherStream;

    Ciphertext cosLat1, cosLat2;
    Ciphertext sinLat1, sinLat2;
    Ciphertext cosLong1, cosLong2;
    Ciphertext sinLong1, sinLong2;

    loadCipherToValues(cosLat1, sinLat1, cosLong1, sinLong1, cipher1, context);
    loadCipherToValues(cosLat2, sinLat2, cosLong2, sinLong2, cipher2, context);

    CKKSEncoder encoder(context);
    // Plaintext cosLatRob, sinLatRob, cosLongRob, sinLongRob;
    double scale = pow(2.0, 60);
    // const double robLat = 52.204761 * M_PI / 180.0, robLong = 0.105507 * M_PI / 180.0;

    // encoder.encode(cos(robLat), scale, cosLatRob);
    // encoder.encode(sin(robLat), scale, sinLatRob);
    // encoder.encode(cos(robLong), scale, cosLongRob);
    // encoder.encode(sin(robLong), scale, sinLongRob);

    Ciphertext cosLatProd;
    eval.multiply(cosLat1, cosLat2, cosLatProd);
    eval.rescale_to_next_inplace(cosLatProd);
    eval.relinearize_inplace(cosLatProd, relinKeys);

    Ciphertext sinLatProd;
    eval.multiply(sinLat1, sinLat2, sinLatProd);
    eval.rescale_to_next_inplace(sinLatProd);
    eval.relinearize_inplace(sinLatProd, relinKeys);

    Ciphertext havLat;
    cosLatProd.scale() = sinLatProd.scale();
    eval.mod_switch_to_inplace(cosLatProd, sinLatProd.parms_id());
    eval.add(cosLatProd, sinLatProd, havLat);
    eval.negate_inplace(havLat);

    Plaintext oneLat;
    encoder.encode(1.0, havLat.scale(), oneLat);
    eval.mod_switch_to_inplace(oneLat, havLat.parms_id());
    eval.add_plain_inplace(havLat, oneLat);

    Ciphertext cosLongProd;
    eval.multiply(cosLong1, cosLong2, cosLongProd);
    eval.rescale_to_next_inplace(cosLongProd);
    eval.relinearize_inplace(cosLongProd, relinKeys);

    Ciphertext sinLongProd;
    eval.multiply(sinLong1, sinLong2, sinLongProd);
    eval.rescale_to_next_inplace(sinLongProd);
    eval.relinearize_inplace(sinLongProd, relinKeys);

    Ciphertext havLong;
    cosLongProd.scale() = sinLongProd.scale();
    eval.mod_switch_to_inplace(cosLongProd, sinLongProd.parms_id());
    eval.add(cosLongProd, sinLongProd, havLong);
    eval.negate_inplace(havLong);

    Plaintext oneLong;
    encoder.encode(1.0, havLong.scale(), oneLong);
    eval.mod_switch_to_inplace(oneLong, havLong.parms_id());
    eval.add_plain_inplace(havLong, oneLong);
 
    eval.multiply_inplace(havLong, cosLatProd);
    eval.rescale_to_next_inplace(havLong);
    eval.relinearize_inplace(havLong, relinKeys);

    Ciphertext result;
    havLat.scale() = havLong.scale();
    eval.mod_switch_to_inplace(havLat, havLong.parms_id());
    eval.add(havLat, havLong, result);

    stringstream resultStore;

    result.save(resultStore);

    return resultStore.str();
}

string CKKSServerHelper::computeAltitudeDifference(string &altitude1, string &altitude2)
{
    Evaluator eval(context);
    Ciphertext altitude1Cipher, altitude2Cipher;
    loadAltitudesToCiphers(altitude1, altitude2, altitude1Cipher, altitude2Cipher, context);
    eval.sub_inplace(altitude1Cipher, altitude2Cipher);

    stringstream resultStream;
    altitude1Cipher.save(resultStream);

    return resultStream.str();
}

vector<string> CKKSServerHelper::computeMulti(
    vector<string> &cipher1, vector<string> &cipher2, string &pubKey1, string &rlk1, string &pubKey2, string &rlk2)
{
    throw "NOT IMPLEMENTED";
}

vector<string> CKKSServerHelper::computeAltitudeDifferenceMulti(
    string &altitude1, string &altitude2, string &pubKey1, string &rlk1, string &pubKey2, string &rlk2)
{
    throw "NOT IMPLEMENTED";
}
