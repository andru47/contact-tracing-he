#include "util.h"

class CKKSClientHelper
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
    string getRelinKeys();
    string getGaloisKeys();
    string getPrivateKey();
    string getPublicKey();
    void loadPublicKeyFromClient(string &publicKeyString);
    void laodPrivateKeyFromClient(string &privateKeyString);
    vector<string> encrypt(double latitudeCos, double latitudeSin, double longitudeCos, double longitudeSin, double altitude);
    double decrypt(string cipherString);
};
