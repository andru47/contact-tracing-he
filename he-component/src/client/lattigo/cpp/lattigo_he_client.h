#include "base/client_base.h"

class LattigoClientHelper : public ClientHelper
{
    string publicKey, privateKey, relinKey, mkPubKey;

public:
    LattigoClientHelper();
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
