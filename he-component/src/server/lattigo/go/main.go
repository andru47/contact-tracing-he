package main

import (
	"C"

	"github.com/ldsec/lattigo/v2/ckks"
	"github.com/ldsec/lattigo/v2/rlwe"
)

// func getCipher(value float64, encoder ckks.Encoder, encryptor ckks.Encryptor, level uint64) *ckks.Ciphertext {
// 	return encryptor.EncryptFastNew(encoder.EncodeNTTAtLvlNew(level, []complex128{complex(value, 0)}, 12))
// }

// func computeDistance(lat1, long1, lat2, long2 float64) float64 {
// 	cosLat1 := math.Cos(lat1 * math.Pi / 180.0)
// 	cosLong1 := math.Cos(long1 * math.Pi / 180.0)
// 	cosLat2 := math.Cos(lat2 * math.Pi / 180.0)
// 	cosLong2 := math.Cos(long2 * math.Pi / 180.0)
// 	sinLat1 := math.Sin(lat1 * math.Pi / 180.0)
// 	sinLong1 := math.Sin(long1 * math.Pi / 180.0)
// 	sinLat2 := math.Sin(lat2 * math.Pi / 180.0)
// 	sinLong2 := math.Sin(long2 * math.Pi / 180.0)

// 	// params, err := ckks.NewParametersFromLiteral(ckks.ParametersLiteral{
// 	// 	LogN:     14,
// 	// 	LogQ:     []uint64{60, 60, 60, 60, 60, 60, 60, 60, 60, 60},
// 	// 	LogP:     []uint64{40},
// 	// 	Sigma:    rlwe.DefaultSigma,
// 	// 	LogSlots: 12,
// 	// 	Scale:    float64(1 << 40)})
// 	params, err := ckks.NewParametersFromLiteral(ckks.ParametersLiteral{
// 		LogN:     14,
// 		Q:        []uint64{1152921504606748673, 576460752308273153, 576460752302473217, 576460752304439297},
// 		P:        []uint64{576460752302080001},
// 		Sigma:    rlwe.DefaultSigma,
// 		LogSlots: 12,
// 		Scale:    float64(1 << 57)})
// 	if err != nil {
// 		panic(err)
// 	}

// 	prng, err := utils.NewKeyedPRNG([]byte{'l', 'a', 't', 't', 'i', 'g', 'o'})

// 	if err != nil {
// 		panic(err)
// 	}
// 	crs := mkrlwe.GenCommonPublicParam(&params.Parameters, prng)
// 	keys1 := mkrlwe.KeyGen(&params.Parameters, crs)

// 	//create an encryptor from the first component of the public key
// 	pk1 := new(rlwe.PublicKey)
// 	pk1.Value[0] = keys1.PublicKey.Key[0].Poly[0] // b[0]
// 	pk1.Value[1] = keys1.PublicKey.Key[1].Poly[0] // a[0]

// 	keys2 := mkrlwe.KeyGen(&params.Parameters, crs)

// 	//create an encryptor from the first component of the public key
// 	pk2 := new(rlwe.PublicKey)
// 	pk2.Value[0] = keys2.PublicKey.Key[0].Poly[0] // b[0]
// 	pk2.Value[1] = keys2.PublicKey.Key[1].Poly[0] // a[0]

// 	encryptor2 := ckks.NewEncryptorFromPk(params, pk2)

// 	encoder2 := ckks.NewEncoder(params)
// 	decryptor2 := mkrlwe.NewMKDecryptor(&params.Parameters)

// 	encryptor1 := ckks.NewEncryptorFromPk(params, pk1)
// 	encoder1 := ckks.NewEncoder(params)
// 	decryptor1 := mkrlwe.NewMKDecryptor(&params.Parameters)

// 	cosLat1c := getCipher(cosLat1, encoder1, encryptor1, params.MaxLevel())
// 	cosLong1c := getCipher(cosLong1, encoder1, encryptor1, params.MaxLevel())
// 	sinLat1c := getCipher(sinLat1, encoder1, encryptor1, params.MaxLevel())
// 	sinLong1c := getCipher(sinLong1, encoder1, encryptor1, params.MaxLevel())
// 	cosLat2c := getCipher(cosLat2, encoder2, encryptor2, params.MaxLevel())
// 	cosLong2c := getCipher(cosLong2, encoder2, encryptor2, params.MaxLevel())
// 	sinLat2c := getCipher(sinLat2, encoder2, encryptor2, params.MaxLevel())
// 	sinLong2c := getCipher(sinLong2, encoder2, encryptor2, params.MaxLevel())

