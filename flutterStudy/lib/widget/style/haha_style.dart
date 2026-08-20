import 'package:flutter/material.dart';

class HahaColors {
  static const int primaryIntValue = 0xFF24292E;

  static const MaterialColor primarySwatch =
  MaterialColor(primaryIntValue, <int, Color>{
    50: Color(primaryIntValue),
    100: Color(primaryIntValue),
    200: Color(primaryIntValue),
    300: Color(primaryIntValue),
    400: Color(primaryIntValue),
    500: Color(primaryIntValue),
    600: Color(primaryIntValue),
    700: Color(primaryIntValue),
    800: Color(primaryIntValue),
    900: Color(primaryIntValue),
  });

  static const String primaryValueString = "#24292E";
  static const String primaryLightValueString = "#42464b";
  static const String primaryDarkValueString = "#121917";
  static const String miWhiteString = "#ececec";
  static const String actionBlueString = "#267aff";
  static const String webDraculaBackgroundColorString = "#282a36";

  static const Color primaryValue = Color(0xFF24292E);
  static const Color primaryLightValue = Color(0xFF42464b);
  static const Color primaryDarkValue = Color(0xFF121917);

  static const Color cardWhite = Color(0xFFFFFFFF);
  static const Color textWhite = Color(0xFFFFFFFF);
  static const Color miWhite = Color(0xffececec);
  static const Color white = Color(0xFFFFFFFF);
  static const Color actionBlue = Color(0xff267aff);
  static const Color subTextColor = Color(0xff959595);
  static const Color subLightTextColor = Color(0xffc4c4c4);

  static const Color mainBackgroundColor = miWhite;

  static const Color mainTextColor = primaryDarkValue;
  static const Color textColorWhite = white;
}

///文本样式
class HahaConstant {
  static const String app_default_share_url =
      "https://github.com/CarGuo/gsy_github_app_flutter";

  static const lagerTextSize = 30.0;
  static const bigTextSize = 23.0;
  static const normalTextSize = 18.0;
  static const middleTextWhiteSize = 16.0;
  static const smallTextSize = 14.0;
  static const minTextSize = 12.0;
  
  static const minText = TextStyle(
    color: HahaColors.subLightTextColor,
    fontSize: minTextSize
  );

  static const smallTextWhite = TextStyle(
    color: HahaColors.textColorWhite,
    fontSize: smallTextSize,
  );

  static const smallText = TextStyle(
    color: HahaColors.mainTextColor,
    fontSize: smallTextSize,
  );

  static const smallTextBold = TextStyle(
    color: HahaColors.mainTextColor,
    fontSize: smallTextSize,
    fontWeight: FontWeight.bold,
  );

  static const smallSubLightText = TextStyle(
    color: HahaColors.subLightTextColor,
    fontSize: smallTextSize,
  );

  static const smallActionLightText = TextStyle(
    color: HahaColors.actionBlue,
    fontSize: smallTextSize,
  );

  static const smallMiLightText = TextStyle(
    color: HahaColors.miWhite,
    fontSize: smallTextSize,
  );

  static const smallSubText = TextStyle(
    color: HahaColors.subTextColor,
    fontSize: smallTextSize,
  );

  static const middleText = TextStyle(
    color: HahaColors.mainTextColor,
    fontSize: middleTextWhiteSize,
  );

  static const middleTextWhite = TextStyle(
    color: HahaColors.textColorWhite,
    fontSize: middleTextWhiteSize,
  );

  static const middleSubText = TextStyle(
    color: HahaColors.subTextColor,
    fontSize: middleTextWhiteSize,
  );

  static const middleSubLightText = TextStyle(
    color: HahaColors.subLightTextColor,
    fontSize: middleTextWhiteSize,
  );

  static const middleTextBold = TextStyle(
    color: HahaColors.mainTextColor,
    fontSize: middleTextWhiteSize,
    fontWeight: FontWeight.bold,
  );

  static const middleTextWhiteBold = TextStyle(
    color: HahaColors.textColorWhite,
    fontSize: middleTextWhiteSize,
    fontWeight: FontWeight.bold,
  );

  static const middleSubTextBold = TextStyle(
    color: HahaColors.subTextColor,
    fontSize: middleTextWhiteSize,
    fontWeight: FontWeight.bold,
  );

  static const normalText = TextStyle(
    color: HahaColors.mainTextColor,
    fontSize: normalTextSize,
  );

  static const normalTextBold = TextStyle(
    color: HahaColors.mainTextColor,
    fontSize: normalTextSize,
    fontWeight: FontWeight.bold,
  );

  static const normalSubText = TextStyle(
    color: HahaColors.subTextColor,
    fontSize: normalTextSize,
  );

  static const normalTextWhite = TextStyle(
    color: HahaColors.textColorWhite,
    fontSize: normalTextSize,
  );

  static const normalTextMitWhiteBold = TextStyle(
    color: HahaColors.miWhite,
    fontSize: normalTextSize,
    fontWeight: FontWeight.bold,
  );

  static const normalTextActionWhiteBold = TextStyle(
    color: HahaColors.actionBlue,
    fontSize: normalTextSize,
    fontWeight: FontWeight.bold,
  );

  static const normalTextLight = TextStyle(
    color: HahaColors.primaryLightValue,
    fontSize: normalTextSize,
  );

  static const largeText = TextStyle(
    color: HahaColors.mainTextColor,
    fontSize: bigTextSize,
  );

  static const largeTextBold = TextStyle(
    color: HahaColors.mainTextColor,
    fontSize: bigTextSize,
    fontWeight: FontWeight.bold,
  );

  static const largeTextWhite = TextStyle(
    color: HahaColors.textColorWhite,
    fontSize: bigTextSize,
  );

