import Foundation

struct NewKeysMessage : Codable {
    let pubKey, relinKey, userId: String;
}
