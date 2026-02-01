import 'package:flutter/material.dart';
import 'package:flutter_spinkit/flutter_spinkit.dart';
import 'package:photo_view/photo_view.dart';

import '../widget/haha_common_option_widget.dart';
import '../widget/haha_title_bar.dart';
import '../widget/style/haha_style.dart';

/// 图片预览
/// Created by guoshuyu
/// Date: 2018-08-09

class PhotoViewPage extends StatelessWidget {
  static const String sName = "PhotoViewPage";

  const PhotoViewPage({super.key});

  @override
  Widget build(BuildContext context) {
    final String? url = ModalRoute.of(context)!.settings.arguments as String?;
    return Scaffold(
        floatingActionButton: FloatingActionButton(
          child: const Icon(Icons.file_download),
          onPressed: () {
            /* CommonUtils.saveImage(url).then((res) {
              if (res != null) {
                Fluttertoast.showToast(msg: res);
                if (Platform.isAndroid) {
                  const updateAlbum = const MethodChannel(
                      'com.shuyu.gsygithub.gsygithubflutter/UpdateAlbumPlugin');
                  updateAlbum.invokeMethod('updateAlbum', {
                    'path': res,
                    'name': CommonUtils.splitFileNameByPath(res)
                  });
                }
              }
            });*/
          },
        ),
        appBar: AppBar(
          title:
              HahaTitleBar("", rightWidget: HahaCommonOptionWidget(url: url)),
        ),
        body: Container(
          color: Colors.black,
          child: PhotoView(
              imageProvider:
                  NetworkImage(url ?? HahaIcons.DEFAULT_REMOTE_PIC),
              loadingBuilder: (
                BuildContext context,
                ImageChunkEvent? event,
              ) {
                return Stack(
                  children: <Widget>[
                    Center(
                        child: Image.asset(HahaIcons.DEFAULT_IMAGE,
                            height: 180.0, width: 180.0)),
                    const Center(
                        child: SpinKitFoldingCube(
                            color: Colors.white30, size: 60.0)),
                  ],
                );
              }),
        ));
  }
}
