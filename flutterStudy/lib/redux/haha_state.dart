// ignore_for_file: implicit_call_tearoffs


import 'package:flutter_study/redux/user_redux.dart';
import 'package:redux/redux.dart';

import '../model/user.dart';
import 'login_redux.dart';
import 'middleware/epic_middleware.dart';

/**
 * Redux全局State
 * Created by guoshuyu
 * Date: 2018-07-16
 */

///全局Redux store 的对象，保存State数据
class HahaState {
  ///用户信息
  User? userInfo;


  ///是否登录
  bool? login;

  ///构造方法
  HahaState(
      {this.userInfo,
      this.login,});
}

///创建 Reducer
///源码中 Reducer 是一个方法 typedef State Reducer<State>(State state, dynamic action);
///我们自定义了 appReducer 用于创建 store
HahaState appReducer(HahaState state, action) {
  return HahaState(
    ///通过 UserReducer 将 GSYState 内的 userInfo 和 action 关联在一起
    userInfo: UserReducer(state.userInfo, action),
    login: LoginReducer(state.login, action),
  );
}

final List<Middleware<HahaState>> middleware = [
  EpicMiddleware<HahaState>(loginEpic),
  EpicMiddleware<HahaState>(userInfoEpic),
  EpicMiddleware<HahaState>(oauthEpic),
  UserInfoMiddleware(),
  LoginMiddleware(),
];
