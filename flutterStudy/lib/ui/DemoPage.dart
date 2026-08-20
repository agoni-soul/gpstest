import 'package:flutter/material.dart';
import 'package:flutter_study/ui/tabber_widget.dart';

import 'DemoItem.dart';

class DemoPage extends StatefulWidget {
  const DemoPage({super.key});

  @override
  State<DemoPage> createState() {
    debugPrint('create called');
    return _DemoPageState();
  }
}

class _DemoPageState extends State<DemoPage> {

  @override
  void initState() {
    super.initState();
    debugPrint('initState called');
  }

  @override
  void didChangeDependencies() {
    super.didChangeDependencies();
    debugPrint('didChangeDependencies called');
  }

  @override
  void didUpdateWidget(DemoPage oldWidget) {
    super.didUpdateWidget(oldWidget);
    debugPrint('didUpdateWidget called');
  }

  @override
  void dispose() {
    debugPrint('dispose called');
    super.dispose();
  }

  Widget _widget() {
    ///一个页面的开始
    ///如果是新页面，会自带返回按键
    return TabberWidget(
    );
  }

  @override
  Widget build(BuildContext context) {
    debugPrint('build called');
    return _widget();
  }
}
