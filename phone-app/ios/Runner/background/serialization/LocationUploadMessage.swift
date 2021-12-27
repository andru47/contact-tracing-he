//
//  LocationUploadMessage.swift
//  Runner
//
//  Created by Andru Stefanescu on 14.12.2021.
//

import Foundation

struct LocationUploadMessage: Codable {
    let latitudeCos, latitudeSin, longitudeCos, longitudeSin, altitude, id, timestamp: String
}
