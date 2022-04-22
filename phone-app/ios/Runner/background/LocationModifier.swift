import Foundation
import CoreLocation

class LocationModifier {
    private static let EARTH_RADIUS_METERS: Double = 6371E3
    
    public static func perturbLocation(location: CLLocationCoordinate2D) -> CLLocationCoordinate2D {
        let epsilon: Double = Config.getEpsilon()

        
        let sampledBearing = Double.random(in: 0..<2 * Double.pi)
        let sampledProbability: Double = Double.random(in: 0..<1)
        NSLog("I have sampled \(sampledProbability)")
        let sampledLambert: Double = approximateLambertW(givenNumber: (sampledProbability - 1) / M_E)
        NSLog("I have sampled \(sampledLambert)")
        
        let radius: Double = (-1 / epsilon) * (sampledLambert + 1)
        NSLog("Radius is \(radius)")
        
        return addDistanceToLocationAndBearing(location: location, distance: abs(radius), bearing: sampledBearing)
    }
    
    private static func addDistanceToLocationAndBearing(location: CLLocationCoordinate2D, distance: Double, bearing: Double) -> CLLocationCoordinate2D {
        var latitude = location.latitude * Double.pi / 180
        var longitude = location.longitude * Double.pi / 180
        let angularDistance = distance / EARTH_RADIUS_METERS
        let lastLatitude = latitude
        
        latitude = asin(sin(latitude) * cos(angularDistance) + cos(latitude) * sin(angularDistance) * cos(bearing))
        longitude = longitude + atan2(sin(bearing) * sin(angularDistance) * cos(lastLatitude), cos(angularDistance) - sin(latitude) * sin(lastLatitude))
        
        latitude = latitude * 180 / Double.pi
        longitude = longitude * 180 / Double.pi
        NSLog("New location is \(latitude) \(longitude)")
        return CLLocationCoordinate2D(latitude: latitude, longitude: longitude)
    }
    
    private static func approximateLambertW(givenNumber: Double) -> Double {
        if (givenNumber == -1 / M_E) {
            return -1
        }
        var last: Double = 1.0
        var iter: Int = 0
        while (true) {
            iter += 1
            let new: Double = last - (last * exp(last) - givenNumber) / (exp(last) * (1 + last))
            if (abs(last - new) < 1e-10) {
                NSLog("iter is \(iter)")
                return round(last * 1e7) / (1e7)
            }
            last = new
        }
    }
}
