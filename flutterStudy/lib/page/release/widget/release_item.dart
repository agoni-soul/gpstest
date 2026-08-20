import 'package:flutter/material.dart';
import 'package:flutter_study/model/release.dart';
import 'package:flutter_study/common/style/haha_style.dart';
import 'package:flutter_study/common/utils/common_utils.dart';
import 'package:flutter_study/widget/haha_card_item.dart';
import 'package:flutter_study/widget/style/haha_style.dart';

/// 版本TagItem
/// Created by guoshuyu
/// Date: 2018-07-30

class ReleaseItem extends StatelessWidget {
  final ReleaseItemViewModel releaseItemViewModel;

  final GestureTapCallback? onPressed;
  final GestureLongPressCallback? onLongPress;

  const ReleaseItem(this.releaseItemViewModel, {super.key, this.onPressed, this.onLongPress});

  @override
  Widget build(BuildContext context) {
    return HahaCardItem(
      child: InkWell(
        onTap: onPressed,
        onLongPress: onLongPress,
        child: Padding(
          padding: const EdgeInsets.only(left: 10.0, top: 15.0, right: 10.0, bottom: 15.0),
          child: Row(
            children: <Widget>[
              Expanded(child: Text(releaseItemViewModel.actionTitle!, style: HahaConstant.smallTextBold)),
              Text(releaseItemViewModel.actionTime ?? "", style: HahaConstant.smallSubText),
            ],
          ),
        ),
      ),
    );
  }
}

class ReleaseItemViewModel {
  String? actionTime;
  String? actionTitle;
  String? actionMode;
  String? actionTarget;
  String? actionTargetHtml;
  String? body;

  ReleaseItemViewModel();

  ReleaseItemViewModel.fromMap(Release map) {
    if (map.publishedAt != null) {
      actionTime = CommonUtils.getNewsTimeStr(map.publishedAt!);
    }
    actionTitle = map.name ?? map.tagName;
    actionTarget = map.targetCommitish;
    actionTargetHtml = map.bodyHtml;
    body = map.body ?? "";
  }
}
