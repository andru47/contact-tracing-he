import 'dart:async';

import 'package:flutter/foundation.dart';
import 'package:flutter/gestures.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:google_maps_flutter/google_maps_flutter.dart';
import 'package:he_contact_tracing/drawer.dart';
import 'package:he_contact_tracing/util/connection_service.dart';
import 'package:he_contact_tracing/util/google_places.dart';
import 'package:he_contact_tracing/util/suggested_place.dart';
import 'package:intl/intl.dart';
import 'package:location/location.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatelessWidget {
  const MyApp({Key? key}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'HE-Contact Tracing',
      theme: ThemeData(
        primarySwatch: Colors.blue,
      ),
      home: const MyHomePage(title: 'HE-Contact Tracing'),
    );
  }
}

class MyHomePage extends StatefulWidget {
  const MyHomePage({Key? key, required this.title}) : super(key: key);

  final String title;

  @override
  State<MyHomePage> createState() => _MyHomePageState();
}

class _MyHomePageState extends State<MyHomePage> {
  int timestampEnd = -1;
  int infectionIndex = -1;
  bool timerStarted = false;
  bool sendWithNoise = false;
  List<Marker> markedPosition = [];
  List<Widget> suggestedPlaces = [];
  List<Widget> mapSuggestedPlaces = [];
  TextEditingController searchController = TextEditingController();
  TextEditingController mapTextInputController = TextEditingController();
  LatLng currentMapLocation = const LatLng(51.501364, -0.14189);
  GoogleMapController? mapController;

  static const platform = MethodChannel('BRIDGE');

  void getIsolationStatus() async {
    int returnedTimestamp = await platform.invokeMethod("get-isolation");
    if (returnedTimestamp != 0 && !timerStarted) {
      Duration end = Duration(
          seconds: returnedTimestamp -
              (DateTime.now().millisecondsSinceEpoch / 1000).floor() +
              1);
      Timer(end, () => getIsolationStatus());
      timerStarted = true;
    } else if (returnedTimestamp == 0 && timerStarted) {
      timerStarted = false;
    }
    setState(() {
      timestampEnd = returnedTimestamp;
    });
  }

