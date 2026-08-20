import 'dart:async';

import 'package:flutter/material.dart';
import 'package:flutter/rendering.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_study/page/user/widget/user_header.dart';
import 'package:flutter_study/page/user/widget/user_item.dart';

import '../../common/repository/user_repository.dart';
import '../../common/utils/event_utils.dart';
import '../../common/utils/navigator_utils.dart';
import '../../model/event.dart';
import '../../model/user.dart';
import '../../model/user_org.dart';
import '../../provider/app_state_provider.dart';
import '../../widget/haha_event_item.dart';
import '../../widget/only_share_widget.dart';
import '../../widget/pull/nested/haha_sliver_header_delegate.dart';
import '../../widget/pull/nested/nested_refresh.dart';
import '../../widget/state/haha_list_state.dart';
import 'base_person_provider.dart';

/// Created by guoshuyu
/// Date: 2018-08-30

abstract class BasePersonState<T extends StatefulWidget> extends State<T>
    with
        AutomaticKeepAliveClientMixin<T>,
        HahaListState<T>,
        SingleTickerProviderStateMixin {
  final GlobalKey<NestedScrollViewRefreshIndicatorState> refreshIKey =
      GlobalKey<NestedScrollViewRefreshIndicatorState>();

  final List<UserOrg> orgList = [];

  @override
  showRefreshLoading() {
    Future.delayed(const Duration(seconds: 0), () {
      refreshIKey.currentState!.show().then((e) {});
      return true;
    });
  }

  @protected
  renderItem(index, User userInfo, String beStaredCount, Color? notifyColor,
      VoidCallback? refreshCallBack, List<UserOrg> orgList) {
    if (userInfo.type == "Organization") {
      return UserItem(
          UserItemViewModel.fromMap(pullLoadWidgetControl.dataList[index]),
          onPressed: () {
        NavigatorUtils.goPerson(
            context,
            UserItemViewModel.fromMap(pullLoadWidgetControl.dataList[index])
                .userName);
      });
    } else {
      Event event = pullLoadWidgetControl.dataList[index];
      return HahaEventItem(EventViewModel.fromEventMap(event), onPressed: () {
        EventUtils.ActionUtils(context, event, "");
      });
    }
  }

  @override
  bool get wantKeepAlive => true;

  @override
  bool get isRefreshFirst => true;

  @override
  bool get needHeader => true;

  @protected
  FetchHonorDataProvider get headerProvider;

  @protected
  Widget buildContainer(BuildContext context);

  @protected
  getUserOrg(String? userName) {
    if (page <= 1 && userName != null) {
      UserRepository.getUserOrgsRequest(userName, page, needDb: true)
          .then((res) {
        if (res != null && res.result) {
          setState(() {
            orgList.clear();
            orgList.addAll(res.data);
          });
          return res.next?.call();
        }
        return Future.value(null);
      }).then((res) {
        if (res != null && res.result) {
          setState(() {
            orgList.clear();
            orgList.addAll(res.data);
          });
        }
      });
    }
  }

  @protected
  List<Widget> sliverBuilder(
      BuildContext context,
      bool innerBoxIsScrolled,
      User userInfo,
      Color? notifyColor,
      String beStaredCount,
      refreshCallBack) {
    double headerSize = 210;
    double bottomSize = 70;
    double chartSize =
        (userInfo.login != null && userInfo.type == "Organization") ? 70 : 215;

    return <Widget>[
      ///头部信息
      SliverPersistentHeader(
        pinned: true,
        delegate: HahaSliverHeaderDelegate(
            maxHeight: headerSize,
            minHeight: headerSize,
            changeSize: true,
            vSyncs: this,
            snapConfig: FloatingHeaderSnapConfiguration(
              curve: Curves.bounceInOut,
              duration: const Duration(milliseconds: 10),
            ),
            builder: (BuildContext context, double shrinkOffset,
                bool overlapsContent) {
              return Transform.translate(
                offset: Offset(0, -shrinkOffset),
                child: SizedBox.expand(
                  child: UserHeaderItem(
                      userInfo, beStaredCount, Theme.of(context).primaryColor,
                      notifyColor: notifyColor,
                      refreshCallBack: refreshCallBack,
                      orgList: orgList),
                ),
              );
            }),
      ),

      ///悬停的item
      SliverPersistentHeader(
        pinned: true,
        floating: true,
        delegate: HahaSliverHeaderDelegate(
            maxHeight: bottomSize,
            minHeight: bottomSize,
            changeSize: true,
            vSyncs: this,
            snapConfig: FloatingHeaderSnapConfiguration(
              curve: Curves.bounceInOut,
              duration: const Duration(milliseconds: 10),
            ),
            builder: (BuildContext context, double shrinkOffset,
                bool overlapsContent) {
              var radius = Radius.circular(10 - shrinkOffset / bottomSize * 10);
              return SizedBox.expand(
                child: Padding(
                  padding: const EdgeInsets.only(bottom: 10, left: 0, right: 0),
                  child: OnlyShareInstanceWidget(
                    value: headerProvider,
                    child: UserHeaderBottom(userInfo, radius),
                  ),
                ),
              );
            }),
      ),

      ///提交图表
      SliverPersistentHeader(
        delegate: HahaSliverHeaderDelegate(
            maxHeight: chartSize,
            minHeight: chartSize,
            changeSize: true,
            vSyncs: this,
            snapConfig: FloatingHeaderSnapConfiguration(
              curve: Curves.bounceInOut,
              duration: const Duration(milliseconds: 10),
            ),
            builder: (BuildContext context, double shrinkOffset,
                bool overlapsContent) {
              return SizedBox.expand(
                child: SizedBox(
                  height: chartSize,
                  child: UserHeaderChart(userInfo),
                ),
              );
            }),
      ),
    ];
  }

  @override
  Widget build(BuildContext context) {
    super.build(context);// See AutomaticKeepAliveClientMixin.
    ///局部 scoped 的 riverpod provider 方案
    ///配合 @Riverpod(dependencies: [])
    return ProviderScope(
      /// 必要时还可以覆盖
      //overrides: [],
      child: buildContainer(context),
    );
  }

  ///获取用户仓库前100个star统计数据
  getHonor() async {
    var _ = globalContainer.refresh(headerProvider);
  }
}
