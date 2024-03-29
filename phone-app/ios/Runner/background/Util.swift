import Foundation

class Util {
    private static let SHARED_PREFERENCES_UID_KEY: String = "uuid"
    private static let SHARED_PREFERENCES_ISO_END_KEY: String = "isolation-end"
    private static let SHARED_PREFERENCES_KEYS_CREATED = "are-keys-created"
    private static var uuid: String? = nil
    private static var privateKey: String? = nil
    private static var publicKey: String? = nil
    private static var isolationEnd: UInt64? = nil
    private static let uuidSem: DispatchSemaphore = DispatchSemaphore(value: 1)
    private static var fcmToken: String? = nil
    
    public static func getUuid() -> String {
        uuidSem.wait()
        if (uuid == nil) {
            uuid = getOrUpdateSharedPrefIfNotPresent()
        }
        uuidSem.signal()
        
        return uuid!
    }
    
    public static func getPrivateKey() -> String {
        if (privateKey == nil) {
            var fileName: String = ""
            if (Config.getEncryptionType() == EncryptionType.MULTI_KEY) {
                fileName = "privateKey.bin"
            } else if (Config.getEncryptionType() == EncryptionType.LATTIGO) {
                fileName = "privateKeyLattigo"
            } else if (Config.getEncryptionType() == EncryptionType.SEAL) {
                fileName = "privateKey"
            } else {
                fileName = "privateKeySMKHE"
            }
            privateKey = readKey(fileName: fileName)
        }
        
        return privateKey!
    }
    
    public static func getPublicKey() -> String {
        if (publicKey == nil) {
            var fileName: String = ""
            if (Config.getEncryptionType() == EncryptionType.MULTI_KEY) {
                fileName = "pubKey.bin"
            } else if (Config.getEncryptionType() == EncryptionType.LATTIGO) {
                fileName = "pubKeyLattigo"
            } else if (Config.getEncryptionType() == EncryptionType.SEAL) {
                fileName = "pubKey"
            } else {
                fileName = "pubKeySMKHE"
            }
            publicKey = readKey(fileName: fileName)
        }
        
        return publicKey!
    }
    
    public static func generateKeys() {
        if (getAreKeysCreated()) {
            return;
        }
        let wrapper: HeBridgeWrapper = HeBridgeWrapper()
        wrapper.generateKeys()
        let privateKey: String = wrapper.getPrivateKey()
        let publicKey: String = wrapper.getPublicKey()
        let rlk: String = wrapper.getRelinKey()
        let mkPublicKey: String = wrapper.getMKPublicKey()
        ConnectionService.sendNewKeys(keys: NewKeysMessage(pubKey: mkPublicKey, relinKey: rlk, userId: getUuid()))
        writeKeyTo(name: "privateKey.bin", value: privateKey)
        writeKeyTo(name: "pubKey.bin", value: publicKey)
        setAreKeysCreated(value: true)
    }
    
    private static func writeKeyTo(name: String, value: String) {
        let path = FileManager.default.urls(for: .documentDirectory, in: .userDomainMask)[0].appendingPathComponent(name)
        try? value.data(using: .utf8)?.write(to: path)
    }
    
    private static func readGeneratedKey(name: String) -> String {
        let path = FileManager.default.urls(for: .documentDirectory, in: .userDomainMask)[0].appendingPathComponent(name)
        return try! String(contentsOf: path, encoding: .utf8)
    }
    
    private static func setAreKeysCreated(value: Bool) {
        let preferences = UserDefaults.standard
        preferences.set(value, forKey: SHARED_PREFERENCES_KEYS_CREATED)
    }
    
    private static func getAreKeysCreated() -> Bool {
        let preferences = UserDefaults.standard
        let storedKeys: Bool? = preferences.bool(forKey: SHARED_PREFERENCES_KEYS_CREATED)
        if (storedKeys == nil) {
            return false
        } else {
            return storedKeys!
        }
    }
    
    private static func readKey(fileName: String) -> String {
        if (Config.getEncryptionType() == EncryptionType.MULTI_KEY) {
            if (getAreKeysCreated()) {
                return readGeneratedKey(name: fileName)
            }
            generateKeys()
            return readKey(fileName: fileName)
        }
        let path = Bundle.main.path(forResource: fileName, ofType: "bin")!
        
        return try! String(contentsOfFile: path, encoding: .utf8)
    }
    
    private static func getOrUpdateSharedPrefIfNotPresent() -> String {
        let preferences = UserDefaults.standard
        let savedUid: String? = preferences.string(forKey: SHARED_PREFERENCES_UID_KEY)
        if (savedUid == nil) {
            let generatedId: String = UUID().uuidString
            NSLog("Generated uuid \(generatedId)")
            preferences.set(generatedId, forKey: SHARED_PREFERENCES_UID_KEY)
            
            return generatedId
        } else {
            return savedUid!
        }
    }
    
    public static func getIsolationEnd(appDelegate: AppDelegate) -> UInt64 {
        if (isolationEnd == nil) {
            isolationEnd = UserDefaults.standard.object(forKey: SHARED_PREFERENCES_ISO_END_KEY) as? UInt64
        }
        
        if (isolationEnd == nil) {
            return 0
        } else {
            if (isolationEnd! < UInt64(NSDate().timeIntervalSince1970)) {
                setIsolationEnd(isolationEnd: 0)
                isolationEnd = 0
            }
            return isolationEnd!
        }
    }
    
    public static func setIsolationEnd(isolationEnd: UInt64) {
        NSLog("Got isolation end \(isolationEnd)")
        UserDefaults.standard.set(isolationEnd, forKey: SHARED_PREFERENCES_ISO_END_KEY)
        self.isolationEnd = isolationEnd
    }
    
    public static func getFcmToken() -> String {
        if (fcmToken == nil) {
            fcmToken = UserDefaults.standard.string(forKey: "fcmToken") ?? ""
        }
        
        return fcmToken!
    }
    
    public static func setFcmToken(givenToken: String) {
        fcmToken = givenToken
        UserDefaults.standard.set(fcmToken, forKey: "fcmToken")
    }
}
