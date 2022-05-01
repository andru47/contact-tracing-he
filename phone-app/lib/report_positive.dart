import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:he_contact_tracing/drawer.dart';
import 'package:he_contact_tracing/util/connection_service.dart';
import 'package:he_contact_tracing/util/util.dart';
import 'package:intl/intl.dart';

import 'main.dart';

class ReportPositive extends StatefulWidget {
  @override
  State<StatefulWidget> createState() {
    return ReportPositiveState();
  }
}

class ReportPositiveState extends State<ReportPositive> {
  TextEditingController dateInputController = TextEditingController();
  DateTime pickedDateTime = DateTime.now();

  @override
  Widget build(BuildContext context) {
    final formKey = GlobalKey<FormState>();

    validate() async {
      formKey.currentState!.save();
      if (dateInputController.value.text.isNotEmpty) {
        String userId =
            await const MethodChannel("BRIDGE").invokeMethod("get-uid");
        await const MethodChannel("BRIDGE").invokeMethod("set-positive", {
          "end": ((pickedDateTime.millisecondsSinceEpoch / 1000).floor() +
                  10 * 24 * 60 * 60)
              .toString()
        });
        ConnectionService.reportInfection(userId, pickedDateTime);
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
              TextField(
                controller: dateInputController,
                decoration: const InputDecoration(
                    icon: Icon(Icons.calendar_today),
                    floatingLabelBehavior: FloatingLabelBehavior.always,
                    label: Text("Date of positive test / first symptoms",
                        maxLines: 2),
                    hintMaxLines: 3,
                    hintText: "Tap to open the date picker"),
                autofocus: true,
                readOnly: true,
                onTap: () async {
                  DateTime? pickedDate = await showDatePicker(
                      context: context,
                      initialDate: DateTime.now(),
                      firstDate:
                          DateTime.now().subtract(const Duration(days: 10)),
                      lastDate: DateTime.now());

                  if (pickedDate != null) {
                    setState(() {
                      dateInputController.text =
                          DateFormat("EEE, d/M/y").format(pickedDate);
                      pickedDateTime = pickedDate;
                    });
                  }
                },
              ),
              const SizedBox(height: 20),
              longButtons("Validate", validate)
            ]),
          )
        ],
      ),
    );
  }
}
