#include "lattigo_he_server.h"
#include <fstream>
#include "liblattigo.h"

string loadRlk()
{
    ifstream f("assets/relinKeyLattigo.bin", ios::binary);
    stringstream stream;
    stream << f.rdbuf();
    f.close();

    return stream.str();
}

void save(string &givenString, string name)
{
    ofstream g("assets/" + name, ios::binary);
    stringstream str(givenString);
    g << str.rdbuf();
    g.close();
}

LattigoServerHelper::LattigoServerHelper()
{
    this->rlk = loadRlk();
}

GoString *getGoStringFromString(string &givenString)
{
    GoString *ptr = new GoString();
    ptr->p = givenString.data();
    ptr->n = static_cast<GoInt>(givenString.size());
    return ptr;
}

GoSlice getGoSliceFromVector(vector<string> &givenVector)
{
    GoString *goStringVec = new GoString[4];

    for (int i = 0; i < givenVector.size(); ++i)
    {
        goStringVec[i] = *getGoStringFromString(givenVector[i]);
    }

    return GoSlice{ goStringVec, static_cast<GoInt>(givenVector.size()), static_cast<GoInt>(givenVector.size()) };
}

string LattigoServerHelper::compute(vector<string> &cipher1, vector<string> &cipher2)
{
    GoSlice goCiphers1 = getGoSliceFromVector(cipher1);
    GoSlice goCiphers2 = getGoSliceFromVector(cipher2);
    GoString *rlkGo = getGoStringFromString(rlk);

    vector<char> outputCipher(524303);

    computeNative(
        goCiphers1, goCiphers2, *rlkGo,
        GoSlice{ outputCipher.data(), static_cast<GoInt>(outputCipher.size()),
                 static_cast<GoInt>(outputCipher.size()) });

    delete[]((GoString *)(goCiphers1.data));
    delete[]((GoString *)(goCiphers2.data));
    delete rlkGo;

    return string(outputCipher.begin(), outputCipher.end());
}

string LattigoServerHelper::computeAltitudeDifference(string &cipher1, string &cipher2)
{
    GoString *goCipher1 = getGoStringFromString(cipher1);
    GoString *goCipher2 = getGoStringFromString(cipher2);
    GoString *rlkGo = getGoStringFromString(rlk);
    vector<char> outputCipher(1048591);

    computeAltitudeDifferenceNative(
        *goCipher1, *goCipher2, *rlkGo,
        GoSlice{ outputCipher.data(), static_cast<GoInt>(outputCipher.size()),
                 static_cast<GoInt>(outputCipher.size()) });

    delete goCipher1;
    delete goCipher2;
    delete rlkGo;

    return string(outputCipher.begin(), outputCipher.end());
}

vector<string> LattigoServerHelper::computeMulti(
    vector<string> &cipher1, vector<string> &cipher2, string &pubKey1, string &rlk1, string &pubKey2, string &rlk2)
{
    GoSlice goCiphers1 = getGoSliceFromVector(cipher1);
    GoSlice goCiphers2 = getGoSliceFromVector(cipher2);
    GoString *goPubKey1 = getGoStringFromString(pubKey1);
    GoString *goRlk1 = getGoStringFromString(rlk1);
    GoString *goPubKey2 = getGoStringFromString(pubKey2);
    GoString *goRlk2 = getGoStringFromString(rlk2);
    vector<char> outputCipher1(524303), outputCipher2(524303);

    computeMultiNative(
        goCiphers1, goCiphers2, *goPubKey1, *goRlk1, *goPubKey2, *goRlk2,
        GoSlice{ outputCipher1.data(), static_cast<GoInt>(outputCipher1.size()),
                 static_cast<GoInt>(outputCipher1.size()) },
        GoSlice{ outputCipher2.data(), static_cast<GoInt>(outputCipher2.size()),
                 static_cast<GoInt>(outputCipher2.size()) });

    delete[]((GoString *)(goCiphers1.data));
    delete[]((GoString *)(goCiphers2.data));
    delete goPubKey1;
    delete goRlk1;
    delete goPubKey2;
    delete goRlk2;

    return { string(outputCipher1.begin(), outputCipher1.end()), string(outputCipher2.begin(), outputCipher2.end()) };
}

vector<string> LattigoServerHelper::computeAltitudeDifferenceMulti(
    string &altitude1, string &altitude2, string &pubKey1, string &rlk1, string &pubKey2, string &rlk2)
{
    GoString *goCipher1 = getGoStringFromString(altitude1);
    GoString *goCipher2 = getGoStringFromString(altitude2);
    GoString *goPubKey1 = getGoStringFromString(pubKey1);
    GoString *goRlk1 = getGoStringFromString(rlk1);
    GoString *goPubKey2 = getGoStringFromString(pubKey2);
    GoString *goRlk2 = getGoStringFromString(rlk2);
    vector<char> outputCipher1(1048591), outputCipher2(1048591);

    computeAltitudeDifferenceMultiNative(
        *goCipher1, *goCipher2, *goPubKey1, *goRlk1, *goPubKey2, *goRlk2,
        GoSlice{ outputCipher1.data(), static_cast<GoInt>(outputCipher1.size()),
                 static_cast<GoInt>(outputCipher1.size()) },
        GoSlice{ outputCipher2.data(), static_cast<GoInt>(outputCipher2.size()),
                 static_cast<GoInt>(outputCipher2.size()) });

    delete goCipher1;
    delete goCipher2;
    delete goPubKey1;
    delete goRlk1;
    delete goPubKey2;
    delete goRlk2;

    return { string(outputCipher1.begin(), outputCipher1.end()), string(outputCipher2.begin(), outputCipher2.end()) };
}
