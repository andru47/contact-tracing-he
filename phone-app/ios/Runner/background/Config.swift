import Foundation

class Config {
    private static let ENCRYPTION_TYPE: EncryptionType = EncryptionType.LATTIGO
    private static let uploadTestLocationsEnabled: Bool = false
    private static let accuracyMeasurementEnabled: Bool = false
    private static let encryptionTimingEnabled: Bool = false
    private static let epsilonDP = 0.01
    private static let movementTime: Double = 5000, storageTime: Double = 1000 * 30
    private static let distance = 5.0
    
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
    
    public static func getDistance() -> Double {
        return distance
    }
    
    public static func getStorageTime() -> Double {
        return storageTime
    }
    
    public static func getMovementTime() -> Double {
        return movementTime
    }
}

enum EncryptionType {
    case SEAL, LATTIGO, SMKHE, MULTI_KEY
}
