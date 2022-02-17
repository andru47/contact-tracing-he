#include "base/server_base.h"
#include "smkhe/smkhe.h"

class SMKHEServerHelper : public ServerHelper
{
    smkhe::Parameters params;

public:
    SMKHEServerHelper();
    string compute(vector<string> &cipher1, vector<string> &cipher2);
    vector<string> computeMulti(
        vector<string> &cipher1, vector<string> &cipher2, string &pubKey1, string &rlk1, string &pubKey2, string &rlk2);
    string computeAltitudeDifference(string &altitude1, string &altitude2);
    vector<string> computeAltitudeDifferenceMulti(
        string &altitude1, string &altitude2, string &pubKey1, string &rlk1, string &pubKey2, string &rlk2);
};
