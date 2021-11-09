import 'dart:convert';

import 'package:http/http.dart' as http;

class ConnectionService {
  static Future<String> getSimpleResult(String url, String cipher) async {
    Uri requestUri = Uri.parse(url);
    try {
      var resp = await http.post(requestUri, body: cipher);
      return resp.body;
    } catch (e) {
      return Future.value("");
    }
  }

  static Future<String> getDistance(String url, List<String> cipher) async {
    Uri requestUri = Uri.parse(url);
    try {
      var resp = await http.post(requestUri, body: JsonEncoder().convert({"latitudeCos1": cipher.elementAt(0),
                                                        "latitudeSin1": cipher.elementAt(1),
                                                        "longitudeCos1": cipher.elementAt(2),
                                                        "longitudeSin1": cipher.elementAt(3),
                                                        "latitudeCos2": cipher.elementAt(4),
                                                        "latitudeSin2": cipher.elementAt(5),
                                                        "longitudeCos2": cipher.elementAt(6),
                                                        "longitudeSin2": cipher.elementAt(7)
      }));
      return resp.body;
    } catch (e) {
    return Future.value("");
    }
  }

  static Future<void> uploadNewLocation(String url, String id, String timestamp, List<String> cipher) async {
    Uri requestUri = Uri.parse(url);
    await http.post(requestUri, body: JsonEncoder().convert({"id": id,
                            "timestamp": timestamp,
                            "latitudeCos": cipher.elementAt(0),
                            "latitudeSin": cipher.elementAt(1),
                            "longitudeCos": cipher.elementAt(2),
                            "longitudeSin": cipher.elementAt(3)}));
  }
}
