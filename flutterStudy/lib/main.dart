import 'dart:async';

import 'package:flutter/gestures.dart';
import 'package:flutter/material.dart';
import 'package:flutter_study/app.dart';
import 'package:flutter_study/common/logger.dart';
import 'package:flutter_study/env/config_wrapper.dart';
import 'package:flutter_study/env/dev.dart';
import 'package:flutter_study/env/env_config.dart';

void main() {
  runZonedGuarded(() {
    ErrorWidget.builder = (FlutterErrorDetails details) {
      Zone.current.handleUncaughtError(details.exception, details.stack!);
      return Material(
        color: Colors.white,
        child: Center(
          child: SingleChildScrollView(
            padding: const EdgeInsets.all(16),
            child: Text(
              '${details.exception}\n${details.stack}',
              style: const TextStyle(color: Colors.red, fontSize: 12),
            ),
          ),
        ),
      );
    };
    runApp(ConfigWrapper(
      config: EnvConfig.fromJson(config),
      child: const FlutterReduxApp(),
    ));

    /// 屏幕刷新率和显示率不一致时的优化，必须挪动到 runApp 之后
    GestureBinding.instance.resamplingEnabled = true;
  }, (Object obj, StackTrace stack) {
    talker.error('Catch Dart error:', obj, stack);
    printLog(obj);
    printLog(stack);
  });
}
