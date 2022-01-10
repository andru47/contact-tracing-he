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
    registerForNotifications(application: application)
    registerForLocation()
    
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
    locationManager.desiredAccuracy = kCLLocationAccuracyBest
    locationManager.allowsBackgroundLocationUpdates = true
    locationManager.distanceFilter = 5000000
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
