//
//  NewTokenMessage.swift
//  Runner
//
//  Created by Andru Stefanescu on 14.12.2021.
//

import Foundation

struct NewTokenMessage: Codable {
    let userId, token: String
}
