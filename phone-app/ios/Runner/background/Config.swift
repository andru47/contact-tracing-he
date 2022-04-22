//
//  Config.swift
//  Runner
//
//  Created by Andru Stefanescu on 07.01.2022.
//

import Foundation

class Config {
    private static let ENCRYPTION_TYPE: EncryptionType = EncryptionType.LATTIGO
    private static let uploadTestLocationsEnabled: Bool = false
    private static let accuracyMeasurementEnabled: Bool = false
    private static let encryptionTimingEnabled: Bool = false
    private static let epsilonDP = 0.01
    
    public static func getEncryptionType() -> EncryptionType {
        return ENCRYPTION_TYPE
    }
    
    public static func isUploadTestLocationsEnabled() -> Bool {
        return uploadTestLocationsEnabled
    }
    
    public static func isAccuracyMeasurementEnabled() -> Bool {
        return accuracyMeasurementEnabled
    }
    
    public static func isEncryptionTimingEnabled() -> Bool {
        return encryptionTimingEnabled
    }
    
    public static func getEpsilon() -> Double {
        return epsilonDP
    }
}

// TODO: Refactor names
enum EncryptionType {
    case SEAL, LATTIGO, LATTIGO_MK, SMKHE
}
