import 'package:flutter/material.dart';
import 'package:fluttertest/ui/DemoPage.dart';

void main() {
  runApp(DemoApp());
}

class DemoApp extends StatelessWidget {
  const DemoApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(home: DemoPage(),);
  }
}