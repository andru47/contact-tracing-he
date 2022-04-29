import 'dart:convert';
import 'package:google_maps_flutter/google_maps_flutter.dart';
import 'package:http/http.dart' as http;

class ConnectionService {
  static const INFECTION_URL = "http://10.4.1.86:8080/report-positive-case";
  static const INDEX_URL = "http://10.4.1.86:8080/get-infection-index?";

  static Future<void> reportInfection(
      String userId, DateTime givenDateTime) async {
    Uri requestUri = Uri.parse(INFECTION_URL);
    await http.post(requestUri,
        body: JsonEncoder().convert({
          "userId": userId,
          "timestamp": (givenDateTime.toUtc().millisecondsSinceEpoch / 1000)
              .floor()
              .toString()
        }));
  }

  static Future<int> getInfectionIndex(LatLng coords) async {
    Uri requestUri = Uri.parse(INDEX_URL +
        "latitude=${coords.latitude}&longitude=${coords.longitude}");
    var response = await http.get(requestUri);
    if (response.statusCode == 200) {
      return int.parse(response.body);
    } else {
      throw Exception("Couldn't get infection index.");
    }
  }
}
