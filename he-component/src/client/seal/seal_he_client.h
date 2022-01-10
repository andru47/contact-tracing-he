#include "base/client_base.h"

class CKKSClientHelper : public ClientHelper
{
private:
    double scale = pow(2.0, 60);
    PublicKey publicKey;
    SecretKey secretKey;
    SEALContext context;
    RelinKeys relinKeys;
    GaloisKeys galoisKeys;

public:
    CKKSClientHelper(EncryptionParameters params);
    void generateKeys();
    string getRelinKeys();
    string getGaloisKeys();
    string getMKPubKey();
    string getPrivateKey();
    string getPublicKey();
    void loadPublicKeyFromClient(string &publicKeyString);
    void loadPrivateKeyFromClient(string &privateKeyString);
    vector<string> encrypt(
        double latitudeCos, double latitudeSin, double longitudeCos, double longitudeSin, double altitude);
    double decrypt(string &cipherString);
    MKResult decryptMulti(string &cipherString, string &partial);
};
