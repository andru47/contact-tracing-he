package main

import "C"
import (
	"github.com/ldsec/lattigo/v2/ckks"
	"github.com/ldsec/lattigo/v2/mkrlwe"
	"github.com/ldsec/lattigo/v2/ring"
	"github.com/ldsec/lattigo/v2/rlwe"
	"github.com/ldsec/lattigo/v2/utils"
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
	ciphertext := ckks.NewCiphertext(params, 1, 0, float64(1<<60))
	err := ciphertext.UnmarshalBinary([]byte(cipherString))

	if err != nil {
		println("Error loading ciphertext", err.Error())
	}

	return ciphertext
}

func getParams() ckks.Parameters {
	params, err := ckks.NewParametersFromLiteral(ckks.ParametersLiteral{
		LogN:     13,
		Q:        []uint64{1152921504605962241, 1152921504606584833, 1152921504606683137},
		P:        []uint64{0x7fffffffe0001, 0x80000001c0001, 0x80000002c0001, 0x7ffffffd20001},
		Sigma:    rlwe.DefaultSigma,
		LogSlots: 12,
		Scale:    float64(1 << 60)})

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
	relinKey := rlwe.NewRelinKey(params.Parameters, 1)

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

func saveMKPublicKeyToByte(pk *mkrlwe.MKPublicKey) []byte {
	data, err := pk.MarshalBinary()
	if err != nil {
		println("Error saving mk pubkey to string")
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

func saveRingPolyToBytes(rp *ring.Poly) []byte {
	data, err := rp.MarshalBinary()
	if err != nil {
		println("Error saving ring poly to bytes")
	}

	return data
}

func loadRingPolyFromBytes(data []byte) *ring.Poly {
	rp := new(ring.Poly)
	err := rp.UnmarshalBinary(data)
	if err != nil {
		println("Error unmarshalling ring poly", err.Error())
	}

	return rp
}

func saveMKRelinKeyToByte(rlk *mkrlwe.MKRelinearizationKey) []byte {
	data, err := rlk.MarshalBinary()
	if err != nil {
		println("Error saving mk rlk to string")
	}

	return data
}

func saveRelinKeyToByte(rlk *rlwe.RelinearizationKey) []byte {
	data, err := rlk.MarshalBinary()
	if err != nil {
		println("Error saving mk rlk to string")
	}

	return data
}

//export generateKeysNative
func generateKeysNative(publicKey, privateKey, mkRelinKey, mkPublicKey []byte) {
	params := getParams()
	prng, err := utils.NewKeyedPRNG([]byte("als.dissertation"))

	if err != nil {
		panic(err)
	}

	crs := mkrlwe.GenCommonPublicParam(&params.Parameters, prng)
	kgen := mkrlwe.KeyGen(&params.Parameters, crs)
	mkPub := kgen.PublicKey
	simplePub := new(rlwe.PublicKey)
	simplePub.Value[0] = mkPub.Key[0].Poly[0]
	simplePub.Value[1] = mkPub.Key[1].Poly[0]

	copy(publicKey, savePublicKeyToByte(simplePub))
	copy(privateKey, saveSecretKeyToByte(kgen.SecretKey.Key))
	copy(mkRelinKey, saveMKRelinKeyToByte(kgen.RelinKey))
	copy(mkPublicKey, saveMKPublicKeyToByte(mkPub))
	// kgen := ckks.NewKeyGenerator(params)

	// sk := kgen.GenSecretKey()
	// pubKeys := kgen.GenPublicKey(sk)
	// evalKeys := kgen.GenRelinearizationKey(sk)

	// copy(publicKey, savePublicKeyToByte(pubKeys))
	// copy(privateKey, saveSecretKeyToByte(sk))
	// copy(mkRelinKey, saveRelinKeyToByte(evalKeys))
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

//export decryptHalfNative
func decryptHalfNative(givenCipher, givenPrivateKey string, cipher []byte) {
	params := getParams()
	privateKey := loadPrivateKey(givenPrivateKey, params)
	mkPrivateKey := new(mkrlwe.MKSecretKey)
	mkPrivateKey.Key = privateKey
	mkPrivateKey.PeerID = 2
	decryptor := mkrlwe.NewMKDecryptor(&params.Parameters)
	toBeDecrypted := loadCipher(givenCipher, params)
	halfDecrypted := decryptor.PartDec(&toBeDecrypted.El().Element, 0, mkPrivateKey, 6.0)
	copy(cipher, saveRingPolyToBytes(halfDecrypted))
}

//export decryptFullNative
func decryptFullNative(givenCipher, givenPart2, givenPrivateKey string) float64 {
	params := getParams()
	privateKey := loadPrivateKey(givenPrivateKey, params)
	mkPrivateKey := new(mkrlwe.MKSecretKey)
	mkPrivateKey.Key = privateKey
	mkPrivateKey.PeerID = 1
	decryptor := mkrlwe.NewMKDecryptor(&params.Parameters)
	toBeDecrypted := loadCipher(givenCipher, params)
	halfResult := loadRingPolyFromBytes([]byte(givenPart2))
	currentHalfResult := decryptor.PartDec(&toBeDecrypted.El().Element, 0, mkPrivateKey, 6.0)

	polyResult := decryptor.MergeDec(&toBeDecrypted.El().Element, 0, []*ring.Poly{currentHalfResult, halfResult})
	plainResult := ckks.NewPlaintext(params, 0, toBeDecrypted.Scale())
	plainResult.SetValue(polyResult)

	decoder := ckks.NewEncoder(params)

	return real(decoder.Decode(plainResult, params.LogSlots())[0])
}

func main() {}
