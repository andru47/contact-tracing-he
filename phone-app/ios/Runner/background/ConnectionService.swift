//
//  ConnectionService.swift
//  Runner
//
//  Created by Andru Stefanescu on 14.12.2021.
//

import Foundation

class ConnectionService {
    private static let URL_STRING: String = "http://192.168.1.100:8080/"
    
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
            NSLog("Received %@ from server for new distances", String(decoding: jsonResponse, as: UTF8.self))
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
}
