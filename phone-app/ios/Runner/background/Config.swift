//
//  Config.swift
//  Runner
//
//  Created by Andru Stefanescu on 07.01.2022.
//

import Foundation

class Config {
    private static let ENCRYPTION_TYPE: EncryptionType = EncryptionType.LATTIGO_MK
    private static let uploadTestLocationsEnabled: Bool = false
    private static let accuracyMeasurementEnabled: Bool = false
    private static let encryptionTimingEnabled: Bool = false
    
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
}

// TODO: Refactor names
enum EncryptionType {
    case SEAL, LATTIGO, LATTIGO_MK, SMKHE
}
