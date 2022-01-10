//
//  Config.swift
//  Runner
//
//  Created by Andru Stefanescu on 07.01.2022.
//

import Foundation

class Config {
    private static let ENCRYPTION_TYPE: EncryptionType = EncryptionType.LATTIGO_MK
    
    public static func getEncryptionType() -> EncryptionType {
        return ENCRYPTION_TYPE
    }
}

enum EncryptionType {
    case SEAL, LATTIGO, LATTIGO_MK
}
