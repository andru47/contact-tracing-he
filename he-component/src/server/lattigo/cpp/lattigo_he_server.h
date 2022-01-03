#include "base/server_base.h"

class LattigoServerHelper : public ServerHelper
{
    string rlk;

public:
    LattigoServerHelper();
    string compute(vector<string> &cipher1, vector<string> &cipher2);
    string computeAltitudeDifference(string &altitude1, string &altitude2);
};
