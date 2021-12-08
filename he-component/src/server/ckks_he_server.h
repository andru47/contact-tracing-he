#include "util.h"

class CKKSServerHelper
{
    SEALContext context;

public:
    CKKSServerHelper(EncryptionParameters params);
    string compute(vector<string> &cipher1, vector<string> &cipher2);
    string computeAltitudeDifference(string &altitude1, string &altitude2);
};
