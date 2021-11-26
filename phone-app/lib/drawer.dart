import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:he_contact_tracing/report_positive.dart';

import 'main.dart';

class MyDrawerStateful extends StatefulWidget {
  @override
  State<StatefulWidget> createState() {
    return MyDrawer();
  }
}

class MyDrawer extends State<MyDrawerStateful> {
  int isIsolating = -1;

  void getIsolationStatus() async {
    int timestampEnd =
        await const MethodChannel("BRIDGE").invokeMethod("get-isolation");
    setState(() {
      isIsolating = timestampEnd;
    });
  }

  @override
  Widget build(BuildContext context) {
    if (isIsolating == -1) {
      getIsolationStatus();
    }

    var reportTest = isIsolating <= 0
        ? ListTile(
            leading: const Icon(Icons.local_hospital),
            title: const Text('Report Positive Test'),
            onTap: () {
              while (Navigator.canPop(context)) {
                Navigator.pop(context);
              }
              Navigator.of(context).push(MaterialPageRoute(
                  builder: (BuildContext context) => ReportPositive()));
            },
          )
        : const SizedBox();

    return Drawer(
      child: ListView(
        padding: EdgeInsets.zero,
        children: <Widget>[
          const DrawerHeader(
            decoration: BoxDecoration(
              color: Colors.blue,
            ),
            child: Text(
              'Main Menu',
              style: TextStyle(
                color: Colors.white,
                fontSize: 24,
              ),
            ),
          ),
          ListTile(
            leading: const Icon(Icons.home),
            title: const Text('Home'),
            onTap: () {
              while (Navigator.canPop(context)) {
                Navigator.pop(context);
              }
              Navigator.of(context).push(MaterialPageRoute(
                  builder: (BuildContext context) => const MyApp()));
            },
          ),
          reportTest
        ],
      ),
    );
  }
}
