import 'dart:convert';

import 'package:http/http.dart' as http;

class ConnectionService {
  static Future<void> reportInfection(String url, String userId) async {
    Uri requestUri = Uri.parse(url);
    await http.post(requestUri,
        body: JsonEncoder().convert({
          "userId": userId,
          "timestamp": (DateTime.now().toUtc().millisecondsSinceEpoch / 1000)
              .floor()
              .toString()
        }));
  }
}
