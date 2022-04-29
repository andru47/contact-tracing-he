#include "he_bridge.hpp"

string HeBridge::hello(string& name) {
    return "Hello " + name + " from c++!";
}

HeBridge::HeBridge(ClientHelper** helperPointer) {
    this -> helper = *helperPointer;
}

void HeBridge::setPublic(string &publicKey) {
    this -> pubKeyMtx.lock();
    if (!publicLoaded) {
        this -> helper -> loadPublicKeyFromClient(publicKey);
        publicLoaded = true;
    }
    this -> pubKeyMtx.unlock();
}

void HeBridge::setPrivate(string &privateKey) {
    this -> privateKeyMtx.lock();
    if (!privateLoaded) {
        this -> helper -> loadPrivateKeyFromClient(privateKey);
        privateLoaded = true;
    }
    this -> privateKeyMtx.unlock();
}

vector<string> HeBridge::encrypt(double latitudeCos, double latitudeSin, double longitudeCos, double longitudeSin, double altitude) {
    return this -> helper -> encrypt(latitudeCos, latitudeSin, longitudeCos, longitudeSin, altitude);
}

double HeBridge::decrypt(string &givenCiphertext) {
    return this -> helper -> decrypt(givenCiphertext);
}

MKResult HeBridge::decryptMulti(string &cipher, string &partial) {
    return this -> helper -> decryptMulti(cipher, partial);
}

string HeBridge::getPublicKey() {
    return this -> helper -> getPublicKey();
}

string HeBridge::getPrivateKey() {
    return this -> helper -> getPrivateKey();
}

string HeBridge::getRelinKeys() {
    return this -> helper -> getRelinKeys();
}

void HeBridge::generateKeys() {
    this -> helper -> generateKeys();
}

string HeBridge::getMKPubKey() {
    return this -> helper -> getMKPubKey();
}
