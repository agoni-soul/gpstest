import 'package:flutter/material.dart';
import 'package:fluttertest/widget/style/haha_style.dart';

/// 头像Icon
/// Created by guoshuyu
/// Date: 2018-07-30

class HahaUserIconWidget extends StatelessWidget {
  final String? image;
  final VoidCallback? onPressed;
  final double width;
  final double height;
  final EdgeInsetsGeometry? padding;

  const HahaUserIconWidget(
      {super.key, this.image,
      this.onPressed,
      this.width = 30.0,
      this.height = 30.0,
      this.padding});

  @override
  Widget build(BuildContext context) {
    return RawMaterialButton(
        materialTapTargetSize: MaterialTapTargetSize.shrinkWrap,
        padding:
            padding ?? const EdgeInsets.only(top: 4.0, right: 5.0, left: 5.0),
        constraints: const BoxConstraints(minWidth: 0.0, minHeight: 0.0),
        onPressed: onPressed,
        child: ClipOval(
          child: FadeInImage(
            placeholder: const AssetImage(
              HahaIcons.DEFAULT_USER_ICON,
            ),
            image: NetworkImage(image ?? HahaIcons.DEFAULT_REMOTE_PIC),
            //预览图
            fit: BoxFit.fitWidth,
            width: width,
            height: height,
          ),
        ));
  }
}
