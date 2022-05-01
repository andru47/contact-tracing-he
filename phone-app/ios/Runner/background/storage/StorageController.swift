import Foundation
import ObjectBox

class StorageController {
    private static let box: Box<LocationEntity> = {
        let appSupport = try? FileManager.default.url(for: .applicationSupportDirectory,
                                                     in: .userDomainMask,
                                                     appropriateFor: nil,
                                                     create: true)
                                                     .appendingPathComponent(Bundle.main.bundleIdentifier!)
        let directory = appSupport!.appendingPathComponent("location_history")
        
        try? FileManager.default.createDirectory(at: directory,
                                                 withIntermediateDirectories: true,
                                                 attributes: nil)

        let store = try! Store(directoryPath: directory.path)
        return store.box(for: LocationEntity.self)
    }()
    
    public static func sendLocationsToServer(timestamp: UInt64) {
        self.removeAllOldLocations(givenTimestamp: timestamp)
        ConnectionService.sendLocationHistory(message: try! box.all())
    }
    
    public static func addLocation(latitude: Double, longitude: Double, timestamp: UInt64) {
        let entity:LocationEntity = LocationEntity()
        entity.latitude = latitude
        entity.longitude = longitude
        entity.locationTimestamp = timestamp
        
        try! box.put(entity)
    }
    
    public static func getAllOldLocations(givenTimestamp: UInt64) -> [LocationEntity] {
        NSLog("Timestamp is \(givenTimestamp)")
        let query: Query<LocationEntity> = try! box.query {
            LocationEntity.locationTimestamp.isLessThan(givenTimestamp + 1)
        }.build()
        return try! query.find()
    }
    
    public static func removeAllOldLocations(givenTimestamp: UInt64) {
        try! box.remove(self.getAllOldLocations(givenTimestamp: givenTimestamp - 7 * 24 * 60 * 60))
    }
}
