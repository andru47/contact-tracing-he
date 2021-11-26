import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:he_contact_tracing/drawer.dart';
import 'package:he_contact_tracing/util/connection_service.dart';
import 'package:he_contact_tracing/util/util.dart';

import 'main.dart';

class ReportPositive extends StatefulWidget {
  @override
  State<StatefulWidget> createState() {
    return ReportPositiveState();
  }
}

class ReportPositiveState extends State<ReportPositive> {
  @override
  Widget build(BuildContext context) {
    final formKey = new GlobalKey<FormState>();
    final responseFieldController = TextEditingController();

    var responseField = TextFormField(
        controller: responseFieldController,
        validator: responseFieldValidator,
        autocorrect: false,
        enableSuggestions: false,
        decoration: const InputDecoration(
            hintText: "Have you got a positive test?", errorMaxLines: 3));

    validate() async {
      formKey.currentState!.save();
      if (formKey.currentState!.validate()) {
        String userId =
            await const MethodChannel("BRIDGE").invokeMethod("get-uid");
        await const MethodChannel("BRIDGE").invokeMethod("set-positive", {
          "end": ((DateTime.now().millisecondsSinceEpoch / 1000).floor() +
                  10 * 24 * 60 * 60)
              .toString()
        });
        ConnectionService.reportInfection(
            "http://10.0.2.2:8080/report-positive-case", userId);
        while (Navigator.canPop(context)) {
          Navigator.pop(context);
        }
        Navigator.of(context).pushReplacement(MaterialPageRoute(
            builder: (BuildContext context) => const MyApp()));
      }
    }

    return Scaffold(
      appBar: AppBar(title: const Text('Report positive test')),
      drawer: MyDrawerStateful(),
      body: ListView(
        padding: const EdgeInsets.all(20),
        children: [
          Form(
            key: formKey,
            child: Column(children: [
              const SizedBox(
                height: 100,
              ),
              ListTile(leading: const Icon(Icons.info), title: responseField),
              const SizedBox(height: 20),
              longButtons("Validate", validate)
            ]),
          )
        ],
      ),
    );
  }
}
