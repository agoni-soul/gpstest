import 'package:flutter/material.dart';
import 'package:flutter_spinkit/flutter_spinkit.dart';
import 'package:fluttertest/common/localization/extension.dart';
import 'package:fluttertest/common/logger.dart';
import 'package:fluttertest/widget/haha_common_option_widget.dart';
import 'package:fluttertest/widget/style/haha_style.dart';
import 'package:webview_flutter/webview_flutter.dart';

/// webview版本
/// Created by guoshuyu
/// on 2018/7/27.

class HahaWebView extends StatefulWidget {
  final String url;
  final String? title;

  const HahaWebView(this.url, this.title, {super.key});

  @override
  _HahaWebViewState createState() => _HahaWebViewState();
}

class _HahaWebViewState extends State<HahaWebView> {
  _renderTitle() {
    if (widget.url.isEmpty) {
      return Text(widget.title!);
    }
    return Row(children: [
      Expanded(
          child: Text(
        widget.title!,
        maxLines: 1,
        overflow: TextOverflow.ellipsis,
      )),
      HahaCommonOptionWidget(url: widget.url),
    ]);
  }

  final FocusNode focusNode = FocusNode();

  bool isLoading = true;

  late final WebViewController controller;

  @override
  void initState() {
    controller = WebViewController()
      ..setJavaScriptMode(JavaScriptMode.unrestricted)
      ..setNavigationDelegate(
        NavigationDelegate(
          onProgress: (int progress) {
            // Update loading bar.
          },
          onPageStarted: (String url) {},
          onPageFinished: (String url) {
            setState(() {
              isLoading = false;
            });
          },
          onWebResourceError: (WebResourceError error) {},
        ),
      )
      ..addJavaScriptChannel("name", onMessageReceived: (message) {
        printLog(message.message);
        FocusScope.of(context).requestFocus(focusNode);
      })
      ..loadRequest(Uri.parse(widget.url));

    super.initState();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: HahaColors.mainBackgroundColor,
      appBar: AppBar(
        title: _renderTitle(),
      ),
      body: Stack(
        children: <Widget>[
          TextField(
            focusNode: focusNode,
          ),
          WebViewWidget(
            controller: controller,
          ),
          if (isLoading)
            Center(
              child: Container(
                width: 200.0,
                height: 200.0,
                padding: const EdgeInsets.all(4.0),
                child: Row(
                  mainAxisAlignment: MainAxisAlignment.center,
                  children: <Widget>[
                    SpinKitDoubleBounce(color: Theme.of(context).primaryColor),
                    Container(width: 10.0),
                    Text(context.l10n.loading_text,
                        style: HahaConstant.middleText),
                  ],
                ),
              ),
            )
        ],
      ),
    );
  }
}

///测试 html 代码，不管
const testhtml = "<!DOCTYPE html>"
    "<html>"
    "<head>"
    "<meta charset=\"utf-8\">"
    "<meta name=\"viewport\" content=\"width=device-width,initial-scale=1,minimum-scale=1,maximum-scale=1,user-scalable=no\" />"
    "<title>Local Title</title>"
    "<script>"
    "function callJS(){"
    "alert(\"Android调用了web js\");"
    "}"
    "function callInterface(){"
    "JSCallBackInterface.callback(\"我是web的js哟\");"
    "}"
    "function callInterface2(){"
    "document.location = \"js://Authority?pra1=111&pra2=222\";"
    "}"
    "function clickPrompt(){"
    "Print.postMessage('Hello');"
    "}"
    "</script>"
    "</head>"
    "<body>"
    "<button type=\"button\" id=\"buttonxx\" onclick=\"callInterface()\">点我调用原生android方法</button>"
    "<button type=\"button\" id=\"buttonxx2\" onclick=\"callInterface2()\">点我调用原生android方法2</button>"
    "<button type=\"button\" id=\"buttonxx3\" onclick=\"clickPrompt()\">点我调用原生android方法3</button>"
    "<input></input>"
    "</body>"
    "</html>";
