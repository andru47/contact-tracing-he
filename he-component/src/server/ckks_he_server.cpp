#include "ckks_he_server.h"

CKKSServerHelper::CKKSServerHelper(EncryptionParameters params) : context(params)
{}

void loadRelin(RelinKeys &relinKeys, string &relinString, SEALContext &context)
{
    stringstream stream(relinString);
    relinKeys.load(context, stream);
}

void loadSecret(SecretKey &secret, string &secretString, SEALContext &context)
{
    stringstream stream(secretString);
    secret.load(context, stream);
}

string CKKSServerHelper::compute(vector<string> &cipher, string relin, string privateKey)
{
    RelinKeys relinKeys;
    SecretKey secret;
    loadRelin(relinKeys, relin, context);
    loadSecret(secret, privateKey, context);

    Evaluator eval(context);
    Decryptor decr(context, secret);
    Ciphertext ciphertext;
    stringstream cipherStream;

    Ciphertext cosLat;
    Ciphertext sinLat, cosLong, sinLong;

    stringstream str(cipher[0]);
    cosLat.load(context, str);
    stringstream str1(cipher[1]);
    sinLat.load(context, str1);
    stringstream str2(cipher[2]);
    cosLong.load(context, str2);
    stringstream str3(cipher[3]);
    sinLong.load(context, str3);

    CKKSEncoder encoder(context);
    Plaintext cosLatRob, sinLatRob, cosLongRob, sinLongRob;
    double scale = pow(2.0, 60);
    const double robLat = 52.204761 * M_PI / 180.0, robLong = 0.105507 * M_PI / 180.0;

    encoder.encode(cos(robLat), scale, cosLatRob);
    encoder.encode(sin(robLat), scale, sinLatRob);
    encoder.encode(cos(robLong), scale, cosLongRob);
    encoder.encode(sin(robLong), scale, sinLongRob);

    Ciphertext cosLatProd;
    eval.multiply_plain(cosLat, cosLatRob, cosLatProd);
    eval.rescale_to_next_inplace(cosLatProd);
    eval.relinearize_inplace(cosLatProd, relinKeys);

    Ciphertext sinLatProd;
    eval.multiply_plain(sinLat, sinLatRob, sinLatProd);
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
    eval.multiply_plain(cosLong, cosLongRob, cosLongProd);
    eval.rescale_to_next_inplace(cosLongProd);
    eval.relinearize_inplace(cosLongProd, relinKeys);

    Ciphertext sinLongProd;
    eval.multiply_plain(sinLong, sinLongRob, sinLongProd);
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
