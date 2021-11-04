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
    vector<string> encrypt(double latitudeCos, double latitudeSin, double longitudeCos, double longitudeSin);
    double decrypt(string cipherString);
};
