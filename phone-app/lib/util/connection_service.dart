
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
}
