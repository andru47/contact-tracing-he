package main

import (
	"C"

	"github.com/ldsec/lattigo/v2/ckks"
	"github.com/ldsec/lattigo/v2/mkckks"
	"github.com/ldsec/lattigo/v2/mkrlwe"
	"github.com/ldsec/lattigo/v2/rlwe"
)

func loadCipher(cipherString string, params ckks.Parameters) *ckks.Ciphertext {
	ciphertext := ckks.NewCiphertext(params, 1, 2, float64(1<<60))
	err := ciphertext.UnmarshalBinary([]byte(cipherString))

	//TODO - Add logging
	if err != nil {
		println("Error loading ciphertext")
	}

	return ciphertext
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

func saveCipher(givenCipher *ckks.Ciphertext) []byte {
	data, err := givenCipher.MarshalBinary()

	if err != nil {
		println("Error marshalling computed ciphertext")
	}

	return data
}

//export computeNative
func computeNative(cipher1, cipher2 []string, relinKey string, outputCipher []byte) {
	ckksParams := getParams()
	loadedCiphers1 := loadCiphers(cipher1, ckksParams)
	loadedCiphers2 := loadCiphers(cipher2, ckksParams)

	rlk := loadRelin(relinKey, ckksParams)

	evaluator := ckks.NewEvaluator(ckksParams, rlwe.EvaluationKey{Rlk: rlk})
	encoder := ckks.NewEncoder(ckksParams)

	cosLatProd := evaluator.MulRelinNew(loadedCiphers1[0], loadedCiphers2[0])
	evaluator.Rescale(cosLatProd, ckksParams.Scale(), cosLatProd)

	sinLatProd := evaluator.MulRelinNew(loadedCiphers1[1], loadedCiphers2[1])
	evaluator.Rescale(sinLatProd, ckksParams.Scale(), sinLatProd)

	havLat := evaluator.AddNew(cosLatProd, sinLatProd)
	evaluator.Neg(havLat, havLat)
	evaluator.Add(havLat, encoder.EncodeNTTAtLvlNew(havLat.Level(), []complex128{complex(1.0, 0)}, 12), havLat)

	cosLongProd := evaluator.MulRelinNew(loadedCiphers1[2], loadedCiphers2[2])
	evaluator.Rescale(cosLongProd, ckksParams.Scale(), cosLongProd)

	sinLongProd := evaluator.MulRelinNew(loadedCiphers1[3], loadedCiphers2[3])
	evaluator.Rescale(sinLongProd, ckksParams.Scale(), sinLongProd)

	havLong := evaluator.AddNew(cosLongProd, sinLongProd)
	evaluator.Neg(havLong, havLong)
	evaluator.Add(havLong, encoder.EncodeNTTAtLvlNew(havLong.Level(), []complex128{complex(1.0, 0)}, 12), havLong)

	newHavLong := evaluator.MulRelinNew(havLong, cosLatProd)
	evaluator.Rescale(newHavLong, ckksParams.Scale(), newHavLong)

	res := evaluator.AddNew(havLat, newHavLong)

	copy(outputCipher, saveCipher(res))
}

func loadMkPubKey(pubKeyString string) *mkrlwe.MKPublicKey {
	pubKey := new(mkrlwe.MKPublicKey)
	err := pubKey.UnmarshalBinary([]byte(pubKeyString))
	if err != nil {
		println("Error loading mk pubkey")
	}

	return pubKey
}

func loadMkRlk(rlkString string) *mkrlwe.MKRelinearizationKey {
	rlk := new(mkrlwe.MKRelinearizationKey)
	err := rlk.UnmarshalBinary([]byte(rlkString))
	if err != nil {
		println("Error loading mk rlk")
	}

	return rlk
}

//export computeMultiNative
func computeMultiNative(cipher1, cipher2 []string, pubKey1S, rlk1S, pubKey2S, rlk2S string, outputCipher1, outputCipher2 []byte) {
	ckksParams := getParams()
	loadedCiphers1 := loadCiphers(cipher1, ckksParams)
	loadedCiphers2 := loadCiphers(cipher2, ckksParams)
	pubKey1 := loadMkPubKey(pubKey1S)
	pubKey2 := loadMkPubKey(pubKey2S)
	rlk1 := loadMkRlk(rlk1S)
	rlk2 := loadMkRlk(rlk2S)

	evaluator := mkckks.NewMKEvaluator(&ckksParams)
	pubKey1.PeerID = 1
	pubKey2.PeerID = 2
	rlk1.PeerID = 1
	rlk2.PeerID = 2
	ids := []uint64{1, 1, 1, 1, 2, 2, 2, 2}
	evalKeys := []*mkrlwe.MKRelinearizationKey{rlk1, rlk2}
	pubKeys := []*mkrlwe.MKPublicKey{pubKey1, pubKey2}
	ciphers := evaluator.ConvertToMKCiphertext([]*ckks.Ciphertext{loadedCiphers1[0], loadedCiphers1[1], loadedCiphers1[2], loadedCiphers1[3],
		loadedCiphers2[0], loadedCiphers2[1], loadedCiphers2[2], loadedCiphers2[3]}, ids)

	encoder := ckks.NewEncoder(ckksParams)

	cosLatProd := evaluator.Mul(ciphers[0], ciphers[4])
	evaluator.RelinInPlace(cosLatProd, evalKeys, pubKeys)
	evaluator.Rescale(cosLatProd, cosLatProd)

	sinLatProd := evaluator.Mul(ciphers[1], ciphers[5])
	evaluator.RelinInPlace(sinLatProd, evalKeys, pubKeys)
	evaluator.Rescale(sinLatProd, sinLatProd)

	havLat := evaluator.Add(cosLatProd, sinLatProd)
	havLat = evaluator.Neg(havLat)
	havLat = evaluator.AddPlaintext(encoder.EncodeNTTAtLvlNew(havLat.Ciphertexts.Level(), []complex128{complex(1.0, 0)}, 12), havLat)

	cosLongProd := evaluator.Mul(ciphers[2], ciphers[6])
	evaluator.RelinInPlace(cosLongProd, evalKeys, pubKeys)
	evaluator.Rescale(cosLongProd, cosLongProd)

	sinLongProd := evaluator.Mul(ciphers[3], ciphers[7])
	evaluator.RelinInPlace(sinLongProd, evalKeys, pubKeys)
	evaluator.Rescale(sinLongProd, sinLongProd)

	havLong := evaluator.Add(cosLongProd, sinLongProd)
	havLong = evaluator.Neg(havLong)
	havLong = evaluator.AddPlaintext(encoder.EncodeNTTAtLvlNew(havLong.Ciphertexts.Level(), []complex128{complex(1.0, 0)}, 12), havLong)

	newHavLong := evaluator.Mul(havLong, cosLatProd)
	evaluator.RelinInPlace(newHavLong, evalKeys, pubKeys)
	evaluator.DropLevel(newHavLong, 1)
	evaluator.Rescale(newHavLong, newHavLong)

	res := evaluator.Add(newHavLong, havLat)

	resCKKS := evaluator.ConvertToCKKSCiphertext(res)

	ckksCipher1 := resCKKS[0]
	ckksCipher2 := resCKKS[1]

	copy(outputCipher1, saveCipher(ckksCipher1))
	copy(outputCipher2, saveCipher(ckksCipher2))
}

//export computeAltitudeDifferenceMultiNative
func computeAltitudeDifferenceMultiNative(cipher1, cipher2, pubKey1S, rlk1S, pubKey2S, rlk2S string, outputCipher1, outputCipher2 []byte) {
	ckksParams := getParams()
	loadedCipher1 := loadCipher(cipher1, ckksParams)
	loadedCipher2 := loadCipher(cipher2, ckksParams)
	pubKey1 := loadMkPubKey(pubKey1S)
	pubKey2 := loadMkPubKey(pubKey2S)
	rlk1 := loadMkRlk(rlk1S)
	rlk2 := loadMkRlk(rlk2S)

	evaluator := mkckks.NewMKEvaluator(&ckksParams)
	pubKey1.PeerID = 1
	pubKey2.PeerID = 2
	rlk1.PeerID = 1
	rlk2.PeerID = 2
	ids := []uint64{1, 2}
	_ = []*mkrlwe.MKRelinearizationKey{rlk1, rlk2}
	_ = []*mkrlwe.MKPublicKey{pubKey1, pubKey2}
	ciphers := evaluator.ConvertToMKCiphertext([]*ckks.Ciphertext{loadedCipher1, loadedCipher2}, ids)

	res := evaluator.Sub(ciphers[0], ciphers[1])
	resCKKS := evaluator.ConvertToCKKSCiphertext(res)

	ckksCipher1 := resCKKS[0]
	ckksCipher2 := resCKKS[1]

	copy(outputCipher1, saveCipher(ckksCipher1))
	copy(outputCipher2, saveCipher(ckksCipher2))
}

//export computeAltitudeDifferenceNative
func computeAltitudeDifferenceNative(cipher1, cipher2, relinKey string, outputCipher []byte) {
	ckksParams := getParams()
	loadedCipher1 := loadCipher(cipher1, ckksParams)
	loadedCipher2 := loadCipher(cipher2, ckksParams)

	rlk := loadRelin(relinKey, ckksParams)

	evaluator := ckks.NewEvaluator(ckksParams, rlwe.EvaluationKey{Rlk: rlk})

	res := evaluator.SubNew(loadedCipher1, loadedCipher2)

	copy(outputCipher, saveCipher(res))
}

func main() {}
