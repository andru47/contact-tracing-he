import Foundation
import ObjectBox

class LocationEntity: Entity, Codable {
    var id : Id = 0
    var latitude: Double = 0
    var longitude: Double = 0
    var locationTimestamp: UInt64 = 0
}
