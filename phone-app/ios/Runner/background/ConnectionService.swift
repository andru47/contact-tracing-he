import Foundation

class ConnectionService {
    private static let URL_STRING: String = "http://127.0.0.1:8080/"
    
    public static func getDistances(userId: String, partial: Bool) -> Array<NewDistanceMessage> {
        var url: String = URL_STRING
        if (partial) {
            url += "get-computed-distances-for-partial/"
        } else {
            url += "get-computed-distances/"
        }
        
        url += userId
        
        let request: URLRequest = URLRequest(url: URL(string: url)!)
        var jsonResponse: Data = Data()
        let lock: NSLock = NSLock()
        lock.lock()
        let task = URLSession.shared.dataTask(with: request) { data, response, error in
            jsonResponse = data!
//            NSLog("Received %@ from server for new distances", String(decoding: jsonResponse, as: UTF8.self))
            lock.unlock()
        }
        task.resume()
        lock.lock()
        return try! JSONDecoder().decode([NewDistanceMessage].self, from: jsonResponse)
    }
    
    private static func postObject(json: Data, endpoint: String) {
        DispatchQueue.global(qos: .background).async {
            var request: URLRequest = URLRequest(url: URL(string: URL_STRING + endpoint)!)
            request.httpMethod = "POST"
            request.httpBody = json
            request.timeoutInterval = 180
            request.addValue("application/json", forHTTPHeaderField: "Content-Type")
            request.addValue("application/json", forHTTPHeaderField: "Accept")
            let lock: NSLock = NSLock()
            
            lock.lock()
            let task = URLSession.shared.dataTask(with: request) { data, response, error in
                lock.unlock()
            }
            task.resume()
            lock.lock()
        }
    }
    
    private static func postObjectWithResp(json: Data, endpoint: String) -> String {
        var request: URLRequest = URLRequest(url: URL(string: URL_STRING + endpoint)!)
        var resp:String = ""
        request.httpMethod = "POST"
        request.httpBody = json
        request.timeoutInterval = 180
        request.addValue("application/json", forHTTPHeaderField: "Content-Type")
        request.addValue("application/json", forHTTPHeaderField: "Accept")
        let lock: NSLock = NSLock()
        
        lock.lock()
        let task = URLSession.shared.dataTask(with: request) { data, response, error in
            resp = String(decoding: data!, as: UTF8.self)
            lock.unlock()
        }
        task.resume()
        lock.lock()
        return resp
    }
    
    public static func sendJSONBody(message: JSONBody) -> String {
        let json: Data = try! JSONEncoder().encode(message)
        
        return postObjectWithResp(json: json, endpoint: "distance-calculator")
    }
    
    public static func sendNewLocation(message: LocationUploadMessage) {
        let json: Data = try! JSONEncoder().encode(message)
        
        postObject(json: json, endpoint: "upload-location")
    }
    
    public static func sendNewToken(token: NewTokenMessage) {
        let json: Data = try! JSONEncoder().encode(token)
        
        postObject(json: json, endpoint: "upload-fcm-token")
    }
    
    public static func sendNewContact(contact: ContactMessage) {
        let json: Data = try! JSONEncoder().encode(contact)
        
        postObject(json: json, endpoint: "new-contact")
    }
    
    public static func sendNewKeys(keys: NewKeysMessage) {
        let json: Data = try! JSONEncoder().encode(keys)
        
        postObject(json: json, endpoint: "new-user-keys")
    }
    
    public static func sendNewPartial(partialMessage: NewPartialMessage) {
        let json: Data = try! JSONEncoder().encode(partialMessage)
        
        postObject(json: json, endpoint: "new-partial-distance")
    }
    
    public static func sendLocationHistory(message: [LocationEntity]) {
        let json: Data = try! JSONEncoder().encode(message)
        postObject(json: json, endpoint: "upload-location-history")
    }
}
