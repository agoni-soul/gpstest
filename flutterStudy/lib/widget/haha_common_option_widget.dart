import 'package:flutter/material.dart';
import 'package:flutter_study/widget/style/haha_style.dart';
import 'package:share_plus/share_plus.dart';

import '../common/utils/common_utils.dart';

/// Created by guoshuyu
/// Date: 2018-07-26
class HahaCommonOptionWidget extends StatelessWidget {
  final List<HahaOptionModel>? otherList;

  final String? url;

  const HahaCommonOptionWidget({super.key, this.otherList, String? url})
      : url = (url == null) ? HahaConstant.app_default_share_url : url;

  _renderHeaderPopItem(List<HahaOptionModel> list) {
    return PopupMenuButton<HahaOptionModel>(
      child: const Icon(HahaIcons.MORE),
      onSelected: (model) {
        model.selected(model);
      },
      itemBuilder: (BuildContext context) {
        return _renderHeaderPopItemChild(list);
      },
    );
  }

  _renderHeaderPopItemChild(List<HahaOptionModel> data) {
    List<PopupMenuEntry<HahaOptionModel>> list = [];
    for (HahaOptionModel item in data) {
      list.add(PopupMenuItem<HahaOptionModel>(
        value: item,
        child: Text(item.name),
      ));
    }
    return list;
  }

  @override
  Widget build(BuildContext context) {
    List<HahaOptionModel> constList = [
      HahaOptionModel("option_web", "option_web", (model) {
        CommonUtils.launchOutURL(url, context);
      }),
      HahaOptionModel("option_copy", "option_copy",
          (model) {
        CommonUtils.copy(url ?? "", context);
      }),
      HahaOptionModel("option_share", "option_share",
          (model) {
        SharePlus.instance.share(ShareParams(text: "option_share_title" + (url ?? "")));
      }),
    ];
    var list = [...constList, ...?otherList];
    return _renderHeaderPopItem(list);
  }
}

class HahaOptionModel {
  final String name;
  final String value;
  final PopupMenuItemSelected<HahaOptionModel> selected;

  HahaOptionModel(this.name, this.value, this.selected);
}
