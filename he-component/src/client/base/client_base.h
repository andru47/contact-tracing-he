#include "util.h"

struct MKResult
{
    string halfCipher;
    double result;
};

class ClientHelper
{
public:
    ClientHelper();
    virtual void generateKeys() = 0;
    virtual string getRelinKeys() = 0;
    virtual string getGaloisKeys() = 0;
    virtual string getPrivateKey() = 0;
    virtual string getPublicKey() = 0;
    virtual string getMKPubKey() = 0;
    virtual void loadPublicKeyFromClient(string &publicKeyString) = 0;
    virtual void loadPrivateKeyFromClient(string &privateKeyString) = 0;
    virtual vector<string> encrypt(
        double latitudeCos, double latitudeSin, double longitudeCos, double longitudeSin, double altitude) = 0;
    virtual double decrypt(string &cipherString) = 0;
    virtual MKResult decryptMulti(string &cipherString, string &partial) = 0;
};
