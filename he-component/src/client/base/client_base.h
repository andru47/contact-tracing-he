//
// Created by Andru Stefanescu on 30.12.2021.
//
#include "util.h"

class ClientHelper {
public:
    ClientHelper();
    virtual string getRelinKeys()=0;
    virtual string getGaloisKeys()=0;
    virtual string getPrivateKey()=0;
    virtual string getPublicKey()=0;
    virtual void loadPublicKeyFromClient(string &publicKeyString)=0;
    virtual void loadPrivateKeyFromClient(string &privateKeyString)=0;
    virtual vector<string> encrypt(double latitudeCos, double latitudeSin, double longitudeCos, double longitudeSin, double altitude)=0;
    virtual double decrypt(string cipherString)=0;
};