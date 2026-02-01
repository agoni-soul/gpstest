import 'package:flutter/material.dart';
import 'package:flutter_spinkit/flutter_spinkit.dart';
import 'package:fluttertest/page/repos/provider/repos_detail_provider.dart';
import 'package:provider/provider.dart';

import '../../common/utils/common_utils.dart';
import '../../widget/markdown/haha_markdown_widget.dart';
import '../../widget/style/haha_style.dart';

/// Readme
/// Created by guoshuyu
/// Date: 2018-07-18

class RepositoryDetailReadmePage extends StatefulWidget {
  const RepositoryDetailReadmePage({super.key});

  @override
  RepositoryDetailReadmePageState createState() =>
      RepositoryDetailReadmePageState();
}

class RepositoryDetailReadmePageState extends State<RepositoryDetailReadmePage>
    with AutomaticKeepAliveClientMixin {
  RepositoryDetailReadmePageState();

  Future? request;

  refreshReadme() {
    context.read<ReposDetailProvider>().refreshReadme();
  }

  @override
  bool get wantKeepAlive => true;

  @override
  void initState() {
    super.initState();
    refreshReadme();
  }

  @override
  void dispose() {
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    super.build(context);

    ///展示 select
    var markdownData =
        context.select<ReposDetailProvider, String?>((p) => p.markdownData);
    var rp = context.read<ReposDetailProvider>();
    var widget = (markdownData == null)
        ? Center(
            child: Container(
              width: 200.0,
              height: 200.0,
              padding: const EdgeInsets.all(4.0),
              child: Row(
                mainAxisAlignment: MainAxisAlignment.center,
                children: <Widget>[
                  SpinKitDoubleBounce(color: Theme.of(context).primaryColor),
                  Container(width: 10.0),
                  Text("loading_text",
                      style: HahaConstant.middleText),
                ],
              ),
            ),
          )
        : HahaMarkdownWidget(
            markdownData: markdownData,
            baseUrl: getRawBaseUrl(
                repoName: rp.reposName,
                userName: rp.userName,
                branch: rp.currentBranch));

    return widget;
  }
}
