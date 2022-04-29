import Foundation

struct LocationUploadMessage: Codable {
    let latitudeCos, latitudeSin, longitudeCos, longitudeSin, altitude, id, timestamp: String
}
