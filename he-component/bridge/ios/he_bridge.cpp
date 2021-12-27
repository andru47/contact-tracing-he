//
//  he_bridge.cpp
//  Runner
//
//  Created by Andru Stefanescu on 17.10.2021.
//

#include "he_bridge.hpp"

string HeBridge::hello(string& name) {
    return "Hello " + name + " from c++!";
}

HeBridge::HeBridge() : helper(getCKKSParams()) {
}

void HeBridge::setPublic(string &publicKey) {
    this -> pubKeyMtx.lock();
    if (!publicLoaded) {
        this -> helper.loadPublicKeyFromClient(publicKey);
        publicLoaded = true;
    }
    this -> pubKeyMtx.unlock();
}

void HeBridge::setPrivate(string &privateKey) {
    this -> privateKeyMtx.lock();
    if (!privateLoaded) {
        this -> helper.loadPrivateKeyFromClient(privateKey);
        privateLoaded = true;
    }
    this -> privateKeyMtx.unlock();
}

vector<string> HeBridge::encrypt(double latitudeCos, double latitudeSin, double longitudeCos, double longitudeSin, double altitude) {
    return this -> helper.encrypt(latitudeCos, latitudeSin, longitudeCos, longitudeSin, altitude);
}

double HeBridge::decrypt(string &givenCiphertext) {
    return this -> helper.decrypt(givenCiphertext);
}
