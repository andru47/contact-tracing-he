//
//  Util.swift
//  Runner
//
//  Created by Andru Stefanescu on 14.12.2021.
//

import Foundation

class Util {
    private static let  SHARED_PREFERENCES_UID_KEY: String = "uuid"
    private static let  SHARED_PREFERENCES_ISO_END_KEY: String = "isolation-end"
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
            privateKey = readKey(fileName: "privateKey")
        }
        
        return privateKey!
    }
    
    public static func getPublicKey() -> String {
        if (publicKey == nil) {
            publicKey = readKey(fileName: "pubKey")
        }
        
        return publicKey!
    }
    
    private static func readKey(fileName: String) -> String {
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
    
    public static func getIsolationEnd() -> UInt64 {
        if (isolationEnd == nil) {
            isolationEnd = UserDefaults.standard.object(forKey: SHARED_PREFERENCES_ISO_END_KEY) as? UInt64
        }
        
        if (isolationEnd == nil) {
            return 0
        } else {
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
