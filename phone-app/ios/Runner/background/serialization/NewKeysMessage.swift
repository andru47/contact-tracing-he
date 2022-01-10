//
//  NewKeysMessage.swift
//  Runner
//
//  Created by Andru Stefanescu on 07.01.2022.
//

import Foundation

struct NewKeysMessage : Codable {
    let pubKey, relinKey, userId: String;
}
