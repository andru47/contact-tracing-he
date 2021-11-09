import 'package:shared_preferences/shared_preferences.dart';
import 'package:uuid/uuid.dart';

String getId(SharedPreferences prefs) {
  if (prefs.containsKey("id")) {
    return prefs.getString("id")!;
  }

  var uuid = Uuid();
  String newId = uuid.v4();
  prefs.setString("id", newId);

  return newId;
}
