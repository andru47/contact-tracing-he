import UIKit
import Flutter
import CoreLocation
import Firebase

@UIApplicationMain
@objc class AppDelegate: FlutterAppDelegate {
  private final let CHANNEL : String = "BRIDGE"
  private final let bridge: HeBridgeWrapper = HeBridgeWrapper()
  let locationManager = CLLocationManager()
  var lastTimestamp: Date? = nil
  
  override func application(
    _ application: UIApplication,
    didRegisterForRemoteNotificationsWithDeviceToken deviceToken: Data
  ) {
    let tokenParts = deviceToken.map { data in String(format: "%02.2hhx", data) }
    let token = tokenParts.joined()
    print("Device Token: \(token)")
    Messaging.messaging().apnsToken = deviceToken
  }
  
  override func application(
    _ application: UIApplication,
    didFailToRegisterForRemoteNotificationsWithError error: Error
  ) {
    print("Failed to register: \(error)")
  }
  
  override func application(
    _ application: UIApplication,
    didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?
  ) -> Bool {
    
    GeneratedPluginRegistrant.register(with: self)

//    if (Config.isUploadTestLocationsEnabled()) {
//      TestLocationUploader.readLocationsAndUploadEncrypted()
//    } else if (Config.isAccuracyMeasurementEnabled()) {
//      TestLocationUploader.readLocationsAndCalculateAccuracy()
//    } else {
//      registerForNotifications(application: application)
//      registerForLocation()
//    }
    let latitudeCos: Double = cos(Double.pi * 54.13557822 / 180.0)
    let latitudeSin: Double = sin(Double.pi * 54.13557822 / 180.0)
    let longitudeCos: Double = cos(Double.pi * -1.281623256 / 180.0)
    let longitudeSin: Double = sin(Double.pi * -1.281623256 / 180.0)
    let latitudeCos2: Double = cos(Double.pi * 54.13571531 / 180.0)
    let latitudeSin2: Double = sin(Double.pi * 54.13571531 / 180.0)
    let longitudeCos2: Double = cos(Double.pi * -1.281632508 / 180.0)
    let longitudeSin2: Double = sin(Double.pi * -1.281632508 / 180.0)
    DispatchQueue.global(qos: .background).async {
//      while (true) {
        let wrapper: HeBridgeWrapper = HeBridgeWrapper()
////        wrapper.generateKeys()
////        let priv: String = wrapper.getPrivateKey()
////        let pub: String = wrapper.getPublicKey()
////        let rlk: String = wrapper.getRelinKey()
////        Util.writeKeyTo(name: "privs.bin", value: priv)
////        Util.writeKeyTo(name: "pubs.bin", value: pub)
        let ciphers : Array<String> = wrapper.encrypt(latitudeCos, latSin: latitudeSin, longCos: longitudeCos, longSin: longitudeSin, alt: 0, pubKey: Util.getPublicKey())
        let ciphers2 : Array<String> = wrapper.encrypt(latitudeCos2, latSin: latitudeSin2, longCos: longitudeCos2, longSin: longitudeSin2, alt: 5, pubKey: Util.getPublicKey())
//  
        let ret: String = ConnectionService.sendJSONBody(message: JSONBody(latitudeCos1: ciphers[0], latitudeSin1: ciphers[1], longitudeCos1: ciphers[2], longitudeSin1: ciphers[3], latitudeCos2: ciphers2[0], latitudeSin2: ciphers2[1], longitudeCos2: ciphers2[2], longitudeSin2: ciphers2[3], rlk: ""))
        var res: Double = wrapper.decrypt(ret, privateKey: Util.getPrivateKey())
        res = asin(sqrt(res / 2.0)) * 6371 * 2.0 * 1000
        print("GOT \(res)")
//        if (abs(res - 11.02) < 1) {
//          break
//        }
//      }
    }
//    let givenCiphertexts : Array<NewDistanceMessage> = ConnectionService.getDistances(userId: "first", partial: false)
//    var initialResult = Double(truncating: bridge.decryptMulti(givenCiphertexts[0].ciphertext, partialCipher: givenCiphertexts[0].partialDistance, privateKey: Util.getPrivateKey(), isFinal: true) as! NSNumber)
//    initialResult = asin(sqrt(initialResult / 2.0)) * 6371 * 2.0 * 1000
//    print(initialResult)
//    var altitude = Double(truncating: bridge.decryptMulti(givenCiphertexts[0].altitudeDifference, partialCipher: givenCiphertexts[0].partialAltitudeDifference, privateKey: Util.getPrivateKey(), isFinal: true) as! NSNumber)
//    print(altitude)
//    let partialStringDistance: String = bridge.decryptMulti(givenCiphertexts[0].ciphertext, partialCipher: "", privateKey: Util.getPrivateKey(), isFinal: false) as! String
//    let partialStringAltitudeDifference: String = bridge.decryptMulti(givenCiphertexts[0].altitudeDifference, partialCipher: "", privateKey: Util.getPrivateKey(), isFinal: false) as! String
//    ConnectionService.sendNewPartial(partialMessage: NewPartialMessage(partialDistance: partialStringDistance, partialAltitudeDifference: partialStringAltitudeDifference, rowId: givenCiphertexts[0].rowId))
//    ConnectionService.sendNewLocation(message: LocationUploadMessage(latitudeCos: ciphers2[0], latitudeSin: ciphers2[1], longitudeCos: ciphers2[2], longitudeSin: ciphers2[3], altitude: ciphers2[4], id: Util.getUuid(), timestamp:String(format: "%lu", UInt64(NSDate().timeIntervalSince1970))))
    let controller : FlutterViewController = window?.rootViewController as! FlutterViewController
    let heBridgeChannel = FlutterMethodChannel(name: CHANNEL, binaryMessenger: controller.binaryMessenger)
    
    heBridgeChannel.setMethodCallHandler({
      (call: FlutterMethodCall, result: @escaping FlutterResult) -> Void in
      
      let args = call.arguments as? Dictionary<String, String> ?? [:]
      if (call.method == "get-isolation") {
        result(Util.getIsolationEnd())
      } else if (call.method == "get-uid") {
        result(Util.getUuid())
      } else if (call.method == "set-positive") {
        let timestampEnd: UInt64 = UInt64(args["end"]!)!
        Util.setIsolationEnd(isolationEnd: timestampEnd)
        result("success")
      } else {
        result(HeBridgeWrapper().hello(args["name"]))
      }
    })
    return super.application(application, didFinishLaunchingWithOptions: launchOptions)
  }
  
