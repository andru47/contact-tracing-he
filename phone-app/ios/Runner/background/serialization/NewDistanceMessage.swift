//
//  NewDistanceMessage.swift
//  Runner
//
//  Created by Andru Stefanescu on 14.12.2021.
//

import Foundation

struct NewDistanceMessage: Codable {
    let ciphertext, altitudeDifference, contactUserId: String;
    let timestamp, timestampEnd: UInt64;
}
