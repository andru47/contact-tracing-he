import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
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
  String _greetingMessage = "";
  static const platform = MethodChannel('BRIDGE');

  Future<void> getGreeting(String name, String hostName) async {
    String receivedGreeting = await ConnectionService.getSimpleMessage(
        "$hostName$name");
    setState(() {
      _greetingMessage = receivedGreeting;
    });
  }

  Future<void> getGreetingChannel(String name) async {
    String receivedGreeting = await platform.invokeMethod("hello", <String, String>{"name": name});
    setState(() {
      _greetingMessage = receivedGreeting;
    });
  }

  @override
  Widget build(BuildContext context) {
    final String URL = Theme.of(context).platform == TargetPlatform.android ? "http://10.0.2.2:8080/hello/" : "http://127.0.0.1:8080/hello/";
    final formKey = new GlobalKey<FormState>();
    TextEditingController nameFieldController = TextEditingController();

    var nameField = TextFormField(
        controller: nameFieldController,
        validator: (name) {
          if (name == null || name.isEmpty) {
            return 'Please input at least one character';
          }

          return null;
        },
        autocorrect: false,
        enableSuggestions: false,
        decoration: const InputDecoration(
            hintText: "Enter your name", errorMaxLines: 3));

    validate() {
      formKey.currentState!.save();
      if (!formKey.currentState!.validate()) {
        return;
      }
      getGreetingChannel(nameFieldController.value.text);
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
                longButtons("Get greeting!", validate),
                SizedBox(height: 20),
                _greetingMessage.isEmpty
                    ? SizedBox(height: 0)
                    : Text(_greetingMessage)
              ],
            ),
          )
        ],
      ),
    );
  }
}
