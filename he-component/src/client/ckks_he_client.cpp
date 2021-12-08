#include "ckks_he_client.h"

CKKSClientHelper::CKKSClientHelper(EncryptionParameters params) : context(params)
{
    // KeyGenerator generator(context);
    // this->secretKey = generator.secret_key();
    // generator.create_public_key(this->publicKey);
    // generator.create_relin_keys(this->relinKeys);
    // generator.create_galois_keys(this->galoisKeys);
}

void loadPublicKey(PublicKey &pubKey, string &givenPubKey, SEALContext &context)
{
    stringstream stream(givenPubKey);
    pubKey.load(context, stream);

    return;
}

void loadPrivateKey(SecretKey &secretKey, string &givenSecretKey, SEALContext &context)
{
    stringstream stream(givenSecretKey);
    secretKey.load(context, stream);

    return;
}

string CKKSClientHelper::getGaloisKeys()
{
    stringstream ret;
    galoisKeys.save(ret);
    return ret.str();
}

string CKKSClientHelper::getRelinKeys()
{
    stringstream ret;
    relinKeys.save(ret);
    return ret.str();
}

vector<string> CKKSClientHelper::encrypt(
    double latitudeCos, double latitudeSin, double longitudeCos, double longitudeSin, double altitude)
{
    vector<double> toEncrypt = { latitudeCos, latitudeSin, longitudeCos, longitudeSin, altitude };
    vector<string> result;
    CKKSEncoder encoder(context);

    Encryptor encryptor(context, this->publicKey);
    for (auto &it : toEncrypt)
    {
        Plaintext plain;

        stringstream saveStream;
        Ciphertext cipher;
        encoder.encode(it, scale, plain);
        encryptor.encrypt(plain, cipher);

        cipher.save(saveStream);

        result.push_back(saveStream.str());
    }

    return result;
}

double CKKSClientHelper::decrypt(string cipherString)
{
    stringstream loadStream(cipherString);
    Ciphertext cipher;
    cipher.load(context, loadStream);

    Decryptor decryptor(context, this->secretKey);
    Plaintext plain;
    decryptor.decrypt(cipher, plain);

    CKKSEncoder encoder(context);
    vector<double> decrypted;
    encoder.decode(plain, decrypted);

    return decrypted[0];
}

string CKKSClientHelper::getPrivateKey()
{
    stringstream ret;
    secretKey.save(ret);
    return ret.str();
}

string CKKSClientHelper::getPublicKey()
{
    stringstream ret;
    publicKey.save(ret);
    return ret.str();
}

void CKKSClientHelper::loadPublicKeyFromClient(string &publicKeyString)
{
    loadPublicKey(this->publicKey, publicKeyString, this->context);
}

void CKKSClientHelper::laodPrivateKeyFromClient(string &privateKeyString)
{
    loadPrivateKey(this->secretKey, privateKeyString, this->context);
}
