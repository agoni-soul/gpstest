import 'package:flutter/material.dart';
import 'package:flutter_redux/flutter_redux.dart';
import 'package:redux/redux.dart';

import '../common/localization/extension.dart';
import '../common/repository/user_repository.dart';
import '../common/utils/common_utils.dart';
import '../model/user.dart';
import '../redux/haha_state.dart';
import '../widget/haha_card_item.dart';
import '../widget/style/haha_style.dart';

const String user_profile_name = "名字";
const String user_profile_email = "邮箱";
const String user_profile_link = "链接";
const String user_profile_org = "公司";
const String user_profile_location = "位置";
const String user_profile_info = "简介";

/// 用户信息中心
/// Created by guoshuyu
/// Date: 2018-08-08
class UserProfileInfo extends StatefulWidget {
  const UserProfileInfo({super.key});

  @override
  _UserProfileState createState() => _UserProfileState();
}

class _UserProfileState extends State<UserProfileInfo> {
  _renderItem(
      IconData leftIcon, String title, String value, VoidCallback onPressed) {
    return HahaCardItem(
      child: RawMaterialButton(
        onPressed: onPressed,
        materialTapTargetSize: MaterialTapTargetSize.shrinkWrap,
        padding: const EdgeInsets.all(15.0),
        constraints: const BoxConstraints(minWidth: 0.0, minHeight: 0.0),
        child: Row(
          children: <Widget>[
            Icon(leftIcon),
            Container(
              width: 10.0,
            ),
            Text(title, style: HahaConstant.normalSubText),
            Container(
              width: 10.0,
            ),
            Expanded(child: Text(value, style: HahaConstant.normalText)),
            Container(
              width: 10.0,
            ),
            const Icon(HahaIcons.REPOS_ITEM_NEXT, size: 12.0),
          ],
        ),
      ),
    );
  }

  _showEditDialog(String title, String? value, String key, Store store) {
    String content = value ?? "";
    CommonUtils.showEditDialog(context, title, (title) {}, (res) {
      content = res;
    }, () {
      if (content.isEmpty) {
        return;
      }
      CommonUtils.showLoadingDialog(context);

      UserRepository.updateUserRequest({key: content}, store).then((res) {
        Navigator.of(context).pop();
        if (res != null && res.result) {
          Navigator.of(context).pop();
        }
      });
    },
        titleController: TextEditingController(),
        valueController: TextEditingController(text: value),
        needTitle: false);
  }

  List<Widget> _renderList(User userInfo, Store store) {
    return [
      _renderItem(
          Icons.info, context.l10n.user_profile_name, userInfo.name ?? "---",
          () {
        _showEditDialog(
            context.l10n.user_profile_name, userInfo.name, "name", store);
      }),
      _renderItem(
          Icons.email, context.l10n.user_profile_email, userInfo.email ?? "---",
          () {
        _showEditDialog(
            context.l10n.user_profile_email, userInfo.email, "email", store);
      }),
      _renderItem(
          Icons.link, context.l10n.user_profile_link, userInfo.blog ?? "---",
          () {
        _showEditDialog(
            context.l10n.user_profile_link, userInfo.blog, "blog", store);
      }),
      _renderItem(
          Icons.group, context.l10n.user_profile_org, userInfo.company ?? "---",
          () {
        _showEditDialog(
            context.l10n.user_profile_org, userInfo.company, "company", store);
      }),
      _renderItem(Icons.location_on, context.l10n.user_profile_location,
          userInfo.location ?? "---", () {
        _showEditDialog(context.l10n.user_profile_location, userInfo.location,
            "location", store);
      }),
      _renderItem(
          Icons.message, context.l10n.user_profile_info, userInfo.bio ?? "---",
          () {
        _showEditDialog(
            context.l10n.user_profile_info, userInfo.bio, "bio", store);
      }),
    ];
  }

  @override
  Widget build(BuildContext context) {
    return StoreBuilder<HahaState>(builder: (context, store) {
      return Scaffold(
        appBar: AppBar(
            title: Hero(
                tag: "home_user_info",
                child: Material(
                    color: Colors.transparent,
                    child: Text(
                      context.l10n.home_user_info,
                      style: HahaConstant.normalTextWhite,
                    )))),
        body: Container(
          color: HahaColors.white,
          child: SingleChildScrollView(
            child: Column(
              children: _renderList(store.state.userInfo!, store),
            ),
          ),
        ),
      );
    });
  }
}
