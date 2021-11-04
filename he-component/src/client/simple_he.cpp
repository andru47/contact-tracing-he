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

string HeUtilClient::getParams() {
    EncryptionParameters parms(scheme_type::ckks);
    size_t poly_modulus_degree = 8192;
    parms.set_poly_modulus_degree(poly_modulus_degree);
    parms.set_coeff_modulus(CoeffModulus::Create(poly_modulus_degree, { 60, 40, 40, 60 }));

    stringstream stream;
    parms.save(stream, compr_mode_type::none);
    return stream.str();
}
