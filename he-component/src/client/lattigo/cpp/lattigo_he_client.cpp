#include "lattigo_he_client.h"
#include "lib_lattigo.h"

GoString *getGoStringFromString(string &givenString)
{
    GoString *ptr = new GoString();
    ptr->p = givenString.data();
    ptr->n = static_cast<GoInt>(givenString.size());
    return ptr;
}

GoSlice getGoSlice(vector<char> &givenVector)
{
    return GoSlice{ givenVector.data(), static_cast<GoInt>(givenVector.size()),
                    static_cast<GoInt>(givenVector.size()) };
}

LattigoClientHelper::LattigoClientHelper()
{}

vector<string> LattigoClientHelper::encrypt(
    double latitudeCos, double latitudeSin, double longitudeCos, double longitudeSin, double altitude)
{
    vector<vector<char>> ret(5, vector<char>(1048591));
    vector<GoSlice> goSliceVec = { getGoSlice(ret[0]), getGoSlice(ret[1]), getGoSlice(ret[2]), getGoSlice(ret[3]),
                                   getGoSlice(ret[4]) };
    GoString *pubKeyGo = getGoStringFromString(publicKey);
    encryptNative(
        latitudeCos, latitudeSin, longitudeCos, longitudeSin, altitude, *pubKeyGo,
        GoSlice{ goSliceVec.data(), static_cast<GoInt>(goSliceVec.size()), static_cast<GoInt>(goSliceVec.size()) });

    delete pubKeyGo;
    vector<string> stringRet = { string(ret[0].begin(), ret[0].end()), string(ret[1].begin(), ret[1].end()),
                                 string(ret[2].begin(), ret[2].end()), string(ret[3].begin(), ret[3].end()),
                                 string(ret[4].begin(), ret[4].end()) };
    return stringRet;
}

void LattigoClientHelper::generateKeysLocally()
{
    vector<char> keys[3] = { vector<char>(1310726), vector<char>(655362), vector<char>(10485795) };
    GoSlice publicKeyGo =
        GoSlice{ keys[0].data(), static_cast<GoInt>(keys[0].size()), static_cast<GoInt>(keys[0].size()) };
    GoSlice secreKeyGo =
        GoSlice{ keys[1].data(), static_cast<GoInt>(keys[1].size()), static_cast<GoInt>(keys[1].size()) };
    GoSlice rlkGo = GoSlice{ keys[2].data(), static_cast<GoInt>(keys[2].size()), static_cast<GoInt>(keys[2].size()) };
    generateKeys(publicKeyGo, secreKeyGo, rlkGo);
    publicKey = string(keys[0].begin(), keys[0].end());
    privateKey = string(keys[1].begin(), keys[1].end());
    relinKey = string(keys[2].begin(), keys[2].end());
}

string LattigoClientHelper::getPublicKey()
{
    return publicKey;
}

string LattigoClientHelper::getPrivateKey()
{
    return privateKey;
}

string LattigoClientHelper::getRelinKeys()
{
    return relinKey;
}

string LattigoClientHelper::getGaloisKeys()
{
    throw("NOT IMPLEMENTED");
}

void LattigoClientHelper::loadPrivateKeyFromClient(string &privateKey)
{
    this->privateKey = privateKey;
}

void LattigoClientHelper::loadPublicKeyFromClient(string &pubKey)
{
    this->publicKey = pubKey;
}

double LattigoClientHelper::decrypt(string cipherString)
{
    GoString *goCipherString = getGoStringFromString(cipherString);
    GoString *goPrivateKey = getGoStringFromString(privateKey);

    double result = decryptNative(*goCipherString, *goPrivateKey);

    delete goCipherString;
    delete goPrivateKey;

    return result;
}
