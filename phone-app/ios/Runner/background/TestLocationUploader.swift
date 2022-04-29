import Foundation

class TestLocationUploader {
    private static let fileName: String = "test-locations"
    
    public static func readAsset() -> String {
        let path = Bundle.main.path(forResource: fileName, ofType: "json")!
        
        return try! String(contentsOfFile: path, encoding: .utf8)
    }
    
    private static func getLocations() -> [NewSavedLocations] {
        let jsonString: String = readAsset()
        return try! JSONDecoder().decode([NewSavedLocations].self, from: jsonString.data(using: .utf8)!)
    }
    
    public static func readLocationsAndUploadEncrypted() {
        var locations: [NewSavedLocations] = getLocations()
        
        for index in locations.indices {
            locations[index].mean = Double(index)
        }
        let wrapper: HeBridgeWrapper = HeBridgeWrapper()
        let pubKey: String = Util.getPublicKey()
        for location in locations {
            DispatchQueue.global().async {
                let latCos: Double = cos(Double.pi * location.latitude2 / 180.0)
                let latSin: Double = sin(Double.pi * location.latitude2 / 180.0)
                let longCos: Double = cos(Double.pi * location.longitude2 / 180.0)
                let longSin: Double = sin(Double.pi * location.longitude2 / 180.0)
                let ciphers: Array<String> = wrapper.encrypt(latCos, latSin: latSin, longCos: longCos, longSin: longSin, alt: 1.0, pubKey: pubKey)
                ConnectionService.sendNewLocation(message: LocationUploadMessage(latitudeCos: ciphers[0], latitudeSin: ciphers[1], longitudeCos: ciphers[2], longitudeSin: ciphers[3], altitude: ciphers[4], id: "second", timestamp:String(format: "%lu", UInt64(location.mean))))
            }
        }
    }
    
