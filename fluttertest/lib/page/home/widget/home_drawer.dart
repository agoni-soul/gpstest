import 'package:flutter/material.dart';
import 'package:flutter_redux/flutter_redux.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:package_info_plus/package_info_plus.dart';

import '../../../common/config/config.dart';
import '../../../common/local/local_storage.dart';
import '../../../common/logger.dart';
import '../../../common/repository/issue_repository.dart';
import '../../../common/repository/repos_repository.dart';
import '../../../common/utils/common_utils.dart';
import '../../../common/utils/navigator_utils.dart';
import '../../../model/common_list_datatype.dart';
import '../../../model/user.dart';
import '../../../provider/app_state_provider.dart';
import '../../../redux/haha_state.dart';
import '../../../redux/login_redux.dart';
import '../../../widget/haha_flex_button.dart';
import '../../../widget/style/haha_style.dart';

/// 主页drawer
/// Created by guoshuyu
/// Date: 2018-07-18
class HomeDrawer extends StatelessWidget {
  const HomeDrawer({super.key});

  showAboutDialog(BuildContext context, String? versionName) {
    versionName ??= "Null";
    NavigatorUtils.showHahaDialog(
        context: context,
        builder: (BuildContext context) => AboutDialog(
              applicationName: "app_name",
              applicationVersion:
                  "${"app_version"}: ${versionName ?? ""}",
              applicationIcon: const Image(
                  image: AssetImage(HahaIcons.DEFAULT_USER_ICON),
                  width: 50.0,
                  height: 50.0),
              applicationLegalese: "http://github.com/CarGuo",
            ));
  }

  showThemeDialog(BuildContext context, WidgetRef ref) {
    StringList list = [
      "home_theme_default",
      "home_theme_1",
      "home_theme_2",
      "home_theme_3",
      "home_theme_4",
      "home_theme_5",
      "home_theme_6",
    ];
    CommonUtils.showCommitOptionDialog(context, list, (index) {
      ref.read(appThemeStateProvider.notifier).pushTheme(index.toString());
      LocalStorage.save(Config.THEME_COLOR, index.toString());
    }, colorList: CommonUtils.getThemeListColor());
  }

  @override
  Widget build(BuildContext context) {
    return Material(child:
        Consumer(builder: (BuildContext context, WidgetRef ref, Widget? child) {
      var themeData = ref.watch(appThemeStateProvider);
      return StoreBuilder<HahaState>(
        builder: (context, store) {
          User user = store.state.userInfo!;
          return Drawer(
            ///侧边栏按钮Drawer
            child: Container(
              ///默认背景
              color: themeData.primaryColor,
              child: SingleChildScrollView(
                ///item 背景
                child: Container(
                  constraints: BoxConstraints(
                      minHeight: MediaQuery.sizeOf(context).height),
                  child: Material(
                    color: HahaColors.white,
                    child: Column(
                      children: <Widget>[
                        UserAccountsDrawerHeader(
                          //Material内置控件
                          accountName: Text(
                            user.login ?? "---",
                            style: HahaConstant.largeTextWhite,
                          ),
                          accountEmail: Text(
                            user.email ?? user.name ?? "---",
                            style: HahaConstant.normalTextLight,
                          ),
                          //用户名
                          //用户邮箱
                          currentAccountPicture: GestureDetector(
                            //用户头像
                            onTap: () {},
                            child: CircleAvatar(
                              //圆形图标控件
                              backgroundImage: NetworkImage(user.avatar_url ??
                                  HahaIcons.DEFAULT_REMOTE_PIC),
                            ),
                          ),
                          decoration: BoxDecoration(
                            //用一个BoxDecoration装饰器提供背景图片
                            color: themeData.primaryColor,
                          ),
                        ),
                        ListTile(
                          title: Text(
                            "home_reply",
                            style: HahaConstant.normalText,
                          ),
                          onTap: () {
                            String content = "";
                            CommonUtils.showEditDialog(
                              context,
                              "home_reply",
                              (title) {},
                              (res) {
                                content = res;
                              },
                              () {
                                if (content.isEmpty) {
                                  return;
                                }
                                CommonUtils.showLoadingDialog(context);
                                IssueRepository.createIssueRequest(
                                    "CarGuo", "gsy_github_app_flutter", {
                                  "title": "home_reply",
                                  "body": content
                                }).then((result) {
                                  Navigator.pop(context);
                                  Navigator.pop(context);
                                });
                              },
                              titleController: TextEditingController(),
                              valueController: TextEditingController(),
                              needTitle: false,
                              hintText: "feed_back_tip",
                            );
                          },
                        ),
                        ListTile(
                            title: Text(
                              "home_history",
                              style: HahaConstant.normalText,
                            ),
                            onTap: () {
                              NavigatorUtils.gotoCommonList(
                                  context,
                                  "home_history",
                                  "repositoryql",
                                  CommonListDataType.history,
                                  userName: "",
                                  reposName: "");
                            }),
                        ListTile(
                            title: Hero(
                                tag: "home_user_info",
                                child: Material(
                                    color: Colors.transparent,
                                    child: Text(
                                      "home_user_info",
                                      style: HahaConstant.normalTextBold,
                                    ))),
                            onTap: () {
                              NavigatorUtils.gotoUserProfileInfo(context);
                            }),
                        ListTile(
                            title: Text(
                              "home_change_theme",
                              style: HahaConstant.normalText,
                            ),
                            onTap: () {
                              showThemeDialog(context, ref);
                            }),
                        ListTile(
                            title: Text(
                              "home_change_language",
                              style: HahaConstant.normalText,
                            ),
                            onTap: () {
                              CommonUtils.showLanguageDialog(ref);
                            }),
                        ListTile(
                            title: Text(
                              "home_change_grey",
                              style: HahaConstant.normalText,
                            ),
                            onTap: () {
                              ref
                                  .read(appGrepStateProvider.notifier)
                                  .changeGrey();
                            }),
                        ListTile(
                            title: Text(
                              "home_check_update",
                              style: HahaConstant.normalText,
                            ),
                            onTap: () {
                              ReposRepository.getNewsVersion(context, true);
                            }),
                        ListTile(
                            title: Text(
                              "home_about",
                              style: HahaConstant.normalText,
                            ),
                            onLongPress: () {
                              NavigatorUtils.goDebugDataPage(context);
                            },
                            onTap: () {
                              PackageInfo.fromPlatform().then((value) {
                                printLog(value);
                                if (!context.mounted) return;
                                showAboutDialog(context, value.version);
                              });
                            }),
                        ListTile(
                            title: HahaFlexButton(
                              text: "login_out",
                              color: Colors.redAccent,
                              textColor: HahaColors.textWhite,
                              onPress: () {
                                store.dispatch(LogoutAction(context));
                              },
                            ),
                            onTap: () {}),
                      ],
                    ),
                  ),
                ),
              ),
            ),
          );
        },
      );
    }));
  }
}
