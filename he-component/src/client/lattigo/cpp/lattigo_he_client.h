#include "base/client_base.h"

class LattigoClientHelper : public ClientHelper
{
    string publicKey, privateKey, relinKey;
    void generateKeysLocally();

public:
    LattigoClientHelper();
    string getRelinKeys();
    string getGaloisKeys();
    string getPrivateKey();
    string getPublicKey();
    void loadPublicKeyFromClient(string &publicKeyString);
    void loadPrivateKeyFromClient(string &privateKeyString);
    vector<string> encrypt(
        double latitudeCos, double latitudeSin, double longitudeCos, double longitudeSin, double altitude);
    double decrypt(string cipherString);
};
