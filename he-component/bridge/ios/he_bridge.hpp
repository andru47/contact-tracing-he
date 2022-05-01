#ifndef he_bridge_hpp
#define he_bridge_hpp

#include <string>
#include "client_selector.h"

using namespace std;

class HeBridge {
private:
    ClientHelper* helper;
    bool publicLoaded = false, privateLoaded = false;
    mutex pubKeyMtx, privateKeyMtx;
public:
    HeBridge(ClientHelper** helplerPointer);
    void setPublic(string& publicKey);
    void setPrivate(string& privateKey);
    string hello(string& name);
    vector<string> encrypt(double latitudeCos, double latitudeSin, double longitudeCos, double longitudeSin, double altitude);
    double decrypt(string& givenCiphertext);
    MKResult decryptMulti(string &cipher, string &partial);
    void generateKeys();
    string getPublicKey();
    string getPrivateKey();
    string getRelinKeys();
    string getMKPubKey();
};

#endif /* he_bridge_hpp */