// 	evaluator := mkckks.NewMKEvaluator(&params)
// 	evk1 := keys1.RelinKey
// 	data, errr := evk1.MarshalBinary()
// 	println(len(data))
// 	if errr != nil {
// 		panic(errr)
// 	}
// 	pubkey1 := keys1.PublicKey
// 	evk2 := keys2.RelinKey
// 	pubkey2 := keys2.PublicKey
// 	ids := []uint64{1, 1, 1, 1, 2, 2, 2, 2}
// 	evk1.PeerID = 1
// 	evk2.PeerID = 2
// 	pubkey1.PeerID = 1
// 	pubkey2.PeerID = 2
// 	evalKeys := []*mkrlwe.MKRelinearizationKey{evk1, evk2}
// 	pubKeys := []*mkrlwe.MKPublicKey{pubkey1, pubkey2}
// 	// convert the ckks ciphertexts into multi key ciphertexts
// 	ciphers := evaluator.ConvertToMKCiphertext([]*ckks.Ciphertext{cosLat1c, cosLong1c, sinLat1c, sinLong1c,
// 		cosLat2c, cosLong2c, sinLat2c, sinLong2c}, ids)
// 	//println(evalKeys, pubKeys, ciphers)

// 	cosLatProd := evaluator.Mul(ciphers[0], ciphers[4])
// 	evaluator.RelinInPlace(cosLatProd, evalKeys, pubKeys)
// 	evaluator.Rescale(cosLatProd, cosLatProd)

// 	sinLatProd := evaluator.Mul(ciphers[2], ciphers[6])
// 	evaluator.RelinInPlace(sinLatProd, evalKeys, pubKeys)
// 	evaluator.Rescale(sinLatProd, sinLatProd)

// 	// // cosLatProd.Ciphertexts.SetScale(sinLatProd.Ciphertexts.Scale())
// 	// //evaluator.DropLevel(cosLatProd, sinLatProd.Ciphertexts.Level())
// 	havLat := evaluator.Add(cosLatProd, sinLatProd)
// 	havLat = evaluator.Neg(havLat)
// 	havLat = evaluator.AddPlaintext(encoder1.EncodeNTTAtLvlNew(havLat.Ciphertexts.Level(), []complex128{complex(1.0, 0)}, 12), havLat)

// 	cosLongProd := evaluator.Mul(ciphers[1], ciphers[5])
// 	evaluator.RelinInPlace(cosLongProd, evalKeys, pubKeys)
// 	evaluator.Rescale(cosLongProd, cosLongProd)

// 	sinLongProd := evaluator.Mul(ciphers[3], ciphers[7])
// 	evaluator.RelinInPlace(sinLongProd, evalKeys, pubKeys)
// 	evaluator.Rescale(sinLongProd, sinLongProd)

// 	havLong := evaluator.Add(cosLongProd, sinLongProd)
// 	havLong = evaluator.Neg(havLong)
// 	havLong = evaluator.AddPlaintext(encoder1.EncodeNTTAtLvlNew(havLong.Ciphertexts.Level(), []complex128{complex(1.0, 0)}, 12), havLong)

// 	newHavLong := evaluator.Mul(havLong, cosLatProd)
// 	evaluator.RelinInPlace(newHavLong, evalKeys, pubKeys)
// 	evaluator.Rescale(newHavLong, newHavLong)

// 	// evaluator.DropLevel(havLat, 2)
// 	println(newHavLong.Ciphertexts.Level())
// 	println(havLat.Ciphertexts.Level())
// 	res := evaluator.Add(newHavLong, havLat)

// 	resCKKS := evaluator.ConvertToCKKSCiphertext(res)

// 	// Partial Decryption done by each participants
// 	ckksCipher1 := resCKKS[0]
// 	ckksCipher2 := resCKKS[1]

// 	part1 := decryptor1.PartDec(&ckksCipher1.El().Element, ckksCipher1.Level(), keys1.SecretKey, 6.0)
// 	part2 := decryptor2.PartDec(&ckksCipher2.El().Element, ckksCipher2.Level(), keys2.SecretKey, 6.0)

// 	// Final decryption using the partial shares
// 	decrypted := decryptor1.MergeDec(&ckksCipher1.El().Element, ckksCipher1.Level(), []*ring.Poly{part1, part2})

// 	// decode
// 	pt := ckks.NewPlaintext(params, ckksCipher1.Level(), ckksCipher1.Scale())
// 	pt.SetValue(decrypted)

// 	finalValues := encoder1.Decode(pt, params.LogSlots())
// 	println(real(finalValues[0]))
// 	println((1 - cosLong1*cosLong2 - sinLong1*sinLong2) * cosLat1 * cosLat2)
// 	number := math.Sqrt(real(finalValues[0]) / 2.0)
// 	println(math.Asin(number) * 6378.8 * 2.0 * 1000)
// 	println(math.Asin(math.Sqrt(((-cosLong1*cosLong2-sinLong1*sinLong2+1)*cosLat1*cosLat2+1-cosLat1*cosLat2-sinLat1*sinLat2)/2.0)) * 6378.8 * 2 * 1000)
// 	return 0
// }

func loadCipher(cipherString string, params ckks.Parameters) *ckks.Ciphertext {
	ciphertext := ckks.NewCiphertext(params, 1, 3, float64(1<<57))
	err := ciphertext.UnmarshalBinary([]byte(cipherString))

	//TODO - Add logging
	if err != nil {
		println("Error loading ciphertext")
	}

	return ciphertext
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
