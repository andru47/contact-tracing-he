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

  static Future<String> getDistance(String url, List<String> cipher, List<String> keys) async {
    Uri requestUri = Uri.parse(url);
    try {
      var resp = await http.post(requestUri, body: JsonEncoder().convert({"latitudeCos": cipher.elementAt(0),
                                                        "latitudeSin": cipher.elementAt(1),
                                                        "longitudeCos": cipher.elementAt(2),
                                                        "longitudeSin": cipher.elementAt(3),
                                                         "relin": keys.elementAt(0),
                                                          "privateKey": keys.elementAt(1)
      }));
      return resp.body;
    } catch (e) {
    return Future.value("");
    }
  }
}
