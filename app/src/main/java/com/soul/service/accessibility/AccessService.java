package com.soul.service;

import android.view.accessibility.AccessibilityEvent;
import android.graphics.Rect;
import android.view.accessibility.AccessibilityNodeInfo;
import java.util.List;

/**
 * 操作类࿰c;在这里实现具体逻辑
 */
public class AccessService extends BaseService {
    private String appPackageName = "com.soul.gpstest";
    private Boolean refresh = true; // 控制在未处理完逻辑前不要进入逻辑空间

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        String packagename = event.getPackageName() == null ? "" : event.getPackageName().toString();
        if (!packagename.equals(appPackageName)) {// 如果活动APP不是目标APP则不响应
            return;
        }
        int eventType = event.getEventType();
        switch (eventType) {
            case AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED:// 捕获窗口内容改变事件
                if (packagename.equals(appPackageName)) {
                    if (refresh) {
                        refresh = false;
                        AccessibilityNodeInfo nodeOne = findViewByText("1");
                        if (nodeOne != null) {
                            performViewClick(nodeOne);
                            sleep(500);
                        }
                        // 有些view是没有text的࿰c;就可以通过ID、类名等属性来获取
                        AccessibilityNodeInfo nodeAdd = findViewByID("com.soul.gpstest:id/op_add");
                        if (nodeOne != null) {
                            performViewClick(nodeAdd);
                            sleep(500);
                        }
                        // 查找所有的2࿰c;并点击
                        List<AccessibilityNodeInfo> nodeOneList = findNodesByText("2");
                        if (nodeOneList != null && nodeOneList.size() != 0) {
                            for (int i = 0; i < nodeOneList.size(); i++) {
                                AccessibilityNodeInfo node = nodeOneList.get(i);
                                if (node != null) {
                                    Rect rect = new Rect();
                                    node.getBoundsInScreen(rect);
                                    int moveToX = (rect.left + rect.right) / 2;
                                    int moveToY = (rect.top + rect.bottom) / 2;
                                    int lineToX = (rect.left + rect.right) / 2;
                                    int lineToY = (rect.top + rect.bottom) / 2;
                                    // 有些View是不能点击࿰c;这时候可以用手势来处理
                                    gesture(moveToX, moveToY, lineToX, lineToY, 100L, 400L);
                                    sleep(500);
                                }
                            }
                        }
                        nodeAdd = findViewByID("com.huawei.calculator:id/op_add");
                        if (nodeOne != null) {
                            performViewClick(nodeAdd);
                            sleep(500);
                        }
                        // getRooTinActiveWindow返回整个view的root节点࿰c;深度优先遍历查找所有的3࿰c;并点击
                        clickNodesByText("3", getRootInActiveWindow());
                        sleep(500);
                        AccessibilityNodeInfo nodeEq = findViewByID("com.huawei.calculator:id/eq");
                        if (nodeOne != null) {
                            performViewClick(nodeEq);
                            sleep(500);
                        }
                        // 更多的操作请看Baseservice࿰c;或者自行百度
                        refresh = true;
                    }
                }
                break;
            default:
                break;
        }
    }
}


