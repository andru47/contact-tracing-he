# HETracing
A contact-tracing phone application and server that protects the privacy by using Homomorphic Encryption (HE) and Differential Privacy. HETracing was created as part of final year dissertation in order to discuss about the costs of using HE for contact tracing.

The app works for both iOS and Android and the front-end is written in Dart. The back-end server is written in Java. Three HE libraries can be linked to the phone application and server:
 - [SEAL](https://github.com/microsoft/SEAL)
 - [Lattigo](https://github.com/tuneinsight/lattigo)
 - [SMKHE](https://github.com/andru47/smkhe)

# Building
There are multiple steps to make the phone application and server build. The HE libraries need to be built for the phone platform that you want to use and also the server's platform.

## Server
To build for the server, you need to build the three HE libraries according to their specification. After this, you need to:
 - Change lines `9-10` from this [CMakeLists.txt](he-component/bridge/server/CMakeLists.txt) file to specify the path of the `smkhe` library
 - Change line `12` from this [CMakeLists.txt](he-component/src/server/lattigo/go/CMakeLists.txt) file to specify the correct `go path`
 - Run `cmake .` in `he-component/bridge/server`
 - Run `make` in `he-component/bridge/server`

After these steps, the binary will be created in `he-component/bridge/server` and can be linked correctly from Java.

### Database
The server is also connected to a MariaDB database. To set the database, use the [tables.sql](backend-server/tables.sql) file to create the tables needed by HETracing and then modify line `19` of the [Controller](backend-server/src/main/java/dissertation/backend/database/Controller.java) file to
point to the correct connection URL for the database. 

## Phone application
As in the case of the server, the first step is to build the three libraries for the phone's platform. After this, you need to link the binaries with CMake for Android or inside Xcode for iOS.

### Android
After building the binaries, you need to:
 - Modify lines `24-26` from [Makefile](he-component/src/client/lattigo/go/Makefile) to point to the right paths
 - Run `make android` inside `he-component/src/client/lattigo/go`
 - Change lines `5, 6, 8, 9` from this [CMakeLists.txt](he-component/bridge/android/CMakeLists.txt) file to point to the binaries created for SEAL/SMKHE
 - Run the application

### iOS
You will probably need a developer account for notifications to work.

 - Run `make ios` inside `he-component/src/client/lattigo/go`, creating the go library inside `he-component/src/client/lattigo/go/build`
 - Inside Xcode, add the header and library search paths under `Search Paths` from `Build Settings`
 - Inside Xcode, under `Build Phases`, go to `Link Binary With Libraries` and all the three libraries' binaries
 - Run the application

## Google APIs
HETracing using Firebase Cloud Messaging to send notifications from the server and the phone application uses the Google Maps API and Places API. To make the application work as expected, you will need:
 - A Google API key that can be used for Maps and Places. You need to copy this in: line `18` of [Android Manifest](phone-app/android/app/src/main/AndroidManifest.xml), line `7` of [google_places.dart](phone-app/lib/util/google_places.dart)
and line `36` of [AppDelegate.swift](phone-app/ios/Runner/AppDelegate.swift)
 - For server, you need the FCM `json` file's path to be kept in environment variable `GOOGLE_APPLICATION_CREDENTIALS`
 - For iOS, you need the FCM `GoogleService-Info.plist` placed in the root of the iOS project
 - For Android, add the FCM `google-services.json` to the `app` folder

# Screenshots
<p align="center">
<img width="959" alt="front-end-new" src="https://user-images.githubusercontent.com/47646359/165987510-cb7a4c8f-8cab-4612-8329-fa5253d3f276.png">
<img width="961" alt="infection-index" src="https://user-images.githubusercontent.com/47646359/165987562-5516f4f9-0f57-49a9-b238-5a7d880342e5.png">
</p>
