import 'dart:convert';
import 'package:google_maps_flutter/google_maps_flutter.dart';
import 'package:he_contact_tracing/util/suggested_place.dart';
import 'package:http/http.dart' as http;

class GooglePlaces {
  static const GOOGLE_API_KEY = "";
  static const AUTOCOMPLETE_URL =
      "https://maps.googleapis.com/maps/api/place/autocomplete/json?key=${GOOGLE_API_KEY}&input=";
  static const REVERSE_LOC_URL =
      "https://maps.googleapis.com/maps/api/geocode/json?key=${GOOGLE_API_KEY}&result_type=street_address&latlng=";
  static const LOC_COORDS_URL =
      "https://maps.googleapis.com/maps/api/geocode/json?key=${GOOGLE_API_KEY}&place_id=";
  static const PLACE_NAME_URL = "maps.googleapis.com";

  static Future<List<SuggestedPlace>> getPlaces(String input) async {
    var response = await http.get(Uri.parse(AUTOCOMPLETE_URL + input));
    if (response.statusCode == 200) {
      List<dynamic> returnedPlaces = json.decode(response.body)['predictions'];
      return List.from(returnedPlaces.map((currentElement) => SuggestedPlace(
          currentElement['description'], currentElement['place_id'])));
    } else {
      throw Exception('Failed to load predictions');
    }
  }

  static Future<String> getLocationNameFromCoordinates(LatLng coords) async {
    print(coords.toString());
    var response = await http.get(Uri.parse(REVERSE_LOC_URL +
        coords.latitude.toString() +
        ',' +
        coords.longitude.toString()));
    if (response.statusCode == 200) {
      var parsedJson = json.decode(response.body)['results'];
      if (parsedJson.length == 0) {
        return "UNNAMED LOCATION";
      }
      return parsedJson[0]['formatted_address'];
    } else {
      throw Exception("Failed to reverse look-up");
    }
  }

  static Future<LatLng> getLocationCoordsFromPlaceId(String placeId) async {
    var response = await http.get(Uri.parse(LOC_COORDS_URL + placeId));
    if (response.statusCode == 200) {
      var locationJson =
          json.decode(response.body)['results'][0]['geometry']['location'];
      return LatLng(locationJson['lat'], locationJson['lng']);
    } else {
      throw Exception('Failed to get locations coordinates');
    }
  }
}
