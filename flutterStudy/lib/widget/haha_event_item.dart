import 'package:flutter/material.dart';
import 'package:flutter_study/widget/style/haha_style.dart';

import '../common/localization/extension.dart';
import '../common/utils/common_utils.dart';
import '../common/utils/event_utils.dart';
import '../common/utils/navigator_utils.dart';
import '../model/event.dart';
import '../model/notification.dart' as Model;
import '../model/repo_commit.dart';
import 'haha_card_item.dart';
import 'haha_user_icon_widget.dart';

/// 事件Item
/// Created by guoshuyu
/// Date: 2018-07-16
class HahaEventItem extends StatelessWidget {
  final EventViewModel eventViewModel;

  final VoidCallback? onPressed;

  final bool needImage;

  const HahaEventItem(this.eventViewModel,
      {super.key, this.onPressed, this.needImage = true});

  @override
  Widget build(BuildContext context) {
    Widget des =
        (eventViewModel.actionDes == null || eventViewModel.actionDes!.isEmpty)
            ? Container()
            : Container(
                margin: const EdgeInsets.only(top: 6.0, bottom: 2.0),
                alignment: Alignment.topLeft,
                child: Text(
                  eventViewModel.actionDes!,
                  style: HahaConstant.smallSubText,
                  maxLines: 3,
                ));

    Widget userImage = (needImage)
        ? HahaUserIconWidget(
            padding: const EdgeInsets.only(top: 0.0, right: 5.0, left: 0.0),
            width: 30.0,
            height: 30.0,
            image: eventViewModel.actionUserPic,
            onPressed: () {
              NavigatorUtils.goPerson(context, eventViewModel.actionUser);
            })
        : Container();
    return HahaCardItem(
        child: TextButton(
            onPressed: onPressed,
            child: Padding(
              padding: const EdgeInsets.only(
                  left: 0.0, top: 10.0, right: 0.0, bottom: 10.0),
              child: Column(
                mainAxisSize: MainAxisSize.min,
                children: <Widget>[
                  Row(
                    children: <Widget>[
                      userImage,
                      Expanded(
                          child: Text(eventViewModel.actionUser!,
                              style: HahaConstant.smallTextBold)),
                      Text(eventViewModel.actionTime,
                          style: HahaConstant.smallSubText),
                    ],
                  ),
                  Container(
                      margin: const EdgeInsets.only(top: 6.0, bottom: 2.0),
                      alignment: Alignment.topLeft,
                      child: Text(eventViewModel.actionTarget!,
                          style: HahaConstant.smallTextBold)),
                  des,
                ],
              ),
            )));
  }
}

class EventViewModel {
  String? actionUser;
  String? actionUserPic;
  String? actionDes;
  late String actionTime;
  String? actionTarget;

  EventViewModel.fromEventMap(Event event) {
    actionTime = CommonUtils.getNewsTimeStr(event.createdAt!);
    actionUser = event.actor!.login;
    actionUserPic = event.actor!.avatar_url;
    var as = EventUtils.getActionAndDes(event);
    actionDes = as.des;
    actionTarget = as.actionStr;
  }

  EventViewModel.fromCommitMap(RepoCommit eventMap) {
    actionTime = CommonUtils.getNewsTimeStr(eventMap.commit!.committer!.date!);
    actionUser = eventMap.commit!.committer!.name;
    actionDes = "sha:${eventMap.sha!}";
    actionTarget = eventMap.commit!.message;
  }

  EventViewModel.fromNotify(BuildContext context, Model.Notification eventMap) {
    actionTime = CommonUtils.getNewsTimeStr(eventMap.updateAt!);
    actionUser = eventMap.repository!.fullName;
    String? type = eventMap.subject!.type;
    String status = eventMap.unread!
        ? context.l10n.notify_unread
        : context.l10n.notify_readed;
    actionDes =
        "${eventMap.reason!}${context.l10n.notify_type}：$type，${context.l10n.notify_status}：$status";
    actionTarget = eventMap.subject!.title;
  }
}
