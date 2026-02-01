import 'package:flutter/cupertino.dart';
import 'package:fluttertest/page/search/widget/haha_search_drawer.dart';

import '../../common/config/config.dart';
import '../../common/repository/repos_repository.dart';

class SearchBLoC {

  ///搜索仓库还是人
  int selectIndex = 0;

  ///搜索文件
  String? get searchText {
    return textEditingController.text;
  }

  ///排序类型
  String? type = searchFilterType[0].value;

  ///排序
  String? sort = sortType[0].value;

  ///过滤语言
  String? language = searchLanguageType[0].value;

  final TextEditingController textEditingController  = TextEditingController();


  ///获取搜索数据
  getDataLogic(int page) async {
    return await ReposRepository.searchRepositoryRequest(searchText, language, type, sort,
        selectIndex == 0 ? null : 'user', page, Config.PAGE_SIZE);
  }

}