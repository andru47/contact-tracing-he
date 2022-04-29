import Foundation

struct NewDistanceMessage: Codable {
    let partialDistance, partialAltitudeDifference, rowId: String;
    let ciphertext, altitudeDifference, contactUserId: String;
    let timestamp, timestampEnd: UInt64;
}
