#include "smkhe_he_client.h"
#include "smkhe/smkhe.h"

SMKHEClientHelper::SMKHEClientHelper()
    : params(
          (1ULL << 60), 16384, { 1152921504605962241, 1152921504606584833, 1152921504606683137 },
          { 0x7fffffffe0001, 0x80000001c0001, 0x80000002c0001, 0x7ffffffd20001 })
{}

void SMKHEClientHelper::generateKeys()
{
    smkhe::Keygen keygen(params);
    this->secretKey = keygen.generateSecretKey();
    this->pubKey = keygen.generatePublicKey();
    this->evk = keygen.generateEvaluationKey();
}

string SMKHEClientHelper::getRelinKeys()
{
    string result;
    this->evk.serialize(result);

    return result;
}

string SMKHEClientHelper::getGaloisKeys()
{
    throw("Not implemented");
}

string SMKHEClientHelper::getPrivateKey()
{
    string result;
    this->secretKey.serialize(result);

    return result;
}

string SMKHEClientHelper::getPublicKey()
{
    string result;
    this->pubKey.serialize(result);

    return result;
}

string SMKHEClientHelper::getMKPubKey()
{
    throw("Not implemented");
}

void SMKHEClientHelper::loadPublicKeyFromClient(string &publicKeyString)
{
    this->pubKey.deserialize(publicKeyString);
}

void SMKHEClientHelper::loadPrivateKeyFromClient(string &privateKeyString)
{
    this->secretKey.deserialize(privateKeyString);
}

vector<string> SMKHEClientHelper::encrypt(
    double latitudeCos, double latitudeSin, double longitudeCos, double longitudeSin, double altitude)
{
    vector<double> toEncrypt = { latitudeCos, latitudeSin, longitudeCos, longitudeSin, altitude };
    vector<string> result;
    smkhe::Encoder encoder(params);
    smkhe::Encryptor encryptor(params);

    for (auto &value : toEncrypt)
    {
        smkhe::Plaintext returnedPlaintext = encoder.encode(vector<double>{ value });
        smkhe::Ciphertext returnedCiphertext = encryptor.encrypt(returnedPlaintext, pubKey);
        string current;
        returnedCiphertext.serialize(current);

        result.push_back(current);
    }

    return result;
}

double SMKHEClientHelper::decrypt(string &cipherString)
{
    smkhe::Encoder encoder(params);
    smkhe::Encryptor encryptor(params);

    smkhe::Ciphertext cipher;
    cipher.deserialize(cipherString);

    smkhe::Plaintext plaintext = encryptor.decrypt(cipher, secretKey);

    return encoder.decode(plaintext)[0].real();
}

MKResult SMKHEClientHelper::decryptMulti(string &cipherString, string &partial)
{
    throw("Not implemented");
}
