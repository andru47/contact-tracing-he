import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:he_contact_tracing/drawer.dart';

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
  static const platform = MethodChannel('BRIDGE');

  void getIsolationStatus() async {
    int returnedTimestamp = await platform.invokeMethod("get-isolation");
    setState(() {
      timestampEnd = returnedTimestamp;
    });
  }

  @override
  Widget build(BuildContext context) {
    if (timestampEnd == -1) {
      getIsolationStatus();
    }
    final formKey = new GlobalKey<FormState>();

    var displayIsolation = timestampEnd == 0
        ? (const SelectableText("The app is scanning possible contacts.",
            style: TextStyle(fontWeight: FontWeight.bold, fontSize: 20)))
        : (timestampEnd == -1
            ? (const CircularProgressIndicator())
            : (SelectableText(
                "You have been in contact with a positive case.\nPlease isolate until " +
                    DateTime.fromMillisecondsSinceEpoch(timestampEnd * 1000)
                        .toLocal()
                        .toString(),
                style: const TextStyle(
                    fontWeight: FontWeight.bold, fontSize: 20))));

    return Scaffold(
      drawer: MyDrawerStateful(),
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
                const SizedBox(
                  height: 100,
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
                  child: Column(children: [
                    const SelectableText(
                      "Isolation status",
                      textAlign: TextAlign.center,
                      style: TextStyle(fontSize: 16),
                    ),
                    displayIsolation
                  ]),
                )
              ],
            ),
          )
        ],
      ),
    );
  }
}
