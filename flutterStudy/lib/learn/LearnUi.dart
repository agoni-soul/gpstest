
import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';

class PaddingLearn extends StatefulWidget {
  const PaddingLearn({super.key});

  @override
  State<PaddingLearn> createState() => _PaddingLearnState();
}

class _PaddingLearnState extends State<PaddingLearn> {
  @override
  Widget build(BuildContext context) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Padding(padding: EdgeInsetsGeometry.only(left: 10, top: 10,right: 10)),
        Text("haha", style: TextStyle(color: Colors.cyan, fontSize: 20)),
        Text("hheh", style: TextStyle(color: Colors.cyan, fontSize: 20)),
        Text("nihao", style: TextStyle(color: Colors.cyan, fontSize: 20)),
        Text("huhu", style: TextStyle(color: Colors.cyan, fontSize: 20)),
        Text("tanti", style: TextStyle(color: Colors.cyan, fontSize: 20)),
        Padding(padding: EdgeInsetsGeometry.only(left: 10, bottom: 10,right: 10)),
        Icon(Icons.search)
      ],
    );
  }
}
