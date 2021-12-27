//
//  ContactMessage.swift
//  Runner
//
//  Created by Andru Stefanescu on 14.12.2021.
//

import Foundation

struct ContactMessage: Codable {
    let userId, infectedUserId: String
    let timestamp, timestampEnd: UInt64
}
