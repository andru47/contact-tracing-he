import 'dart:io';
import 'package:http/http.dart' as http;

class ConnectionService {
  static Future<String> getSimpleMessage(String url) async {
    Uri requestUri = Uri.parse(url);
    try {
      var resp = await http.get(requestUri,
          headers: {HttpHeaders.acceptHeader: "application/json"});
      return resp.body;
    } catch (e) {
      return Future.value("");
    }
  }
}
