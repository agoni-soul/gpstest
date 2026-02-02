import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';
import 'package:fluttertest/widget/style/haha_style.dart';

import '../widget/haha_tabs.dart' as HahaTab;

class TabberWidget extends StatefulWidget {
  final TabType type;
  final bool resizeToAvoidBottomPadding;
  final List<Widget>? tabItems;
  final List<Widget>? tabViews;
  final Color? backgroundColor;
  final Color? indicatorColor;
  final Widget? title;
  final Widget? drawer;
  final Widget? floatingActionButton;
  final FloatingActionButtonLocation? floatingActionButtonLocation;
  final Widget? bottomBar;
  final List<Widget>? footerButtons;
  final ValueChanged<int>? onPageChanged;
  final ValueChanged<int>? onDoublePress;
  final ValueChanged<int>? onSinglePress;

  const TabberWidget({
    super.key,
    this.type = TabType.top,
    this.tabItems,
    this.tabViews,
    this.backgroundColor,
    this.indicatorColor,
    this.title,
    this.drawer,
    this.bottomBar,
    this.onDoublePress,
    this.onSinglePress,
    this.floatingActionButtonLocation,
    this.floatingActionButton,
    this.resizeToAvoidBottomPadding = true,
    this.footerButtons,
    this.onPageChanged,
  });

  @override
  State<TabberWidget> createState() => _TabberWidgetState();
}

class _TabberWidgetState extends State<TabberWidget>
    with SingleTickerProviderStateMixin {
  final PageController _pageController = PageController();

  TabController? _tabController;

  int _index = 0;

  @override
  void initState() {
    super.initState();
    _tabController =
        TabController(vsync: this, length: widget.tabItems!.length);
  }

  @override
  void dispose() {
    _tabController!.dispose();
    super.dispose();
  }

  void _navigationPageChanged(int index) {
    if (_index == index) return;
    _index = index;
    _tabController!.animateTo(index);
    widget.onPageChanged?.call(index);
  }

  void _navigationTopClick(int index) {
    if (_index == index) return;
    _index = index;
    widget.onPageChanged?.call(index);

    /// 不想要动画
    _pageController.jumpTo(MediaQuery.sizeOf(context).width * index);
    widget.onSinglePress?.call(index);
  }

  void _navigationDoubleTapClick(int index) {
    _navigationTopClick(index);
    widget.onDoublePress?.call(index);
  }

  @override
  Widget build(BuildContext context) {
    if (widget.type == TabType.top) {
      // 顶部tab bar
      return Scaffold(
        backgroundColor: HahaColors.mainBackgroundColor,
        resizeToAvoidBottomInset: widget.resizeToAvoidBottomPadding,
        floatingActionButton: SafeArea(child: widget.floatingActionButton ?? Container()),
        floatingActionButtonLocation: widget.floatingActionButtonLocation,
        persistentFooterButtons: widget.footerButtons,
        appBar: AppBar(
          backgroundColor: Theme.of(context).primaryColor,
          title: widget.title,
          bottom: TabBar(
            controller: _tabController,
            tabs: widget.tabItems!,
            indicatorColor: widget.indicatorColor,
            onTap: _navigationTopClick,
          ),
        ),
        body: PageView(
          controller: _pageController,
          onPageChanged: _navigationPageChanged,
          children: widget.tabViews!,
        ),
      );
    } else {
      return Scaffold(
        drawer: widget.drawer,
        appBar: AppBar(
          backgroundColor: Theme.of(context).primaryColor,
          title: widget.title,
        ),
        body: PageView(
          controller: _pageController,
          onPageChanged: _navigationPageChanged,
          children: widget.tabViews!,
        ),
        bottomNavigationBar: Material(
          color: Theme.of(context).primaryColor,
          child: SafeArea(
              child: HahaTab.TabBar(
                //TabBar导航标签，底部导航放到Scaffold的bottomNavigationBar中
                controller: _tabController,
                //配置控制器
                tabs: widget.tabItems!,
                indicatorColor: widget.indicatorColor,
                onDoubleTap: _navigationDoubleTapClick,
              )
          ),
        ),
      );
    }
  }
}

enum TabType { top, bottom }
