#include "simple_he.h"

string HeUtilClient::encrypt(uint64_t givenNumber) {
    Encryptor encryptor(context, publicKey);
    Plaintext givenNumberPlain(uint64_to_hex_string(givenNumber));
    Ciphertext givenNumerCipher;
    encryptor.encrypt(givenNumberPlain, givenNumerCipher);
    stringstream stream;
    givenNumerCipher.save(stream);
    return stream.str();
}

string HeUtilClient::decrypt(string cipher) {
    stringstream stream(cipher);
    Ciphertext cipherText;
    cipherText.load(context, stream);
    Decryptor decryptor(context, secretKey);
    Plaintext decryptedResult;
    decryptor.decrypt(cipherText, decryptedResult);

    return decryptedResult.to_string();
}

HeUtilClient::HeUtilClient(EncryptionParameters givenParams) : context(givenParams), keyGenerator(context) {
    this -> secretKey = keyGenerator.secret_key();
    keyGenerator.create_public_key(this -> publicKey);
}