  static const largeTextWhiteBold = TextStyle(
    color: HahaColors.textColorWhite,
    fontSize: bigTextSize,
    fontWeight: FontWeight.bold,
  );

  static const largeLargeTextWhite = TextStyle(
    color: HahaColors.textColorWhite,
    fontSize: lagerTextSize,
    fontWeight: FontWeight.bold,
  );

  static const largeLargeText = TextStyle(
    color: HahaColors.primaryValue,
    fontSize: lagerTextSize,
    fontWeight: FontWeight.bold,
  );
}

class HahaIcons {
  static const String FONT_FAMILY = 'wxcIconFont';

  static const String DEFAULT_USER_ICON = 'static/images/logo.png';
  static const String DEFAULT_IMAGE = 'static/images/default_img.png';
  static const String DEFAULT_REMOTE_PIC =
      'http://img.cdn.guoshuyu.cn/gsy_github_app_logo.png';
  
  static const IconData HOME = IconData(
    0xe624,
    fontFamily: HahaIcons.FONT_FAMILY
  );
  static const IconData MORE = IconData(
    0xe674,
    fontFamily: HahaIcons.FONT_FAMILY,
  );
  static const IconData SEARCH = IconData(
    0xe61c,
    fontFamily: HahaIcons.FONT_FAMILY,
  );

  static const IconData MAIN_DT = IconData(
    0xe684,
    fontFamily: HahaIcons.FONT_FAMILY,
  );
  static const IconData MAIN_QS = IconData(
    0xe818,
    fontFamily: HahaIcons.FONT_FAMILY,
  );
  static const IconData MAIN_MY = IconData(
    0xe6d0,
    fontFamily: HahaIcons.FONT_FAMILY,
  );
  static const IconData MAIN_SEARCH = IconData(
    0xe61c,
    fontFamily: HahaIcons.FONT_FAMILY,
  );

  static const IconData LOGIN_USER = IconData(
    0xe666,
    fontFamily: HahaIcons.FONT_FAMILY,
  );
  static const IconData LOGIN_PW = IconData(
    0xe60e,
    fontFamily: HahaIcons.FONT_FAMILY,
  );

  static const IconData REPOS_ITEM_USER = IconData(
    0xe63e,
    fontFamily: HahaIcons.FONT_FAMILY,
  );
  static const IconData REPOS_ITEM_STAR = IconData(
    0xe643,
    fontFamily: HahaIcons.FONT_FAMILY,
  );
  static const IconData REPOS_ITEM_FORK = IconData(
    0xe67e,
    fontFamily: HahaIcons.FONT_FAMILY,
  );
  static const IconData REPOS_ITEM_ISSUE = IconData(
    0xe661,
    fontFamily: HahaIcons.FONT_FAMILY,
  );

  static const IconData REPOS_ITEM_STARED = IconData(
    0xe698,
    fontFamily: HahaIcons.FONT_FAMILY,
  );
  static const IconData REPOS_ITEM_WATCH = IconData(
    0xe681,
    fontFamily: HahaIcons.FONT_FAMILY,
  );
  static const IconData REPOS_ITEM_WATCHED = IconData(
    0xe629,
    fontFamily: HahaIcons.FONT_FAMILY,
  );
  static const IconData REPOS_ITEM_DIR = Icons.folder;
  static const IconData REPOS_ITEM_FILE = IconData(
    0xea77,
    fontFamily: HahaIcons.FONT_FAMILY,
  );
  static const IconData REPOS_ITEM_NEXT = IconData(
    0xe610,
    fontFamily: HahaIcons.FONT_FAMILY,
  );

  static const IconData USER_ITEM_COMPANY = IconData(
    0xe63e,
    fontFamily: HahaIcons.FONT_FAMILY,
  );
  static const IconData USER_ITEM_LOCATION = IconData(
    0xe7e6,
    fontFamily: HahaIcons.FONT_FAMILY,
  );
  static const IconData USER_ITEM_LINK = IconData(
    0xe670,
    fontFamily: HahaIcons.FONT_FAMILY,
  );
  static const IconData USER_NOTIFY = IconData(
    0xe600,
    fontFamily: HahaIcons.FONT_FAMILY,
  );

  static const IconData ISSUE_ITEM_ISSUE = IconData(
    0xe661,
    fontFamily: HahaIcons.FONT_FAMILY,
  );
  static const IconData ISSUE_ITEM_COMMENT = IconData(
    0xe6ba,
    fontFamily: HahaIcons.FONT_FAMILY,
  );
  static const IconData ISSUE_ITEM_ADD = IconData(
    0xe662,
    fontFamily: HahaIcons.FONT_FAMILY,
  );

  static const IconData ISSUE_EDIT_H1 = Icons.filter_1;
  static const IconData ISSUE_EDIT_H2 = Icons.filter_2;
  static const IconData ISSUE_EDIT_H3 = Icons.filter_3;
  static const IconData ISSUE_EDIT_BOLD = Icons.format_bold;
  static const IconData ISSUE_EDIT_ITALIC = Icons.format_italic;
  static const IconData ISSUE_EDIT_QUOTE = Icons.format_quote;
  static const IconData ISSUE_EDIT_CODE = Icons.format_shapes;
  static const IconData ISSUE_EDIT_LINK = Icons.insert_link;

  static const IconData NOTIFY_ALL_READ = IconData(
    0xe62f,
    fontFamily: HahaIcons.FONT_FAMILY,
  );

  static const IconData PUSH_ITEM_EDIT = Icons.mode_edit;
  static const IconData PUSH_ITEM_ADD = Icons.add_box;
  static const IconData PUSH_ITEM_MIN = Icons.indeterminate_check_box;
  
}
