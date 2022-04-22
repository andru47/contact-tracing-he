#include "smkhe_he_client.h"
#include "smkhe/smkhe.h"

SMKHEClientHelper::SMKHEClientHelper()
    : params(
          (1ULL << 60), 8192, { 1152921504605962241, 1152921504606584833, 1152921504606683137 },
          { 0x7fffffffe0001, 0x80000001c0001, 0x80000002c0001, 0x7ffffffd20001 })
{}

void SMKHEClientHelper::generateKeys()
{
    smkhe::MKKeygen keygen(params, 59431639);
    this -> secretKey = keygen.generateSecretKey();
    this -> mkPublicKey = keygen.generatePublicKey();
    this -> pubKey = this -> mkPublicKey.getPublicKey();
    this -> evk = keygen.generateEvaluationKey(mkPublicKey);
    // smkhe::Keygen keygen(params);
    // this -> secretKey = keygen.generateSecretKey();
    // this -> pubKey = keygen.generatePublicKey();
    // this -> evkSingle = keygen.generateEvaluationKey();
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
    string result;
    this->mkPublicKey.serialize(result);

    return result;
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
    MKResult result;

    smkhe::Encoder encoder(params);
    smkhe::MKCiphertext ciphertext;
    smkhe::MKDecryptor decrytor(params);

    ciphertext.deserialize(cipherString);

    if (partial.size()) {
        smkhe::PartialCiphertext partialCipher;
        partialCipher.deserialize(partial);

        smkhe::PartialCiphertext myPartial = decrytor.partialDecryption(ciphertext, 1, this -> secretKey);

        vector<smkhe::PartialCiphertext> allPartials = {myPartial, partialCipher};
        smkhe::Plaintext returnedPlaintext = decrytor.mergeDecryptions(ciphertext, allPartials);
        result.result = encoder.decode(returnedPlaintext)[0].real();
    } else {
        smkhe::PartialCiphertext partialCipher = decrytor.partialDecryption(ciphertext, 2, this -> secretKey);
        string serialized;
        partialCipher.serialize(serialized);

        result.halfCipher = serialized;
    }
 
    return result;
}
