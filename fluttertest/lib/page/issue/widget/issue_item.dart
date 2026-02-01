import 'package:flutter/material.dart';

import '../../../common/utils/common_utils.dart';
import '../../../common/utils/navigator_utils.dart';
import '../../../model/issue.dart';
import '../../../widget/haha_card_item.dart';
import '../../../widget/haha_icon_text.dart';
import '../../../widget/haha_user_icon_widget.dart';
import '../../../widget/markdown/haha_markdown_widget.dart';
import '../../../widget/style/haha_style.dart';

/// Issue相关item
/// Created by guoshuyu
/// Date: 2018-07-19
class IssueItem extends StatelessWidget {
  final IssueItemViewModel issueItemViewModel;

  ///点击
  final GestureTapCallback? onPressed;

  ///长按
  final GestureTapCallback? onLongPress;

  ///是否需要底部状态
  final bool hideBottom;

  ///是否需要限制内容行数
  final bool limitComment;

  const IssueItem(this.issueItemViewModel,
      {super.key,
      this.onPressed,
      this.onLongPress,
      this.hideBottom = false,
      this.limitComment = true});

  ///issue 底部状态
  _renderBottomContainer() {
    Color issueStateColor =
        issueItemViewModel.state == "open" ? Colors.green : Colors.red;
    return (hideBottom)
        ? Container()
        : Row(
            children: <Widget>[
              ///issue 关闭打开状态
              HahaIconText(
                HahaIcons.ISSUE_ITEM_ISSUE,
                issueItemViewModel.state,
                TextStyle(
                  color: issueStateColor,
                  fontSize: HahaConstant.smallTextSize,
                ),
                issueStateColor,
                15.0,
                padding: 2.0,
              ),
              const Padding(padding: EdgeInsets.all(2.0)),

              ///issue标号
              Expanded(
                child: Text(issueItemViewModel.issueTag,
                    style: HahaConstant.smallSubText),
              ),

              ///评论数
              HahaIconText(
                HahaIcons.ISSUE_ITEM_COMMENT,
                issueItemViewModel.commentCount,
                HahaConstant.smallSubText,
                HahaColors.subTextColor,
                15.0,
                padding: 2.0,
              ),
            ],
          );
  }

  ///评论内容
  _renderCommentText() {
    return (limitComment)
        ? Container(
            margin: const EdgeInsets.only(top: 6.0, bottom: 2.0),
            alignment: Alignment.topLeft,
            child: Text(
              issueItemViewModel.issueComment,
              style: HahaConstant.smallSubText,
              maxLines: limitComment ? 2 : 1000,
            ),
          )
        : HahaMarkdownWidget(
            markdownData: issueItemViewModel.issueComment,
            baseUrl: "",
            shrinkWrap: true,
            scroll: false,
          );
  }

  @override
  Widget build(BuildContext context) {
    return HahaCardItem(
      child: InkWell(
        onTap: onPressed,
        onLongPress: onLongPress,
        child: Padding(
          padding: const EdgeInsets.only(
              left: 5.0, top: 5.0, right: 10.0, bottom: 8.0),
          child: Row(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: <Widget>[
                ///头像
                HahaUserIconWidget(
                    width: 30.0,
                    height: 30.0,
                    image: issueItemViewModel.actionUserPic,
                    onPressed: () {
                      NavigatorUtils.goPerson(
                          context, issueItemViewModel.actionUser);
                    }),
                Expanded(
                  child: Column(
                    mainAxisSize: MainAxisSize.min,
                    children: <Widget>[
                      Row(
                        children: <Widget>[
                          ///用户名
                          Expanded(
                              child: Text(issueItemViewModel.actionUser!,
                                  style: HahaConstant.smallTextBold)),
                          Text(
                            issueItemViewModel.actionTime,
                            style: HahaConstant.smallSubText,
                            maxLines: 2,
                            overflow: TextOverflow.ellipsis,
                          ),
                        ],
                      ),

                      ///评论内容
                      _renderCommentText(),
                      const Padding(
                        padding: EdgeInsets.only(
                            left: 0.0, top: 2.0, right: 0.0, bottom: 0.0),
                      ),
                      _renderBottomContainer(),
                    ],
                  ),
                ),
              ]),
        ),
      ),
    );
  }
}

class IssueItemViewModel {
  String actionTime = "---";
  String? actionUser = "---";
  String? actionUserPic;

  String issueComment = "---";
  String commentCount = "---";
  String? state = "---";
  String issueTag = "---";
  String number = "---";
  String id = "";

  IssueItemViewModel();

  IssueItemViewModel.fromMap(Issue issueMap, {needTitle = true}) {
    String fullName = CommonUtils.getFullName(issueMap.repoUrl);
    actionTime = CommonUtils.getNewsTimeStr(issueMap.createdAt!);
    actionUser = issueMap.user!.login;
    actionUserPic = issueMap.user!.avatar_url;
    if (needTitle) {
      issueComment = "$fullName- ${issueMap.title!}";
      commentCount = issueMap.commentNum.toString();
      state = issueMap.state;
      issueTag = "#${issueMap.number}";
      number = issueMap.number.toString();
    } else {
      issueComment = issueMap.body ?? "";
      id = issueMap.id.toString();
    }
  }
}
