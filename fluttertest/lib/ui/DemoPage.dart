import 'package:flutter/material.dart';

import 'DemoItem.dart';

class DemoPage extends StatefulWidget {
  const DemoPage({super.key});

  @override
  State<DemoPage> createState() => _DemoPageState();
}

class _DemoPageState extends State<DemoPage> {
  Widget _widget() {
    ///一个页面的开始
    ///如果是新页面，会自带返回按键
    return Scaffold(
      ///背景样式
      backgroundColor: Colors.blue,

      ///标题栏，当然不仅仅是标题栏
      appBar: AppBar(title: Text("Title")),

      ///正式的页面开始
      ///一个ListView，20个Item
      body: ListView.builder(
        itemBuilder: (context, index) {
          return DemoItem();
        },
        itemCount: 20,
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    return _widget();
  }
}
