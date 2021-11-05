#include "util.h"

class CKKSServerHelper
{
    SEALContext context;

public:
    CKKSServerHelper(EncryptionParameters params);
    string compute(vector<string> &cipher, string relin, string privateKey);
};
