//
//  he_bridge.hpp
//  Runner
//
//  Created by Andru Stefanescu on 17.10.2021.
//

#ifndef he_bridge_hpp
#define he_bridge_hpp

#include <string>
#include "ckks_he_client.h"
#include "util.h"

using namespace std;

class HeBridge {
private:
    CKKSClientHelper helper;
    bool publicLoaded, privateLoaded;
    mutex pubKeyMtx, privateKeyMtx;
public:
    HeBridge();
    void setPublic(string& publicKey);
    void setPrivate(string& privateKey);
    string hello(string& name);
    vector<string> encrypt(double latitudeCos, double latitudeSin, double longitudeCos, double longitudeSin, double altitude);
    double decrypt(string& givenCiphertext);
};

#endif /* he_bridge_hpp */
