import Foundation

struct JSONBody: Codable {
    let latitudeCos1, latitudeSin1, longitudeCos1, longitudeSin1: String;
    let latitudeCos2, latitudeSin2, longitudeCos2, longitudeSin2: String;
    let rlk: String;
}