    public static func readLocationsAndCalculateAccuracy() {
        let locations: [NewSavedLocations] = getLocations()
        var startTimestampEncrypt: Double = 0.0
        var startTimestampDecrypt: Double = 0.0
        var timingsEncrypt: [Double] = []
        var timingsDecrypt: [Double] = []
        var measurements: [Double] = []
        let bridge: HeBridgeWrapper = HeBridgeWrapper()
        let pubKey: String = Util.getPublicKey()
        let privateKey: String = Util.getPrivateKey()
        
        DispatchQueue.global().async {
            if (Config.isEncryptionTimingEnabled()) {
                bridge.encrypt(0, latSin: 0, longCos: 0, longSin: 0, alt: 1.0, pubKey: pubKey) // Used to load pubKey
                for location in locations {
                    let latCos1: Double = cos(Double.pi * location.latitude1 / 180.0)
                    let latSin1: Double = sin(Double.pi * location.latitude1 / 180.0)
                    let longCos1: Double = cos(Double.pi * location.longitude1 / 180.0)
                    let longSin1: Double = sin(Double.pi * location.longitude1 / 180.0)
                    let latCos2: Double = cos(Double.pi * location.latitude2 / 180.0)
                    let latSin2: Double = sin(Double.pi * location.latitude2 / 180.0)
                    let longCos2: Double = cos(Double.pi * location.longitude2 / 180.0)
                    let longSin2: Double = sin(Double.pi * location.longitude2 / 180.0)
                    startTimestampEncrypt = Date().timeIntervalSince1970 * 1000
                    bridge.encrypt(latCos1, latSin: latSin1, longCos: longCos1, longSin: longSin1, alt: 1.0, pubKey: pubKey)
                    timingsEncrypt.append(Date().timeIntervalSince1970 * 1000 - startTimestampEncrypt)
                    
                    startTimestampEncrypt = Date().timeIntervalSince1970 * 1000
                    bridge.encrypt(latCos2, latSin: latSin2, longCos: longCos2, longSin: longSin2, alt: 1.0, pubKey: pubKey)
                    timingsEncrypt.append(Date().timeIntervalSince1970 * 1000 - startTimestampEncrypt)
                }
            }
            
            if (Config.isAccuracyMeasurementEnabled()) {
                var outputCSV: String = "distance,dec_distance,abs_diff\n"
                let partialDecryption: Bool = false
                let cipher = bridge.encrypt(0, latSin: 0, longCos: 0, longSin: 0, alt: 1.0, pubKey: pubKey)[0] // Used to load pubKey
                bridge.decrypt(cipher, privateKey: privateKey) // Used to load privateKey
                var givenCiphertexts : Array<NewDistanceMessage> = ConnectionService.getDistances(userId: "first", partial: partialDecryption)
                givenCiphertexts.append(contentsOf: ConnectionService.getDistances(userId: "first", partial: partialDecryption))
                
                for ciphertext in givenCiphertexts {
                    if (!partialDecryption) {
                        var initialResult: Double = 0
                        var alt: Double = 0
                        startTimestampDecrypt = Date().timeIntervalSince1970 * 1000
                        if (Config.getEncryptionType() == EncryptionType.MULTI_KEY) {
                            initialResult = Double(truncating: bridge.decryptMulti(ciphertext.ciphertext, partialCipher: ciphertext.partialDistance, privateKey: privateKey, isFinal: true) as! NSNumber)
                            alt = Double(truncating: bridge.decryptMulti(ciphertext.altitudeDifference, partialCipher: ciphertext.partialAltitudeDifference, privateKey: privateKey, isFinal: true) as! NSNumber)
                        } else {
                            initialResult = bridge.decrypt(ciphertext.ciphertext, privateKey: privateKey)
                            alt = bridge.decrypt(ciphertext.altitudeDifference, privateKey: privateKey)
                        }
                        timingsDecrypt.append(Date().timeIntervalSince1970 * 1000 - startTimestampDecrypt)
                        initialResult = asin(sqrt(initialResult / 2.0)) * 6371 * 2.0 * 1000
                        
                        if (initialResult.isNaN) {
                            initialResult = 0
                        }
                        outputCSV += "\(locations[Int(ciphertext.timestamp)].distance),\(initialResult),\(abs(initialResult - locations[Int(ciphertext.timestamp)].distance))\n"
                        measurements.append(abs(initialResult - locations[Int(ciphertext.timestamp)].distance))
                    } else {
                        startTimestampDecrypt = Date().timeIntervalSince1970 * 1000
                        let partialStringDistance: String = bridge.decryptMulti(ciphertext.ciphertext, partialCipher: "", privateKey: privateKey, isFinal: false) as! String
                        timingsDecrypt.append(Date().timeIntervalSince1970 * 1000 - startTimestampDecrypt)
                        startTimestampDecrypt = Date().timeIntervalSince1970 * 1000
                        let partialStringAltitudeDifference: String = bridge.decryptMulti(ciphertext.altitudeDifference, partialCipher: "", privateKey: privateKey, isFinal: false) as! String
                        timingsDecrypt.append(Date().timeIntervalSince1970 * 1000 - startTimestampDecrypt)
                        
                        ConnectionService.sendNewPartial(partialMessage: NewPartialMessage(partialDistance: partialStringDistance, partialAltitudeDifference: partialStringAltitudeDifference, rowId: ciphertext.rowId))
                    }
                }
                print(outputCSV)
            }
            
            logMeasurements(timings: timingsEncrypt, name: "ENCRYPT")
            logMeasurements(timings: timingsDecrypt, name: "DECRYPT")
            logMeasurements(timings: measurements, name: "ACCURACY")
        }
    }
    
    private static func logMeasurements(timings: [Double], name: String) {
        let sum: Double = timings.reduce(0.0, +)
        let mean: Double = sum / Double(timings.count)
        var stDev: Double = 0.0
        for timing in timings {
            stDev += pow(timing - mean, 2.0)
        }
        
        stDev = sqrt(stDev / Double(timings.count))
        NSLog("Finished \(name): MEAN \(mean) ST DEV \(stDev) SAMPLES \(timings.count)")
    }
}
