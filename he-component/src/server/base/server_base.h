#include <util.h>

class ServerHelper
{
public:
    ServerHelper();
    virtual string compute(vector<string> &cipher1, vector<string> &cipher2) = 0;
    virtual vector<string> computeMulti(
        vector<string> &cipher1, vector<string> &cipher2, string &pubKey1, string &rlk1, string &pubKey2,
        string &rlk2) = 0;
    virtual string computeAltitudeDifference(string &altitude1, string &altitude2) = 0;
    virtual vector<string> computeAltitudeDifferenceMulti(
        string &altitude1, string &altitude2, string &pubKey1, string &rlk1, string &pubKey2, string &rlk2) = 0;
};
