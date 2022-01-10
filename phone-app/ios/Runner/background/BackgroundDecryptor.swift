//
//  BackgroundDecryptor.swift
//  Runner
//
//  Created by Andru Stefanescu on 14.12.2021.
//

import Foundation

class BackgroundDecryptor {
    private static let bridge: HeBridgeWrapper = HeBridgeWrapper()
    
    public static func getDistances(userId: String, privateKey: String, partial: Bool) {
        DispatchQueue.global(qos: .background).async {
            while(true) {
                let givenCiphertexts : Array<NewDistanceMessage> = ConnectionService.getDistances(userId: userId, partial: partial)
                if (givenCiphertexts.isEmpty) {
                    return
                }
                NSLog("I have received %d ciphertexts", givenCiphertexts.count)
                if (partial) {
                    computePartial(givenCiphertexts: givenCiphertexts, privateKey: privateKey, userId: userId)
                } else {
                    wasInContact(givenCiphertexts: givenCiphertexts, privateKey: privateKey, userId: userId)
                }
            }
        }
    }
    
    private static func wasInContact(givenCiphertexts: Array<NewDistanceMessage>, privateKey: String, userId: String) {
        for distanceMessage in givenCiphertexts {
            NSLog("Start timestamp \(distanceMessage.timestamp)")
            NSLog("End timestamp \(distanceMessage.timestampEnd)")
            NSLog("Contact user id \(distanceMessage.contactUserId)")
            NSLog("Current ciphertext size is \(distanceMessage.ciphertext.count)")
            
            var initialResult: Double
            var altitudeDifference: Double
            
            if (Config.getEncryptionType() == EncryptionType.LATTIGO_MK) {
                initialResult = Double(truncating: bridge.decryptMulti(distanceMessage.ciphertext,partialCipher: distanceMessage.partialDistance, privateKey: privateKey, isFinal: true) as! NSNumber)
                altitudeDifference = Double(truncating: bridge.decryptMulti(distanceMessage.altitudeDifference,partialCipher: distanceMessage.partialAltitudeDifference, privateKey: privateKey, isFinal: true) as! NSNumber)
            } else {
                initialResult = bridge.decrypt(distanceMessage.ciphertext, privateKey: privateKey)
                altitudeDifference = bridge.decrypt(distanceMessage.altitudeDifference, privateKey: privateKey)
            }
            
            NSLog("Altitude difference was \(altitudeDifference)")
            initialResult = asin(sqrt(initialResult / 2.0)) * 6378.8 * 2.0 * 1000
            
            if (initialResult.isNaN) {
                initialResult = 0
            }
            
            NSLog("Distance was \(initialResult)")
            
            if (initialResult <= 6.0 && altitudeDifference * 100 < 210) {
                NSLog("Found contact \(distanceMessage.contactUserId)")
                ConnectionService.sendNewContact(contact: ContactMessage(userId: userId, infectedUserId: distanceMessage.contactUserId, timestamp: distanceMessage.timestamp, timestampEnd: distanceMessage.timestampEnd))
            }
        }
    }
    
    public static func computePartial(givenCiphertexts: Array<NewDistanceMessage>, privateKey: String, userId: String) {
        for distanceMessage in givenCiphertexts {
            NSLog("Start timestamp \(distanceMessage.timestamp)")
            NSLog("End timestamp \(distanceMessage.timestampEnd)")
            NSLog("Contact user id \(distanceMessage.contactUserId)")
            NSLog("Current ciphertext size is \(distanceMessage.ciphertext.count)")
            let partialStringDistance: String = bridge.decryptMulti(distanceMessage.ciphertext, partialCipher: "", privateKey: privateKey, isFinal: false) as! String
            let partialStringAltitudeDifference: String = bridge.decryptMulti(distanceMessage.altitudeDifference, partialCipher: "", privateKey: privateKey, isFinal: false) as! String
            ConnectionService.sendNewPartial(partialMessage: NewPartialMessage(partialDistance: partialStringDistance, partialAltitudeDifference: partialStringAltitudeDifference, rowId: distanceMessage.rowId))
        }
    }
}
