import 'dart:async';

import 'package:flutter/material.dart';
import 'package:flutter_redux/flutter_redux.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:fluttertest/common/event/http_error_event.dart';
import 'package:fluttertest/common/event/index.dart';
import 'package:fluttertest/common/localization/extension.dart';
import 'package:fluttertest/common/localization/l10n/app_localizations.dart';
import 'package:fluttertest/common/net/code.dart';
import 'package:fluttertest/common/toast.dart';
import 'package:fluttertest/common/utils/navigator_utils.dart';
import 'package:fluttertest/model/user.dart';
import 'package:fluttertest/page/debug/debug_label.dart';
import 'package:fluttertest/page/home/home_page.dart';
import 'package:fluttertest/page/login/login_page.dart';
import 'package:fluttertest/page/photoview_page.dart';
import 'package:fluttertest/page/welcome_page.dart';
import 'package:fluttertest/provider/app_state_provider.dart';
import 'package:fluttertest/redux/haha_state.dart';
import 'package:redux/redux.dart';

class FlutterReduxApp extends StatefulWidget {
  const FlutterReduxApp({super.key});

  @override
  State<FlutterReduxApp> createState() => _FlutterReduxAppState();
}

class _FlutterReduxAppState extends State<FlutterReduxApp>
    with HttpErrorListener {
  /// 创建 Store，引用 HahaState 中的 appReducer
  final store = Store<HahaState>(
    appReducer,
    middleware: middleware,
    initialState: HahaState(
      userInfo: User.empty(),
      login: false,
    ),
  );

  final NavigatorObserver navigatorObserver = NavigatorObserver();

  Locale _checkSupportedLocale(Locale locale) {
    const supportedLocales = AppLocalizations.supportedLocales;
    for (final supportedLocale in supportedLocales) {
      if (supportedLocale.languageCode == locale.languageCode) {
        return locale;
      }
    }
    return const Locale('en', 'US');
  }

  @override
  Widget build(BuildContext context) {
    return UncontrolledProviderScope(
      container: globalContainer,
      child: Consumer(
        builder: (BuildContext context, WidgetRef ref, Widget? child) {
          final (greyApp, appLocale, themeData) = ref.watch(appStateProvider);
          final effectiveLocale = _checkSupportedLocale(appLocale);

          return StoreProvider<HahaState>(
            store: store,
            child: StoreBuilder<HahaState>(builder: (context, store) {
              Widget app = MaterialApp(
                navigatorKey: navKey,
                localizationsDelegates:
                    AppLocalizations.localizationsDelegates,
                supportedLocales: [effectiveLocale],
                locale: effectiveLocale,
                theme: themeData,
                navigatorObservers: [navigatorObserver],
                routes: {
                  WelcomePage.sName: (context) {
                    DebugLabel.showDebugLabel(context);
                    return const WelcomePage();
                  },
                  HomePage.sName: (context) {
                    return NavigatorUtils.pageContainer(
                        const HomePage(), context);
                  },
                  LoginPage.sName: (context) {
                    return NavigatorUtils.pageContainer(
                        const LoginPage(), context);
                  },
                  PhotoViewPage.sName: (context) {
                    return const PhotoViewPage();
                  },
                },
              );

              if (greyApp) {
                app = ColorFiltered(
                  colorFilter: const ColorFilter.mode(
                      Colors.grey, BlendMode.saturation),
                  child: app,
                );
              }

              return app;
            }),
          );
        },
      ),
    );
  }
}

mixin HttpErrorListener on State<FlutterReduxApp> {
  StreamSubscription? stream;

  GlobalKey<NavigatorState> navKey = GlobalKey<NavigatorState>();

  @override
  void initState() {
    super.initState();
    stream = eventBus.on<HttpErrorEvent>().listen((event) {
      errorHandleFunction(event.code, event.message);
    });
  }

  @override
  void dispose() {
    stream?.cancel();
    stream = null;
    super.dispose();
  }

  void errorHandleFunction(int? code, message) {
    final context = navKey.currentContext;
    if (context == null) {
      return;
    }
    switch (code) {
      case Code.NETWORK_ERROR:
        showToast(context.l10n.network_error);
        break;
      case 401:
        showToast(context.l10n.network_error_401);
        break;
      case 403:
        showToast(context.l10n.network_error_403);
        break;
      case 404:
        showToast(context.l10n.network_error_404);
        break;
      case 422:
        showToast(context.l10n.network_error_422);
        break;
      case Code.NETWORK_TIMEOUT:
        showToast(context.l10n.network_error_timeout);
        break;
      case Code.GITHUB_API_REFUSED:
        showToast(context.l10n.github_refused);
        break;
      default:
        showToast("${context.l10n.network_error_unknown} $message");
        break;
    }
  }
}