  private func registerForNotifications(application: UIApplication) {
    FirebaseApp.configure()
    Messaging.messaging().delegate = self
    UNUserNotificationCenter.current().delegate = self
    let authOptions: UNAuthorizationOptions = [.alert, .badge, .sound]
    UNUserNotificationCenter.current().requestAuthorization(
      options: authOptions) { _, _ in }
    
    application.registerForRemoteNotifications()
  }
  
  private func registerForLocation() {
    if (Util.getIsolationEnd() > UInt64(NSDate().timeIntervalSince1970)) {
      return
    }
    locationManager.delegate = self
    locationManager.requestAlwaysAuthorization()
    locationManager.desiredAccuracy = kCLLocationAccuracyBestForNavigation
    locationManager.allowsBackgroundLocationUpdates = true
    locationManager.distanceFilter = 5
    locationManager.startUpdatingLocation()
  }
  
  override func application(
    _ application: UIApplication,
    didReceiveRemoteNotification userInfo: [AnyHashable: Any]) {
      NSLog("Received notification: \(userInfo)")
      if (userInfo["he-server-message"] == nil) {
        NSLog("Notification was not from server")
        return
      }
      
      if (userInfo["he-server-message"] as? String == "new data") {
        BackgroundDecryptor.getDistances(userId: Util.getUuid(), privateKey: Util.getPrivateKey(), partial: false)
      } else if (userInfo["he-server-message"] as? String == "new partial data") {
        BackgroundDecryptor.getDistances(userId: Util.getUuid(), privateKey: Util.getPrivateKey(), partial: true)
      } else {
        let isolationEnd: UInt64 = UInt64((userInfo["he-server-message"] as? String)!)!
        sendNewNotification(isolationEnd: isolationEnd)
        Util.setIsolationEnd(isolationEnd: isolationEnd)
        locationManager.stopUpdatingLocation()
      }
    }
  
