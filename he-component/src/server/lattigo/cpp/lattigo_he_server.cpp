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
