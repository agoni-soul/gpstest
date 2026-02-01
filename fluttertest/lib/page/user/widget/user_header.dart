import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_spinkit/flutter_spinkit.dart';
import 'package:flutter_svg/svg.dart';
import 'package:fluttertest/widget/style/haha_style.dart';

import '../../../common/localization/extension.dart';
import '../../../common/utils/common_utils.dart';
import '../../../common/utils/navigator_utils.dart';
import '../../../model/common_list_datatype.dart';
import '../../../model/user.dart';
import '../../../model/user_org.dart';
import '../../../widget/haha_card_item.dart';
import '../../../widget/haha_icon_text.dart';
import '../../../widget/haha_user_icon_widget.dart';
import '../../../widget/only_share_widget.dart';
import '../base_person_provider.dart';

/// 用户详情头部
/// Created by guoshuyu
/// Date: 2018-07-17
///
///
class UserHeaderItem extends StatelessWidget {
  final User userInfo;

  final String beStaredCount;

  final Color? notifyColor;

  final Color themeColor;

  final VoidCallback? refreshCallBack;

  final List<UserOrg>? orgList;

  const UserHeaderItem(this.userInfo, this.beStaredCount, this.themeColor,
      {super.key, this.notifyColor, this.refreshCallBack, this.orgList});

  ///通知Icon
  _getNotifyIcon(BuildContext context, Color? color) {
    if (notifyColor == null) {
      return Container();
    }
    return RawMaterialButton(
        materialTapTargetSize: MaterialTapTargetSize.shrinkWrap,
        padding: const EdgeInsets.only(top: 0.0, right: 5.0, left: 5.0),
        constraints: const BoxConstraints(minWidth: 0.0, minHeight: 0.0),
        child: ClipOval(
          child: Icon(
            HahaIcons.USER_NOTIFY,
            color: color,
            size: 18.0,
          ),
        ),
        onPressed: () {
          NavigatorUtils.goNotifyPage(context).then((res) {
            refreshCallBack?.call();
          });
        });
  }

  ///用户组织
  _renderOrgs(BuildContext context, List<UserOrg>? orgList) {
    if (orgList == null || orgList.isEmpty) {
      return Container();
    }
    List<Widget> list = [];

    renderOrgsItem(UserOrg orgs) {
      return HahaUserIconWidget(
          padding: const EdgeInsets.only(right: 5.0, left: 5.0),
          width: 30.0,
          height: 30.0,
          image: orgs.avatarUrl ?? HahaIcons.DEFAULT_REMOTE_PIC,
          onPressed: () {
            NavigatorUtils.goPerson(context, orgs.login);
          });
    }

    int length = orgList.length > 3 ? 3 : orgList.length;

    list.add(Text("${context.l10n.user_orgs_title}:",
        style: HahaConstant.smallSubLightText));

    for (int i = 0; i < length; i++) {
      list.add(renderOrgsItem(orgList[i]));
    }
    if (orgList.length > 3) {
      list.add(RawMaterialButton(
          onPressed: () {
            NavigatorUtils.gotoCommonList(
                context,
                "${userInfo.login!} 所在组织",
                "org",
                CommonListDataType.userOrgs,
                userName: userInfo.login);
          },
          materialTapTargetSize: MaterialTapTargetSize.shrinkWrap,
          padding: const EdgeInsets.only(right: 5.0, left: 5.0),
          constraints: const BoxConstraints(minWidth: 0.0, minHeight: 0.0),
          child: const Icon(
            Icons.more_horiz,
            color: HahaColors.white,
            size: 18.0,
          )));
    }
    return Row(children: list);
  }

  _renderImg(BuildContext context) {
    return RawMaterialButton(
        onPressed: () {
          if (userInfo.avatar_url != null) {
            NavigatorUtils.gotoPhotoViewPage(context, userInfo.avatar_url);
          }
        },
        materialTapTargetSize: MaterialTapTargetSize.shrinkWrap,
        padding: const EdgeInsets.all(0.0),
        constraints: const BoxConstraints(minWidth: 0.0, minHeight: 0.0),
        child: ClipOval(
            child: FadeInImage.assetNetwork(
          placeholder: HahaIcons.DEFAULT_USER_ICON,
          key: (userInfo.avatar_url != null && userInfo.avatar_url!.isNotEmpty)
              ? ValueKey(userInfo.avatar_url)
              : null,
          //预览图
          fit: BoxFit.fitWidth,
          image: (userInfo.avatar_url != null &&
                  userInfo.avatar_url!.isNotEmpty)
              ? userInfo.avatar_url!
              : "https://github.com/CarGuo/gsy_github_app_flutter/blob/master/logo.png?raw=true",
          width: 80.0,
          height: 80.0,
        )));
  }

