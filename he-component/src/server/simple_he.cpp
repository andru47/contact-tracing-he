#include "simple_he.h"

HeUtilServer::HeUtilServer(EncryptionParameters parms) : context(parms), evaluator(context){};

string HeUtilServer::evaluate(stringstream &stream)
{
    Ciphertext cipher;
    cipher.load(context, stream);

    evaluator.add_plain_inplace(cipher, Plaintext("1"));
    evaluator.square_inplace(cipher);

    stringstream str;
    cipher.save(str);

    return str.str();
}
