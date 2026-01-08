import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(home: MyHomePage());
  }
}

class MyHomePage extends StatefulWidget {
  @override
  _MyHomePageState createState() => _MyHomePageState();
}

class _MyHomePageState extends State<MyHomePage> {
  static const platform = MethodChannel('com.example.flutter_module/channel');

  // 提供给 Android 调用的方法
  Future<String> getFlutterData(String input) async {
    return "Flutter 处理后的数据: $input";
  }

  @override
  void initState() {
    super.initState();

    // 设置方法调用处理器
    platform.setMethodCallHandler((call) async {
      switch (call.method) {
        case 'getFlutterData':
          final String input = call.arguments as String;
          return await getFlutterData(input);
        case 'calculateSum':
          final List<int> numbers = List<int>.from(call.arguments as List);
          return numbers.reduce((a, b) => a + b);
        default:
          throw PlatformException(
            code: '未实现的方法',
            message: '方法 ${call.method} 未实现',
            details: null,
          );
      }
    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: Text('Flutter Module')),
      body: Center(child: Text('等待 Kotlin 调用...')),
    );
  }
}
