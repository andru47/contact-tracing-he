#include "base/server_base.h"

class CKKSServerHelper : public ServerHelper
{
    SEALContext context;

public:
    CKKSServerHelper(EncryptionParameters params);
    string compute(vector<string> &cipher1, vector<string> &cipher2);
    string computeAltitudeDifference(string &altitude1, string &altitude2);
};