  _renderUserInfo(BuildContext context) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: <Widget>[
        Row(
          children: <Widget>[
            ///用户名
            Text(userInfo.login ?? "", style: HahaConstant.largeTextWhiteBold),
            _getNotifyIcon(context, notifyColor),
          ],
        ),
        Text(userInfo.name == null ? "" : userInfo.name!,
            style: HahaConstant.smallSubLightText),

        ///用户组织
        HahaIconText(
          HahaIcons.USER_ITEM_COMPANY,
          userInfo.company ?? "nothing_now",
          HahaConstant.smallSubLightText,
          HahaColors.subLightTextColor,
          10.0,
          padding: 3.0,
        ),

        ///用户位置
        HahaIconText(
          HahaIcons.USER_ITEM_LOCATION,
          userInfo.location ?? "nothing_now",
          HahaConstant.smallSubLightText,
          HahaColors.subLightTextColor,
          10.0,
          padding: 3.0,
        ),
      ],
    );
  }

  _renderBlog(BuildContext context) {
    return Container(

        ///用户博客
        margin: const EdgeInsets.only(top: 6.0, bottom: 2.0),
        alignment: Alignment.topLeft,

        ///用户博客
        child: RawMaterialButton(
          onPressed: () {
            if (userInfo.blog != null) {
              CommonUtils.launchOutURL(userInfo.blog!, context);
            }
          },
          materialTapTargetSize: MaterialTapTargetSize.shrinkWrap,
          padding: const EdgeInsets.all(0.0),
          constraints: const BoxConstraints(minWidth: 0.0, minHeight: 0.0),
          child: HahaIconText(
            HahaIcons.USER_ITEM_LINK,
            userInfo.blog ?? "nothing_now",
            (userInfo.blog == null)
                ? HahaConstant.smallSubLightText
                : HahaConstant.smallActionLightText,
            HahaColors.subLightTextColor,
            10.0,
            padding: 3.0,
            textWidth: MediaQuery.sizeOf(context).width - 50,
          ),
        ));
  }

  @override
  Widget build(BuildContext context) {
    return HahaCardItem(
        color: themeColor,
        elevation: 0,
        margin: const EdgeInsets.all(0.0),
        shape: const RoundedRectangleBorder(
            borderRadius: BorderRadius.only(
                bottomLeft: Radius.circular(0.0),
                bottomRight: Radius.circular(0.0))),
        child: Padding(
          padding: const EdgeInsets.only(
              left: 10.0, top: 10.0, right: 10.0, bottom: 0.0),
          child: Column(
            mainAxisSize: MainAxisSize.max,
            mainAxisAlignment: MainAxisAlignment.spaceAround,
            children: <Widget>[
              Row(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: <Widget>[
                  ///用户头像
                  _renderImg(context),
                  const Padding(padding: EdgeInsets.all(10.0)),
                  Expanded(
                    child: _renderUserInfo(context),
                  ),
                ],
              ),
              _renderBlog(context),

              ///组织
              _renderOrgs(context, orgList),

              ///用户描述
              Container(
                  alignment: Alignment.topLeft,
                  child: Text(
                    userInfo.bio == null ? "" : userInfo.bio!,
                    style: HahaConstant.smallSubLightText,
                    maxLines: 3,
                    overflow: TextOverflow.ellipsis,
                  )),

              ///用户创建时长
              Container(
                  margin: const EdgeInsets.only(top: 6.0, bottom: 2.0),
                  alignment: Alignment.topLeft,
                  child: Text(
                    "user_create_at" +
                        CommonUtils.getDateStr(userInfo.created_at),
                    style: HahaConstant.smallSubLightText,
                    overflow: TextOverflow.ellipsis,
                  )),
              const Padding(padding: EdgeInsets.only(bottom: 5.0)),
            ],
          ),
        ));
  }
}

class UserHeaderBottom extends StatelessWidget {
  final User userInfo;
  final Radius radius;

  const UserHeaderBottom(this.userInfo, this.radius, {super.key});

  ///底部状态栏
  _getBottomItem(String? title, var value, onPressed) {
    String data = value == null ? "" : value.toString();
    TextStyle valueStyle = (value != null && value.toString().length > 6)
        ? HahaConstant.minText
        : HahaConstant.smallSubLightText;
    TextStyle titleStyle = (title != null && title.toString().length > 6)
        ? HahaConstant.minText
        : HahaConstant.smallSubLightText;
    return Expanded(
      child: Center(
          child: RawMaterialButton(
              materialTapTargetSize: MaterialTapTargetSize.shrinkWrap,
              padding: const EdgeInsets.only(top: 5.0),
              constraints: const BoxConstraints(minWidth: 0.0, minHeight: 0.0),
              onPressed: onPressed,
              child: RichText(
                textAlign: TextAlign.center,
                text: TextSpan(
                  children: [
                    TextSpan(text: title, style: titleStyle),
                    TextSpan(text: "\n", style: valueStyle),
                    TextSpan(text: data, style: valueStyle)
                  ],
                ),
              ))),
    );
  }

