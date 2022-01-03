package main

import "C"
import (
	"github.com/ldsec/lattigo/v2/ckks"
	"github.com/ldsec/lattigo/v2/rlwe"
)

func loadPublicKey(pubKeyString string, params ckks.Parameters) *rlwe.PublicKey {
	pubKey := ckks.NewPublicKey(params)
	err := pubKey.UnmarshalBinary([]byte(pubKeyString))

	if err != nil {
		println("Error loading public key")
	}

	return pubKey
}

func loadPrivateKey(privateKeyString string, params ckks.Parameters) *rlwe.SecretKey {
	secretKey := rlwe.NewSecretKey(params.Parameters)
	err := secretKey.UnmarshalBinary([]byte(privateKeyString))

	if err != nil {
		println("Error loading secret key")
	}

	return secretKey
}

func loadCipher(cipherString string, params ckks.Parameters) *ckks.Ciphertext {
	ciphertext := ckks.NewCiphertext(params, 1, 1, float64(1<<114))
	err := ciphertext.UnmarshalBinary([]byte(cipherString))

	if err != nil {
		println("Error loading ciphertext", err.Error())
	}

	return ciphertext
}

func getParams() ckks.Parameters {
	params, err := ckks.NewParametersFromLiteral(ckks.ParametersLiteral{
		LogN:     14,
		Q:        []uint64{1152921504606748673, 576460752308273153, 576460752302473217, 576460752304439297},
		P:        []uint64{576460752302080001},
		Sigma:    rlwe.DefaultSigma,
		LogSlots: 12,
		Scale:    float64(1 << 57)})

	if err != nil {
		println("Error creating parameters")
	}

	return params
}

func encryptNew(toEncrypt float64, encoder ckks.Encoder, encryptor ckks.Encryptor, params ckks.Parameters) *ckks.Ciphertext {
	encodedValue := encoder.EncodeNTTAtLvlNew(params.MaxLevel(), []complex128{complex(toEncrypt, 0.0)}, 12)
	encryptedValue := encryptor.EncryptFastNew(encodedValue)

	return encryptedValue
}

func saveCipherToBytes(givenCipher *ckks.Ciphertext) []byte {
	data, err := givenCipher.MarshalBinary()
	if err != nil {
		println("Error saving ciphertext to string")
	}
	return data
}

func loadRelin(relinString string, params ckks.Parameters) *rlwe.RelinearizationKey {
	relinKey := rlwe.NewRelinKey(params.Parameters, 2)

	err := relinKey.UnmarshalBinary([]byte(relinString))

	if err != nil {
		println("Error loading relinkey")
	}

	return relinKey
}

func loadCiphers(cipherArray []string, params ckks.Parameters) []*ckks.Ciphertext {
	ret := []*ckks.Ciphertext{}
	for _, cipher := range cipherArray {
		ret = append(ret, loadCipher(cipher, params))
	}

	return ret
}

//export encryptNative
func encryptNative(latitudeCos, latitudeSin, longitudeCos, longitudeSin, altitude float64, pubKey string, ciphers [][]byte) {
	toEncrypt := []float64{latitudeCos, latitudeSin, longitudeCos, longitudeSin, altitude}

	params := getParams()
	loadedPublicKey := loadPublicKey(pubKey, params)
	encoder := ckks.NewEncoder(params)
	encryptor := ckks.NewEncryptorFromPk(params, loadedPublicKey)

	for index, element := range toEncrypt {
		copy(ciphers[index], saveCipherToBytes(encryptNew(element, encoder, encryptor, params)))
	}
}

func savePublicKeyToByte(pk *rlwe.PublicKey) []byte {
	data, err := pk.MarshalBinary()
	if err != nil {
		println("Error saving pubkey to string")
	}

	return data
}

func saveSecretKeyToByte(sk *rlwe.SecretKey) []byte {
	data, err := sk.MarshalBinary()
	if err != nil {
		println("Error saving secret key to string")
	}

	return data
}

func saveRelinKeyToByte(rlk *rlwe.RelinearizationKey) []byte {
	data, err := rlk.MarshalBinary()
	if err != nil {
		println("Error saving rlk to string")
	}

	return data
}

//export generateKeys
func generateKeys(publicKey, privateKey, relinKey []byte) {
	params := getParams()
	kgen := ckks.NewKeyGenerator(params)
	sk := kgen.GenSecretKey()
	pk := kgen.GenPublicKey(sk)
	rlk := kgen.GenRelinearizationKey(sk)
	copy(publicKey, savePublicKeyToByte(pk))
	copy(privateKey, saveSecretKeyToByte(sk))
	copy(relinKey, saveRelinKeyToByte(rlk))
}

//export decryptNative
func decryptNative(givenCipher, givenPrivateKey string) float64 {
	params := getParams()
	loadedCipher := loadCipher(givenCipher, params)
	secretKey := loadPrivateKey(givenPrivateKey, params)
	decryptor := ckks.NewDecryptor(params, secretKey)
	decoder := ckks.NewEncoder(params)
	plaintext := decryptor.DecryptNew(loadedCipher)

	result := decoder.Decode(plaintext, params.LogSlots())

	return real(result[0])
}

func main() {}
