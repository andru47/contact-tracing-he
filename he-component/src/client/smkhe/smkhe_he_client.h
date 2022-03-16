#include "base/client_base.h"
#include "smkhe/smkhe.h"

class SMKHEClientHelper : public ClientHelper
{
private:
    smkhe::Parameters params;
    smkhe::PublicKey pubKey;
    smkhe::SecretKey secretKey;
    smkhe::MKEvaluationKey evk;
    smkhe::MKPublicKey mkPublicKey;

public:
    SMKHEClientHelper();
    void generateKeys();
    string getRelinKeys();
    string getGaloisKeys();
    string getPrivateKey();
    string getPublicKey();
    string getMKPubKey();
    void loadPublicKeyFromClient(string &publicKeyString);
    void loadPrivateKeyFromClient(string &privateKeyString);
    vector<string> encrypt(
        double latitudeCos, double latitudeSin, double longitudeCos, double longitudeSin, double altitude);
    double decrypt(string &cipherString);
    MKResult decryptMulti(string &cipherString, string &partial);
};
