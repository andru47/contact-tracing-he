import 'dart:math';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:he_contact_tracing/util/util.dart';
import 'package:shared_preferences/shared_preferences.dart';

import './util/connection_service.dart';

void main() {
  runApp(const MyApp());
}

MaterialButton longButtons(String title, Function fun,
    {Color color: const Color(0xfff063057), Color textColor: Colors.white}) {
  return MaterialButton(
    onPressed: () {
      fun();
    },
    textColor: textColor,
    color: color,
    child: SizedBox(
      width: double.infinity,
      child: Text(
        title,
        textAlign: TextAlign.center,
      ),
    ),
    height: 45,
    minWidth: 600,
    shape: RoundedRectangleBorder(
        borderRadius: BorderRadius.all(Radius.circular(10))),
  );
}

class MyApp extends StatelessWidget {
  const MyApp({Key? key}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Flutter Demo',
      theme: ThemeData(
        primarySwatch: Colors.blue,
      ),
      home: const MyHomePage(title: 'Flutter Demo Home Page'),
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
  String _computedResult = "";
  static const platform = MethodChannel('BRIDGE');

  Future<void> computeResult(
      String hostName, double latitude1, double longitude1, double latitude2, double longitude2) async {
    String publicKey = await rootBundle.loadString("assets/pubKey.bin");
    String privateKey = await rootBundle.loadString("assets/privateKey.bin");
    //final prefs = await SharedPreferences.getInstance();
    List<String> cipherTexts = List<String>.from(await platform.invokeMethod(
        "encrypt", {
      "latitude1": latitude1,
      "longitude1": longitude1,
      "latitude2": latitude2,
      "longitude2": longitude2,
      "publicKey": publicKey
    }));
    String cipherTextComputed =
        await ConnectionService.getDistance(hostName, cipherTexts);
    //await ConnectionService.uploadNewLocation("http://10.0.2.2:8080/upload-location", getId(prefs), "2021-11-09 12:45", cipherTexts);

    double distance = await platform.invokeMethod(
        "decrypt", {"cipher": cipherTextComputed, "privateKey": privateKey});
    print(distance);
    distance = asin(sqrt(distance / 2.0)) * 6378.8 * 2.0;
    setState(() {
      _computedResult = distance.toString();
    });
    /*String cipherText = await platform.invokeMethod("encrypt", <String, String>{"plain": number});
    String cipherTextComputed = await ConnectionService.getSimpleResult(hostName, cipherText);
    String newNumber = await platform.invokeMethod("decrypt", <String, String>{"cipher": cipherTextComputed});
    setState(() {
      _computedResult = newNumber;
    });*/
  }

  @override
  Widget build(BuildContext context) {
    final String URL = Theme.of(context).platform == TargetPlatform.android
        ? "http://10.0.2.2:8080/distance-calculator"
        : "http://127.0.0.1:8080/compute-simple";
    final formKey = new GlobalKey<FormState>();
    TextEditingController nameFieldController = TextEditingController();

    var nameField = TextFormField(
        controller: nameFieldController,
        validator: (name) {
          if (name == null || name.isEmpty) {
            return 'Please input at least one digit';
          }

          return null;
        },
        autocorrect: false,
        enableSuggestions: false,
        decoration: const InputDecoration(
            hintText: "Enter a number", errorMaxLines: 3));

    validate() {
      formKey.currentState!.save();
      if (!formKey.currentState!.validate()) {
        return;
      }
      computeResult(URL, 52.20467756586156, 0.10558759665058481, 52.204761, 0.105507);
    }

    return Scaffold(
      appBar: AppBar(
        title: Text(widget.title),
      ),
      body: ListView(
        padding: EdgeInsets.all(20),
        children: [
          Form(
            key: formKey,
            child: Column(
              children: [
                SizedBox(
                  height: 100,
                ),
                ListTile(leading: Icon(Icons.info), title: nameField),
                SizedBox(
                  height: 20,
                ),
                longButtons("Compute (x + 1)^2!", validate),
                SizedBox(height: 20),
                _computedResult.isEmpty
                    ? SizedBox(height: 0)
                    : Text(_computedResult)
              ],
            ),
          )
        ],
      ),
    );
  }
}
