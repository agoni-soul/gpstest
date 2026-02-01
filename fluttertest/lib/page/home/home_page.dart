import 'dart:io';

import 'package:android_intent_plus/android_intent.dart';
import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';
import 'package:fluttertest/page/home/widget/home_drawer.dart';
import 'package:fluttertest/widget/haha_tabber_widget.dart';
import 'package:fluttertest/widget/style/haha_style.dart';
import 'package:lottie/lottie.dart';

import '../../common/utils/navigator_utils.dart';
import '../../widget/haha_title_bar.dart';
import '../dynamic/dynamic_page.dart';
import '../my_page.dart';
import '../trend/trend_page.dart';

class HomePage extends StatefulWidget {
  static const String sName = "home";

  const HomePage({super.key});

  @override
  _HomePageState createState() => _HomePageState();
}

class _HomePageState extends State<HomePage> {
  final GlobalKey<DynamicPageState> dynamicKey = GlobalKey();
  final GlobalKey<TrendPageState> trendKey = GlobalKey();
  final GlobalKey<MyPageState> myKey = GlobalKey();
  final GlobalKey rightKey = GlobalKey();

  /// 不退出
  _dialogExitApp(BuildContext context) async {
    ///如果是 android 回到桌面
    if (Platform.isAndroid) {
      AndroidIntent intent = const AndroidIntent(
        action: 'android.intent.action.MAIN',
        category: "android.intent.category.HOME",
      );
      await intent.launch();
    }
  }

  _renderTab(icon, text) {
    return Tab(
      child: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        children: <Widget>[Icon(icon, size: 16.0), Text(text)],
      ),
    );
  }

  // This widget is the root of your application.
  @override
  Widget build(BuildContext context) {
    List<Widget> tabs = [
      _renderTab(
          HahaIcons.MAIN_DT, "home_dynamic"),
      _renderTab(HahaIcons.MAIN_QS, "home_trend"),
      _renderTab(HahaIcons.MAIN_MY, "home_my"),
    ];

    ///增加返回按键监听
    return PopScope(
      canPop: false,
      onPopInvokedWithResult: (didPop, _) {
        _dialogExitApp(context);
      },
      child: TabBarWidget(
        drawer: const HomeDrawer(),
        type: TabType.bottom,
        tabItems: tabs,
        tabViews: [
          DynamicPage(key: dynamicKey),
          TrendPage(key: trendKey),
          MyPage(key: myKey),
        ],
        onDoublePress: (index) {
          switch (index) {
            case 0:
              dynamicKey.currentState?.scrollToTop();
              break;
            case 1:
              trendKey.currentState?.scrollToTop();
              break;
            case 2:
              myKey.currentState?.scrollToTop();
              break;
          }
        },
        backgroundColor: HahaColors.primarySwatch,
        indicatorColor: HahaColors.white,
        title: HahaTitleBar(
          "app_name",
          rightWidget: InkWell(
            onTap: () {
              RenderBox renderBox2 =
              rightKey.currentContext?.findRenderObject() as RenderBox;
              var position = renderBox2.localToGlobal(Offset.zero);
              var size = renderBox2.size;
              var centerPosition = Offset(
                position.dx + size.width / 2,
                position.dy + size.height / 2,
              );
              NavigatorUtils.goSearchPage(context, centerPosition);
            },
            child: Container(
              key: rightKey,
              alignment: Alignment.centerRight,
              child: Lottie.asset('static/file/search.json',
                  width: 70,
                  height: 80,
                  fit: BoxFit.cover,
                  alignment: Alignment.centerRight),
            ),
          ),
        ),
      ),
    );
  }
}
