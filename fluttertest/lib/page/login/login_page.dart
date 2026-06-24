import 'package:flutter/material.dart';
import 'package:flutter_redux/flutter_redux.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:fluttertest/common/config/config.dart';
import 'package:fluttertest/common/local/local_storage.dart';
import 'package:fluttertest/common/localization/extension.dart';
import 'package:fluttertest/common/net/address.dart';
import 'package:fluttertest/common/toast.dart';
import 'package:fluttertest/common/utils/navigator_utils.dart';
import 'package:fluttertest/redux/haha_state.dart';
import 'package:fluttertest/redux/login_redux.dart';
import 'package:fluttertest/common/style/Haha_style.dart';
import 'package:fluttertest/common/utils/common_utils.dart';
import 'package:fluttertest/widget/animated_background.dart';
import 'package:fluttertest/widget/Haha_flex_button.dart';
import 'package:fluttertest/widget/Haha_input_widget.dart';
import 'package:fluttertest/widget/particle/particle_widget.dart';

import '../../widget/style/haha_style.dart';

/// 登录页
/// Created by guoshuyu
/// Date: 2018-07-16
class LoginPage extends StatefulWidget {
  static const String sName = "login";

  const LoginPage({super.key});

  @override
  State createState() {
    return _LoginPageState();
  }
}

class _LoginPageState extends State<LoginPage> with LoginBLoC {
  @override
  Widget build(BuildContext context) {
    /// 触摸收起键盘
    return GestureDetector(
      behavior: HitTestBehavior.translucent,
      onTap: () {
        FocusScope.of(context).requestFocus(FocusNode());
      },
      child: Scaffold(
        body: Consumer(
            builder: (BuildContext context, WidgetRef ref, Widget? child) {
          return Container(
            color: Theme.of(context).primaryColor,
            child: Stack(children: <Widget>[
              const Positioned.fill(child: AnimatedBackground()),
              const Positioned.fill(child: ParticlesWidget(30)),
              Center(
                ///防止overFlow的现象
                child: SafeArea(
                  ///同时弹出键盘不遮挡
                  child: SingleChildScrollView(
                    child: Card(
                      elevation: 5.0,
                      shape: const RoundedRectangleBorder(
                          borderRadius:
                              BorderRadius.all(Radius.circular(10.0))),
                      color: HahaColors.cardWhite,
                      margin: const EdgeInsets.only(left: 30.0, right: 30.0),
                      child: Padding(
                        padding: const EdgeInsets.only(
                            left: 30.0, top: 40.0, right: 30.0, bottom: 0.0),
                        child: Column(
                          mainAxisAlignment: MainAxisAlignment.center,
                          mainAxisSize: MainAxisSize.min,
                          children: <Widget>[
                            const Image(
                                image: AssetImage(HahaIcons.DEFAULT_USER_ICON),
                                width: 90.0,
                                height: 90.0),
                            const Padding(padding: EdgeInsets.all(10.0)),
                            HahaInputWidget(
                              hintText: context.l10n.login_username_hint_text,
                              iconData: HahaIcons.LOGIN_USER,
                              onChanged: (String value) {
                                _userName = value;
                              },
                              controller: userController,
                            ),
                            const Padding(padding: EdgeInsets.all(10.0)),
                            HahaInputWidget(
                              hintText: context.l10n.login_password_hint_text,
                              iconData: HahaIcons.LOGIN_PW,
                              obscureText: true,
                              onChanged: (String value) {
                                _password = value;
                              },
                              controller: pwController,
                            ),
                            const Padding(padding: EdgeInsets.all(10.0)),
                            SizedBox(
                              height: 50,
                              child: Row(
                                children: <Widget>[
                                  Expanded(
                                    child: HahaFlexButton(
                                      text: context.l10n.login_text,
                                      color: Theme.of(context).primaryColor,
                                      textColor: HahaColors.textWhite,
                                      fontSize: 16,
                                      onPress: loginIn,
                                    ),
                                  ),
                                  const SizedBox(
                                    width: 10,
                                  ),
                                  Expanded(
                                    child: HahaFlexButton(
                                      text: context.l10n.oauth_text,
                                      color: Theme.of(context).primaryColor,
                                      textColor: HahaColors.textWhite,
                                      fontSize: 16,
                                      onPress: oauthLogin,
                                    ),
                                  ),
                                ],
                              ),
                            ),
                            const Padding(padding: EdgeInsets.all(15.0)),
                            InkWell(
                              onTap: () {
                                CommonUtils.showLanguageDialog(ref);
                              },
                              child: Text(
                                context.l10n.switch_language,
                                style: const TextStyle(
                                    color: HahaColors.subTextColor),
                              ),
                            ),
                            const Padding(padding: EdgeInsets.all(15.0)),
                          ],
                        ),
                      ),
                    ),
                  ),
                ),
              )
            ]),
          );
        }),
      ),
    );
  }
}

mixin LoginBLoC on State<LoginPage> {
  final TextEditingController userController = TextEditingController();

  final TextEditingController pwController = TextEditingController();

  String? _userName = "";

  String? _password = "";

  @override
  void initState() {
    super.initState();
    initParams();
  }

  @override
  void dispose() {
    super.dispose();
    userController.removeListener(_usernameChange);
    pwController.removeListener(_passwordChange);
  }

  _usernameChange() {
    _userName = userController.text;
  }

  _passwordChange() {
    _password = pwController.text;
  }

  initParams() async {
    _userName = await LocalStorage.get(Config.USER_NAME_KEY);
    _password = await LocalStorage.get(Config.PW_KEY);
    userController.value = TextEditingValue(text: _userName ?? "");
    pwController.value = TextEditingValue(text: _password ?? "");
  }

  loginIn() async {
    showToast(context.l10n.login_deprecated);
    return;
    // if (_userName == null || _userName.isEmpty) {
    //   return;
    // }
    // if (_password == null || _password.isEmpty) {
    //   return;
    // }
    //
    // ///通过 redux 去执行登陆流程
    // StoreProvider.of<HahaState>(context)
    //     .dispatch(LoginAction(context, _userName, _password));
  }

  oauthLogin() async {
    var st = StoreProvider.of<HahaState>(context);
    String? code = await NavigatorUtils.goLoginWebView(
        context, Address.getOAuthUrl(), context.l10n.oauth_text);

    if (code != null && code.isNotEmpty) {
      ///通过 redux 去执行登陆流程
      // ignore: use_build_context_synchronously
      st.dispatch(OAuthAction(context, code));
    }
  }
}