  @override
  Widget build(BuildContext context) {
    if (timestampEnd == -1) {
      getIsolationStatus();
    }

    var displayIsolation = timestampEnd == 0
        ? (const SelectableText("The app is scanning for possible contacts.",
            style: TextStyle(fontSize: 20, fontStyle: FontStyle.italic),
            textAlign: TextAlign.center))
        : (timestampEnd == -1
            ? (const CircularProgressIndicator())
            : (Column(children: [
                const SelectableText(
                    "You have been in contact with a positive case or have tested positive.\nPlease isolate until",
                    style: TextStyle(fontSize: 20),
                    textAlign: TextAlign.center),
                SelectableText(
                    DateFormat("EEEE, dd/MM/y").format(
                        DateTime.fromMillisecondsSinceEpoch(timestampEnd * 1000)
                            .toLocal()),
                    style: const TextStyle(
                        fontSize: 22,
                        fontWeight: FontWeight.w500,
                        fontStyle: FontStyle.italic),
                    textAlign: TextAlign.center)
              ])));

    return Scaffold(
      drawer: MyDrawerStateful(),
      appBar: AppBar(
        title: Text(widget.title),
      ),
      body: ListView(
        padding: EdgeInsets.all(20),
        children: [
          Column(
            children: [
              const SizedBox(
                height: 100,
              ),
              const Text("Isolation Status",
                  textAlign: TextAlign.center,
                  style: TextStyle(
                      fontWeight: FontWeight.w500,
                      color: Colors.black,
                      fontStyle: FontStyle.italic,
                      fontSize: 25.0)),
              const SizedBox(
                height: 15,
              ),
              Container(
                width: double.infinity,
                decoration: BoxDecoration(
                    borderRadius: const BorderRadius.all(Radius.circular(5)),
                    color: timestampEnd == -1
                        ? Colors.yellow
                        : (timestampEnd == 0 ? Colors.green : Colors.red),
                    boxShadow: [
                      BoxShadow(
                        color: Colors.grey.withOpacity(0.5),
                        spreadRadius: 5,
                        blurRadius: 7,
                        offset: const Offset(0, 3),
                      )
                    ]),
                padding: const EdgeInsets.all(20),
                child: Column(children: [displayIsolation]),
              ),
              const SizedBox(
                height: 50,
              ),
              const Text("Infection Index",
                  textAlign: TextAlign.center,
                  style: TextStyle(
                      fontWeight: FontWeight.w500,
                      color: Colors.black,
                      fontStyle: FontStyle.italic,
                      fontSize: 25.0)),
              const SizedBox(
                height: 15,
              ),
              Stack(children: [
                Padding(
                    padding: EdgeInsets.only(top: 75),
                    child: Row(
                      mainAxisAlignment: MainAxisAlignment.spaceBetween,
                      children: [
                        Row(children: [
                          const Text("Perturb\nLocation",
                              style: TextStyle(fontWeight: FontWeight.bold)),
                          Tooltip(
                              decoration: BoxDecoration(
                                color: Colors.white,
                                boxShadow: [
                                  BoxShadow(
                                    color: Colors.grey.withOpacity(0.5),
                                    spreadRadius: 5,
                                    blurRadius: 7,
                                    offset: const Offset(
                                        0, 3), // changes position of shadow
                                  ),
                                ],
                                borderRadius: const BorderRadius.only(
                                    topLeft: Radius.circular(10),
                                    topRight: Radius.circular(10),
                                    bottomLeft: Radius.circular(10),
                                    bottomRight: Radius.circular(10)),
                              ),
                              textStyle: const TextStyle(
                                  color: Colors.black,
                                  fontStyle: FontStyle.italic),
                              margin: EdgeInsets.symmetric(
                                  horizontal:
                                      MediaQuery.of(context).size.width / 4),
                              message:
                                  "This will add a small noise to the location such that the server can't find your real location",
                              child: const Icon(Icons.info_outlined)),
                          Checkbox(
                            value: sendWithNoise,
                            onChanged: (newValue) {
                              setState(() {
                                if (newValue == null) {
                                  sendWithNoise = false;
                                } else {
                                  sendWithNoise = newValue;
                                }
                              });
                            },
                          )
                        ]),
                        ElevatedButton(
                          onPressed: () async {
                            var toBeSent = currentMapLocation;
                            if (sendWithNoise) {
                              var perturbedLocationDict =
                                  await platform.invokeMethod("perturb", {
                                "lat": currentMapLocation.latitude.toString(),
                                "long": currentMapLocation.longitude.toString()
                              });
                              toBeSent = LatLng(perturbedLocationDict['lat'],
                                  perturbedLocationDict['long']);
                            }

                            showDialog(
                                context: context,
                                builder: (BuildContext context) =>
                                    _buildInfectionIndexPopup(
                                        context,
                                        ConnectionService.getInfectionIndex(
                                            toBeSent)));
                          },
                          child: Row(
                            mainAxisAlignment: MainAxisAlignment.spaceEvenly,
                            children: const [Icon(Icons.send), Text("Submit")],
                          ),
                        )
                      ],
                    )),
                Row(
                    mainAxisAlignment: MainAxisAlignment.spaceBetween,
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Flexible(
                          child: Column(children: [
                        TextField(
                          controller: searchController,
                          onChanged: (value) async {
                            List<SuggestedPlace> returnedList =
                                await GooglePlaces.getPlaces(value);
                            setState(() {
                              suggestedPlaces = [];
                              for (SuggestedPlace place in returnedList) {
                                suggestedPlaces.add(Container(
                                    decoration:
                                        BoxDecoration(color: Colors.white),
                                    child: ListTile(
                                      title: Text(place.name),
                                      style: ListTileStyle.drawer,
                                      contentPadding: EdgeInsets.zero,
                                      onTap: () async {
                                        LatLng newSelectedPointOnMap =
                                            await GooglePlaces
                                                .getLocationCoordsFromPlaceId(
                                                    place.placeId);
                                        setState(() {
                                          suggestedPlaces = [];
                                          searchController.text = place.name;
                                          currentMapLocation =
                                              newSelectedPointOnMap;
                                          markedPosition = [
                                            Marker(
                                                markerId: MarkerId(place.name),
                                                position: currentMapLocation)
                                          ];
                                        });
                                      },
                                    )));
                              }
                            });
                          },
                          autocorrect: false,
                          decoration: InputDecoration(
                              hintText: "Introduce address",
                              suffixIcon: IconButton(
                                icon: const Icon(Icons.near_me_outlined),
                                onPressed: () async {
                                  var location = await Location().getLocation();
                                  var returnedLocationName = await GooglePlaces
                                      .getLocationNameFromCoordinates(LatLng(
                                          location.latitude!,
                                          location.longitude!));
                                  setState(() {
                                    currentMapLocation = LatLng(
                                        location.latitude!,
                                        location.longitude!);
                                    searchController.text =
                                        returnedLocationName;
                                    markedPosition = [
                                      Marker(
                                          markerId:
                                              MarkerId(returnedLocationName),
                                          position: currentMapLocation)
                                    ];
                                  });
                                },
                              ),
                              icon: const Icon(Icons.location_on),
                              contentPadding:
                                  const EdgeInsets.symmetric(vertical: 15)),
                        ),
                        SizedBox(
                          height: suggestedPlaces.isNotEmpty ? 100 : 0,
                          child: ListView(
                            children: suggestedPlaces,
                            padding: const EdgeInsets.only(left: 40),
                          ),
                        )
                      ])),
                      IconButton(
                        icon: const Icon(Icons.map),
                        padding: EdgeInsets.zero,
                        onPressed: () {
                          showDialog(
                            context: context,
                            builder: (BuildContext context) =>
                                _buildMapPopup(context),
                          );
                        },
                      ),
                    ]),
              ])
            ],
          ),
        ],
      ),
    );
  }

  Widget _buildInfectionIndexPopup(
      BuildContext context, Future<int> givenFuture) {
    return StatefulBuilder(builder: (context, setState) {
      return AlertDialog(
        title: const Text('Infection Index', textAlign: TextAlign.center),
        content: FutureBuilder(
            future: givenFuture,
            builder: (context, snapshot) {
              if (snapshot.connectionState == ConnectionState.done &&
                  !snapshot.hasError) {
                return Column(
                    crossAxisAlignment: CrossAxisAlignment.center,
                    mainAxisSize: MainAxisSize.min,
                    children: [
                      Text(
                          "The infection index for ${searchController.value.text} is:",
                          textAlign: TextAlign.center,
                          style: const TextStyle(fontStyle: FontStyle.italic)),
                      const SizedBox(
                        height: 10,
                      ),
                      Text(
                        snapshot.data!.toString(),
                        textAlign: TextAlign.center,
                        style: const TextStyle(
                            fontWeight: FontWeight.bold, fontSize: 36),
                      ),
                    ]);
              } else {
                return Column(
                    mainAxisSize: MainAxisSize.min,
                    crossAxisAlignment: CrossAxisAlignment.center,
                    children: [
                      Transform.scale(
                        scale: 1,
                        child: CircularProgressIndicator(),
                      ),
                      SizedBox(height: 10),
                      const Text(
                        "Please wait, the information is getting fetched.",
                        style: TextStyle(fontWeight: FontWeight.bold),
                        textAlign: TextAlign.center,
                      )
                    ]);
              }
            }),
        actions: <Widget>[
          TextButton(
            onPressed: () {
              Navigator.of(context).pop();
            },
            child: const Text('Back'),
          ),
        ],
      );
    });
  }

  Widget _buildMapPopup(BuildContext context) {
    return StatefulBuilder(builder: (context, setState) {
      return AlertDialog(
        title: const Text('Location Selector', textAlign: TextAlign.center),
        content: SingleChildScrollView(
            padding: const EdgeInsets.symmetric(vertical: 10),
            child: Column(
              mainAxisSize: MainAxisSize.min,
              crossAxisAlignment: CrossAxisAlignment.start,
              children: <Widget>[
                Row(
                  mainAxisAlignment: MainAxisAlignment.spaceBetween,
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Flexible(
                        child: TextField(
                      controller: mapTextInputController,
                      onChanged: (value) async {
                        List<SuggestedPlace> returnedList =
                            await GooglePlaces.getPlaces(value);
                        setState(() {
                          mapSuggestedPlaces = [];
                          for (SuggestedPlace place in returnedList) {
                            mapSuggestedPlaces.add(Container(
                                decoration: BoxDecoration(color: Colors.white),
                                child: ListTile(
                                  title: Text(place.name),
                                  style: ListTileStyle.drawer,
                                  contentPadding: EdgeInsets.zero,
                                  onTap: () async {
                                    LatLng newSelectedPointOnMap =
                                        await GooglePlaces
                                            .getLocationCoordsFromPlaceId(
                                                place.placeId);
                                    setState(() {
                                      currentMapLocation =
                                          newSelectedPointOnMap;
                                      mapController?.animateCamera(
                                          CameraUpdate.newCameraPosition(
                                              CameraPosition(
                                                  target: currentMapLocation,
                                                  zoom: 14)));
                                      mapTextInputController.text = place.name;
                                      searchController.text = place.name;
                                      mapSuggestedPlaces = [];
                                      markedPosition = [
                                        Marker(
                                            markerId: MarkerId(place.name),
                                            position: currentMapLocation)
                                      ];
                                    });
                                  },
                                )));
                          }
                        });
                      },
                      decoration: const InputDecoration(
                          hintText: "Tap to introduce an address",
                          contentPadding: EdgeInsets.symmetric(vertical: 15)),
                    )),
                  ],
                ),
                Stack(children: getMapAndChildren(setState))
              ],
            )),
        actions: <Widget>[
          TextButton(
            onPressed: () {
              Navigator.of(context).pop();
            },
            child: const Text('Back'),
          ),
        ],
      );
    });
  }

  List<Widget> getMapAndChildren(setState) {
    List<Widget> currentList = [
      Container(
        padding: EdgeInsets.only(top: 30),
        height: MediaQuery.of(context).size.height / 2,
        width: MediaQuery.of(context).size.width,
        child: GoogleMap(
          markers: Set.from(markedPosition),
          onTap: (tappedLocation) async {
            String locationName =
                await GooglePlaces.getLocationNameFromCoordinates(
                    tappedLocation);
            setState(() {
              currentMapLocation = tappedLocation;
              mapTextInputController.text = locationName;
              searchController.text = locationName;
              markedPosition = [
                Marker(
                    markerId: MarkerId(tappedLocation.toString()),
                    position: tappedLocation)
              ];
            });
          },
          gestureRecognizers: Set()
            ..add(Factory<EagerGestureRecognizer>(
                () => EagerGestureRecognizer())),
          zoomGesturesEnabled: true,
          initialCameraPosition: CameraPosition(
            target: currentMapLocation,
            zoom: 14.0,
          ),
          myLocationEnabled: true,
          myLocationButtonEnabled: true,
          mapType: MapType.normal,
          onMapCreated: (controller) {
            setState(() {
              mapController = controller;
              mapController?.animateCamera(CameraUpdate.newCameraPosition(
                  CameraPosition(target: currentMapLocation, zoom: 14)));
            });
          },
        ),
      )
    ];
    if (mapSuggestedPlaces.isNotEmpty) {
      currentList.add(Container(
          height: MediaQuery.of(context).size.height / 4,
          width: MediaQuery.of(context).size.width,
          child: ListView(children: mapSuggestedPlaces, shrinkWrap: true)));
    }
    return currentList;
  }
}