  private func getCurrentDateString(unixSeconds: UInt64) -> String {
    let dateFormatter = DateFormatter()
    dateFormatter.timeZone = TimeZone.current
    dateFormatter.locale = NSLocale.current
    dateFormatter.dateFormat = "yyyy-MM-dd HH:mm"
    
    return dateFormatter.string(from: Date(timeIntervalSince1970: TimeInterval(unixSeconds)))
  }
  
  private func sendNewNotification(isolationEnd: UInt64) {
    let content = UNMutableNotificationContent()
    content.title = NSString.localizedUserNotificationString(forKey: "You need to isolate!", arguments: nil)
    content.body = NSString.localizedUserNotificationString(forKey: "You have been in contact and need to isolate until \(getCurrentDateString(unixSeconds: isolationEnd))", arguments: nil)
    content.sound = UNNotificationSound.default
    
    let trigger = UNTimeIntervalNotificationTrigger(timeInterval: 2, repeats: false)
    let request = UNNotificationRequest(identifier: "Contact notification", content: content, trigger: trigger)
    let center = UNUserNotificationCenter.current()
    center.add(request) { (error : Error?) in
      if let theError = error {
        NSLog("There has been an error showing the contact notification to the user: \(theError)")
      }
    }
  }
}

extension AppDelegate: CLLocationManagerDelegate {
  
  func locationManagerDidChangeAuthorization(_ manager: CLLocationManager) {
    if #available(iOS 14.0, *) {
      print(manager.authorizationStatus.rawValue)
    }
  }
  
  func locationManager(_ manager: CLLocationManager, didUpdateLocations locations: [CLLocation]) {
    let lastLocation: CLLocation = locations.last!
    let difference: TimeInterval = (self.lastTimestamp != nil) ? lastLocation.timestamp.timeIntervalSince(self.lastTimestamp!) : 0;
    if (difference == 0 || difference >= 5) {
      NSLog("Got new location \(lastLocation.coordinate) \(lastLocation.timestamp)")
      self.lastTimestamp = lastLocation.timestamp;
      let message: LocationUploadMessage = getLocationJson(givenLocation: lastLocation)
      ConnectionService.sendNewLocation(message: message)
    }
  }
  
  private func getLocationJson(givenLocation: CLLocation) -> LocationUploadMessage {
    let latitudeCos: Double = cos(Double.pi * givenLocation.coordinate.latitude / 180.0)
    let latitudeSin: Double = sin(Double.pi * givenLocation.coordinate.latitude / 180.0)
    let longitudeCos: Double = cos(Double.pi * givenLocation.coordinate.longitude / 180.0)
    let longitudeSin: Double = sin(Double.pi * givenLocation.coordinate.longitude / 180.0)
    
    let ciphers : Array<String> = bridge.encrypt(latitudeCos, latSin: latitudeSin, longCos: longitudeCos, longSin: longitudeSin, alt: givenLocation.altitude, pubKey: Util.getPublicKey())
    
    return LocationUploadMessage(latitudeCos: ciphers[0], latitudeSin: ciphers[1], longitudeCos: ciphers[2], longitudeSin: ciphers[3], altitude: ciphers[4], id: Util.getUuid(), timestamp:String(format: "%lu", UInt64(givenLocation.timestamp.timeIntervalSince1970)))
  }
}


extension AppDelegate {
  override func userNotificationCenter(
    _ center: UNUserNotificationCenter,
    willPresent notification: UNNotification,
    withCompletionHandler completionHandler:
    @escaping (UNNotificationPresentationOptions) -> Void
  ) {
    completionHandler([[.alert, .badge, .sound]])
  }
  
  override func userNotificationCenter(
    _ center: UNUserNotificationCenter,
    didReceive response: UNNotificationResponse,
    withCompletionHandler completionHandler: @escaping () -> Void
  ) {
    completionHandler()
  }
}

extension AppDelegate: MessagingDelegate {
  func messaging(
    _ messaging: Messaging,
    didReceiveRegistrationToken fcmToken: String?
  ) {
    if (fcmToken == nil) {
      NSLog("Received nil token")
      return
    }
    
    if (Util.getFcmToken() != fcmToken) {
      NSLog("Received new fcm token \(fcmToken!)")
      Util.setFcmToken(givenToken: fcmToken!)
      ConnectionService.sendNewToken(token: NewTokenMessage(userId: Util.getUuid(), token: fcmToken!))
    }
  }
}