  @override
  Widget build(BuildContext context) {
    ///用户底部状态
    return HahaCardItem(
      color: Theme.of(context).primaryColor,
      margin: const EdgeInsets.all(0.0),
      shape: RoundedRectangleBorder(
          borderRadius:
              BorderRadius.only(bottomLeft: radius, bottomRight: radius)),
      child: Container(
        alignment: Alignment.center,
        child: Row(
          crossAxisAlignment: CrossAxisAlignment.center,
          children: <Widget>[
            _getBottomItem(
              "user_tab_repos",
              userInfo.public_repos,
              () {
                NavigatorUtils.gotoCommonList(context, userInfo.login,
                    "repository", CommonListDataType.userRepos,
                    userName: userInfo.login);
              },
            ),
            Container(
                width: 0.3,
                height: 40.0,
                alignment: Alignment.center,
                color: HahaColors.subLightTextColor),
            _getBottomItem(
              "user_tab_fans",
              userInfo.followers,
              () {
                NavigatorUtils.gotoCommonList(context, userInfo.login, "user",
                    CommonListDataType.follower,
                    userName: userInfo.login);
              },
            ),
            Container(
                width: 0.3,
                height: 40.0,
                alignment: Alignment.center,
                color: HahaColors.subLightTextColor),
            _getBottomItem(
              "user_tab_focus",
              userInfo.following,
              () {
                NavigatorUtils.gotoCommonList(context, userInfo.login, "user",
                    CommonListDataType.followed,
                    userName: userInfo.login);
              },
            ),
            Container(
                width: 0.3,
                height: 40.0,
                alignment: Alignment.center,
                color: HahaColors.subLightTextColor),
            _getBottomItem(
              "user_tab_star",
              userInfo.starred,
              () {
                NavigatorUtils.gotoCommonList(context, userInfo.login,
                    "repository", CommonListDataType.userStar,
                    userName: userInfo.login);
              },
            ),
            Container(
                width: 0.3,
                height: 40.0,
                alignment: Alignment.center,
                color: HahaColors.subLightTextColor),
            Consumer(
                builder: (BuildContext context, WidgetRef ref, Widget? child) {
              var data = ref.watch(
                  OnlyShareInstanceWidget.of<FetchHonorDataProvider>(context)!);
              return _getBottomItem(
                "user_tab_honor",
                switch (data) {
                  AsyncData(:final value) =>
                    value?.beStaredCount.toString() ?? "---",
                  AsyncError() => "----",
                  _ => "---",
                },
                () {
                  var list = data.when(
                      data: (result) {
                        return result?.honorList;
                      },
                      error: (_, __) => null,
                      loading: () => null);
                  if (list != null && list.isNotEmpty) {
                    NavigatorUtils.goHonorListPage(context, list);
                  }
                },
              );
            })
            // _getBottomItem("user_tab_honor",
            //     honorModel?.beStaredCount, () {
            //   if (honorModel?.honorList != null) {
            //     NavigatorUtils.goHonorListPage(context, honorModel?.honorList);
            //   }
            // }),
          ],
        ),
      ),
    );
  }
}

class UserHeaderChart extends StatelessWidget {
  final User userInfo;

  const UserHeaderChart(this.userInfo, {super.key});

  _renderChart(BuildContext context) {
    double height = 140.0;
    double width = 3 * MediaQuery.sizeOf(context).width / 2;
    if (userInfo.login != null && userInfo.type == "Organization") {
      return Container();
    }
    return (userInfo.login != null)
        ? Card(
            margin: const EdgeInsets.only(
                top: 0.0, left: 10.0, right: 10.0, bottom: 0.0),
            color: HahaColors.white,
            child: SingleChildScrollView(
              scrollDirection: Axis.horizontal,
              child: Container(
                padding: const EdgeInsets.only(left: 10.0, right: 10.0),
                width: width,
                height: height,

                ///svg chart
                child: SvgPicture.network(
                  CommonUtils.getUserChartAddress(userInfo.login!),
                  width: width,
                  height: height - 10,
                  allowDrawingOutsideViewBox: true,
                  placeholderBuilder: (BuildContext context) => SizedBox(
                    height: height,
                    width: width,
                    child: Center(
                      child:
                          SpinKitRipple(color: Theme.of(context).primaryColor),
                    ),
                  ),
                ),
              ),
            ),
          )
        : SizedBox(
            height: height,
            child: Center(
              child: SpinKitRipple(color: Theme.of(context).primaryColor),
            ),
          );
  }

  @override
  Widget build(BuildContext context) {
    return Column(
      children: <Widget>[
        Container(
            margin: const EdgeInsets.only(top: 15.0, bottom: 15.0, left: 12.0),
            alignment: Alignment.topLeft,
            child: Text(
              (userInfo.type == "Organization")
                  ? "user_dynamic_group"
                  : "user_dynamic_title",
              style: HahaConstant.normalTextBold,
              overflow: TextOverflow.ellipsis,
            )),
        _renderChart(context),
      ],
    );
  }
}
