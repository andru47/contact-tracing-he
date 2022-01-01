//
//  BackgroundDecryptor.swift
//  Runner
//
//  Created by Andru Stefanescu on 14.12.2021.
//

import Foundation

class BackgroundDecryptor {
    private static let bridge: HeBridgeWrapper = HeBridgeWrapper()
    
    public static func getDistances(userId: String, privateKey: String) {
        DispatchQueue.global(qos: .background).async {
            while(true) {
                let givenCiphertexts : Array<NewDistanceMessage> = ConnectionService.getDistances(userId: userId)
                if (givenCiphertexts.isEmpty) {
                    return
                }
                NSLog("I have received %d ciphertexts", givenCiphertexts.count)
                wasInContact(givenCiphertexts: givenCiphertexts, privateKey: privateKey, userId: userId)
            }
        }
    }
    
    private static func wasInContact(givenCiphertexts: Array<NewDistanceMessage>, privateKey: String, userId: String) {
        for distanceMessage in givenCiphertexts {
            NSLog("Start timestamp \(distanceMessage.timestamp)")
            NSLog("End timestamp \(distanceMessage.timestampEnd)")
            NSLog("Contact user id \(distanceMessage.contactUserId)")
            NSLog("Current ciphertext size is \(distanceMessage.ciphertext.count)")
            
            var initialResult: Double = bridge.decrypt(distanceMessage.ciphertext, privateKey: privateKey)
            let altitudeDifference: Double = bridge.decrypt(distanceMessage.altitudeDifference, privateKey: privateKey)
            
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
}
