import 'package:flutter/material.dart';

class DemoItem extends StatefulWidget {
  const DemoItem({super.key});

  @override
  State<DemoItem> createState() => _DemoItemState();
}

class _DemoItemState extends State<DemoItem> {
  ///返回一个居中带图标和文本的Item
  Widget _getBottomItem(IconData icon, String text) {
    return Expanded(
      flex: 1,

      ///居中显示
      child: Center(
        child: Row(
          ///主轴居中,即是横向居中
          mainAxisAlignment: MainAxisAlignment.start,

          ///大小按照最大充满
          mainAxisSize: MainAxisSize.min,

          ///竖向也居中
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            ///一个图标，大小16.0，灰色
            Icon(icon, size: 16.0, color: Colors.grey),

            ///间隔
            Padding(padding: EdgeInsets.only(left: 5.0)),

            ///显示文本
            new Text(
              text,
              //设置字体样式：颜色灰色，字体大小14.0
              style: new TextStyle(color: Colors.grey, fontSize: 14.0),
              //超过的省略为...显示
              overflow: TextOverflow.ellipsis,
              //最长一行
              maxLines: 1,
            ),
          ],
        ),
      ),
    );
  }

  Widget _cardWidget() {
    return Container(
      child: Card(
        color: Colors.limeAccent,
        child: GestureDetector(
          onTap: () {
            print("点击时间");
          },
          child: Padding(
            padding: EdgeInsets.only(
              left: 10.0,
              top: 10.0,
              right: 10.0,
              bottom: 10.0,
            ),
            child: Column(
              mainAxisSize: MainAxisSize.min,
              children: [
                Container(
                  margin: EdgeInsets.only(top: 6.0, bottom: 2.0),
                  alignment: Alignment.topLeft,
                  child: Text(
                    "这是一点描述",
                    style: TextStyle(color: Colors.black38, fontSize: 14.0),
                    maxLines: 3,
                    overflow: TextOverflow.ellipsis,
                  ),
                ),
                Padding(padding: EdgeInsets.only(top: 10, bottom: 10)),
                Row(
                  crossAxisAlignment: CrossAxisAlignment.center,
                  children: [
                    _getBottomItem(Icons.star, "1000"),
                    _getBottomItem(Icons.link, "1000"),
                    _getBottomItem(Icons.subject, "1000"),
                  ],
                ),
              ],
            ),
          ),
        ),
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    return _cardWidget();
  }
}
