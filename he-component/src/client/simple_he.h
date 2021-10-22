#include <seal/seal.h>
#include <string>
#include "util.h"
#define string std::string

using namespace std;
using namespace seal;

string example_bfv_basics();
class HeUtilClient {
private:
    SEALContext context;
    KeyGenerator keyGenerator;
    SecretKey secretKey;
    PublicKey publicKey;

public:
    HeUtilClient(EncryptionParameters givenParams);
    string encrypt(uint64_t givenNumber);
    string decrypt(string givenCipher);
};
