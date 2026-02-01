import 'package:flutter/material.dart';
import 'package:flutter_redux/flutter_redux.dart';
import 'package:fluttertest/page/user/base_person_provider.dart';
import 'package:fluttertest/page/user/base_person_state.dart';
import 'package:redux/redux.dart';

import '../common/repository/event_repository.dart';
import '../common/repository/user_repository.dart';
import '../redux/haha_state.dart';
import '../redux/user_redux.dart';
import '../widget/pull/nested/haha_nested_pull_load_widget.dart';
import '../widget/style/haha_style.dart';

/// 主页我的tab页
/// Created by guoshuyu
/// Date: 2018-07-16
class MyPage extends StatefulWidget {
  const MyPage({super.key});

  @override
  MyPageState createState() => MyPageState();
}

class MyPageState extends BasePersonState<MyPage> {
  final ScrollController scrollController = ScrollController();

  String beStaredCount = '---';

  Color notifyColor = HahaColors.subTextColor;

  Store<HahaState>? _getStore() {
    return StoreProvider.of(context);
  }

  ///从全局状态中获取我的用户名
  _getUserName() {
    if (_getStore()?.state.userInfo == null) {
      return null;
    }
    return _getStore()?.state.userInfo?.login;
  }

  ///从全局状态中获取我的用户类型
  getUserType() {
    if (_getStore()?.state.userInfo == null) {
      return null;
    }
    return _getStore()?.state.userInfo?.type;
  }

  ///更新通知图标颜色
  _refreshNotify() {
    UserRepository.getNotifyRequest(false, false, 0).then((res) {
      Color newColor;
      if (res != null && res.result && res.data.length > 0) {
        newColor = HahaColors.actionBlue;
      } else {
        newColor = HahaColors.subLightTextColor;
      }
      if (isShow) {
        setState(() {
          notifyColor = newColor;
        });
      }
    });
  }

  scrollToTop() {
    if (scrollController.offset <= 0) {
      scrollController
          .animateTo(0,
              duration: const Duration(milliseconds: 600), curve: Curves.linear)
          .then((_) {
        showRefreshLoading();
      });
    } else {
      scrollController.animateTo(0,
          duration: const Duration(milliseconds: 600), curve: Curves.linear);
    }
  }

  @override
  bool get wantKeepAlive => true;

  @override
  void initState() {
    pullLoadWidgetControl.needHeader = true;
    super.initState();
  }

  _getDataLogic() async {
    if (_getUserName() == null) {
      return [];
    }
    if (getUserType() == "Organization") {
      return await UserRepository.getMemberRequest(_getUserName(), page);
    }
    return await EventRepository.getEventRequest(_getUserName(),
        page: page, needDb: page <= 1);
  }

  @override
  requestRefresh() async {
    if (_getUserName() != null) {
      /*User.getUserInfo(null).then((res) {
        if (res != null && res.result) {
          _getStore()?.dispatch(UpdateUserAction(res.data));
          //todo getUserOrg(_getUserName());
        }
      });*/

      ///通过 redux 提交更新用户数据行为
      ///触发网络请求更新
      _getStore()?.dispatch(FetchUserAction());

      ///获取用户组织信息
      getUserOrg(_getUserName());

      ///获取用户仓库前100个star统计数据
      getHonor();
      _refreshNotify();
    }
    return await _getDataLogic();
  }

  @override
  requestLoadMore() async {
    return await _getDataLogic();
  }

  @override
  bool get isRefreshFirst => false;

  @override
  bool get needHeader => false;

  @override
  FetchHonorDataProvider get headerProvider {
    return fetchHonorDataProvider(_getUserName());
  }

  @override
  void didChangeDependencies() {
    if (pullLoadWidgetControl.dataList.isEmpty) {
      showRefreshLoading();
    }
    super.didChangeDependencies();
  }

  @override
  Widget buildContainer(BuildContext context) {
    return StoreBuilder<HahaState>(
      builder: (context, store) {
        return HahaNestedPullLoadWidget(
          pullLoadWidgetControl,
          (BuildContext context, int index) => renderItem(
              index, store.state.userInfo!, beStaredCount, notifyColor, () {
            _refreshNotify();
          }, orgList),
          handleRefresh,
          onLoadMore,
          scrollController: scrollController,
          refreshKey: refreshIKey,
          headerSliverBuilder: (context, innerBoxIsScrolled) {
            return sliverBuilder(context, innerBoxIsScrolled,
                store.state.userInfo!, notifyColor, beStaredCount, () {
              _refreshNotify();
            });
          },
        );
      },
    );
  }
}
