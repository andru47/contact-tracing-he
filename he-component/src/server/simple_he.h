#include <seal/seal.h>
#include <string>

using namespace std;
using namespace seal;

class HeUtilServer
{
private:
    SEALContext context;
    Evaluator evaluator;

public:
    HeUtilServer(EncryptionParameters parms);
    string evaluate(stringstream &stream);
};
