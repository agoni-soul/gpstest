import 'package:flutter/material.dart';
import 'package:flutter_study/common/localization/extension.dart';
import 'package:flutter_study/common/utils/navigator_utils.dart';
import 'package:flutter_study/page/repos/widget/repos_item.dart';

/// 荣耀list
/// Created by guoshuyu
/// on 2018/7/22.
class HonorListPage extends StatefulWidget {
  final List? list;

  const HonorListPage(this.list, {super.key});

  @override
  _HonorListPageState createState() => _HonorListPageState();
}

class _HonorListPageState extends State<HonorListPage> {
  _renderItem(item) {
    ReposViewModel reposViewModel = ReposViewModel.fromMap(item);
    return ReposItem(reposViewModel, onPressed: () {
      NavigatorUtils.goReposDetail(
          context, reposViewModel.ownerName, reposViewModel.repositoryName);
    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
          title: Text(
        context.l10n.user_tab_honor,
        maxLines: 1,
        overflow: TextOverflow.ellipsis,
      )),
      body: ListView.builder(
        itemBuilder: (context, index) {
          return _renderItem(widget.list![index]);
        },
        itemCount: widget.list!.length,
      ),
    );
  }
}
