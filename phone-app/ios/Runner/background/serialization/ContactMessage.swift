import Foundation

struct ContactMessage: Codable {
    let userId, infectedUserId: String
    let timestamp, timestampEnd: UInt64
}
