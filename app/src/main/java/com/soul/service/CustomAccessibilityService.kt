package com.soul.service

import android.accessibilityservice.AccessibilityService
import android.app.Notification
import android.app.Service
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.soul.log.DOFLogUtil

class CustomAccessibilityService : AccessibilityService() {
    private val TAG = this::class.java.simpleName
    private var mContext: Context? = null

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "onCreate")
        mContext = applicationContext
        AccessibilityOperator.instance.init(this)
    }

    override fun onStart(intent: Intent?, startId: Int) {
        super.onStart(intent, startId)
        DOFLogUtil.d(TAG, "onStart")
    }

    override fun startService(service: Intent?): ComponentName? {
        DOFLogUtil.d(TAG, "startService")
        return super.startService(service)
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        DOFLogUtil.d(TAG, "onStartCommand")
        return Service.START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        DOFLogUtil.d(TAG, "onDestroy")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        DOFLogUtil.d(TAG, "onAccessibilityEvent: event = $event")
        AccessibilityOperator.instance.updateEvent(event)
        val packageName = AccessibilityOperator.instance.rootNodeInfo?.packageName?.toString()
        pasteToEditTextContent(packageName)
        val accessibilityService = AccessibilityOperator.instance

        if (event.eventType == AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED) {
            val parcelable = event.parcelableData
            if (parcelable !is Notification) {
                if (event.text.isNotEmpty()) {
                    val toastMsg = event.text[0].toString()
                    DOFLogUtil.d(TAG, toastMsg)
                }
            }
        }
        //按下返回键
//        accessibilityService.performGlobalAction(GLOBAL_ACTION_BACK)
        //向下拉出状态栏
//        accessibilityService.performGlobalAction(GLOBAL_ACTION_NOTIFICATIONS)
        //向下拉出状态栏并显示出所有的快捷操作按钮
//        accessibilityService.performGlobalAction(GLOBAL_ACTION_QUICK_SETTINGS)
        //按下HOME键
//        accessibilityService.performGlobalAction(GLOBAL_ACTION_HOME)
        //显示最近任务
//        accessibilityService.performGlobalAction(GLOBAL_ACTION_RECENTS)
        //长按电源键
//        accessibilityService.performGlobalAction(GLOBAL_ACTION_POWER_DIALOG)
        //分屏
//        accessibilityService.performGlobalAction(GLOBAL_ACTION_TOGGLE_SPLIT_SCREEN)
        //锁屏(安卓9.0适用)
//        accessibilityService.performGlobalAction(GLOBAL_ACTION_LOCK_SCREEN)
        //截屏(安卓9.0适用)
//        accessibilityService.performGlobalAction(GLOBAL_ACTION_TAKE_SCREENSHOT)
        //打开快速设置
        accessibilityService.performGlobalAction(GLOBAL_ACTION_QUICK_SETTINGS)
    }

    /**
     * 修改EditText输入框内容。
     * 下面样例修改了QQ搜索输入框内容。
     */
    private fun changeEditTextContent(packageName: String?) {
        getNodeToOperate(packageName)?.let {
            val arguments = Bundle()
            arguments.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, "被无障碍服务修改啦")
            it.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments)
        }
    }



    /**
     * 读取剪贴板内容，粘贴到EditText输入框。
     * 下面样例修改了QQ搜索输入框内容。
     */
    fun pasteToEditTextContent(packageName: String?) {
        getNodeToOperate(packageName)?.let {
            DOFLogUtil.d(TAG, "AccessibilityNodeInfo = $it")
            it.performAction(AccessibilityNodeInfo.ACTION_CLICK)
            it.performAction(AccessibilityNodeInfo.ACTION_LONG_CLICK)
            it.recycle()
        }
    }

    private fun getNodeToOperate(packageName: String?): AccessibilityNodeInfo? {
        if (packageName != null && packageName == "com.soul.gpstest") {
            val nodes = AccessibilityOperator.instance.findNodesById("com.soul.gpstest:id/btn_skip_gps")
            if (nodes != null && nodes.isNotEmpty()) {
                return nodes[0]
            }
        }
        return null
    }

    override fun onInterrupt() {
    }

}