package com.haha.servicerouter.core

import android.annotation.TargetApi
import android.app.Activity
import android.app.Fragment
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import android.text.TextUtils
import android.util.Log
import android.util.Size
import android.util.SizeF
import android.util.SparseArray
import androidx.activity.result.ActivityResultLauncher
import androidx.annotation.IntDef
import java.io.Serializable

/**
 * @auther: haha
 * @Date:   2026/1/3
 * @Detail:
 */
object DOFRouter {
    private val TAG = "DOFRouter"

    @JvmStatic
    @Synchronized
    fun init(context: Context) {
        Log.d(TAG, "Init start")
        Router.init(context)
        Log.d(TAG, "Init end")
    }

    /**
     * 创建一个Navigator 用以发起一个路由请求
     * @param uri 路由地址
     * @return Navigator
     */
    fun create(uri: Uri): Navigator {
        return Navigator(uri)
    }

    /**
     * 创建一个Navigator 用以发起一个路由请求
     * @param path 路由地址
     * @return Navigator
     */
    fun create(path: String): Navigator {
        return Navigator(path)
    }

    /**
     * 开启日志
     */
    @JvmStatic
    fun openDebug() {
//        Logger.openDebug()
    }

    @IntDef(
        Intent.FLAG_GRANT_READ_URI_PERMISSION,
        Intent.FLAG_GRANT_WRITE_URI_PERMISSION,
        Intent.FLAG_FROM_BACKGROUND,
        Intent.FLAG_DEBUG_LOG_RESOLUTION,
        Intent.FLAG_EXCLUDE_STOPPED_PACKAGES,
        Intent.FLAG_INCLUDE_STOPPED_PACKAGES,
        Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION,
        Intent.FLAG_GRANT_PREFIX_URI_PERMISSION,
        Intent.FLAG_ACTIVITY_NO_HISTORY,
        Intent.FLAG_ACTIVITY_SINGLE_TOP,
        Intent.FLAG_ACTIVITY_NEW_TASK,
        Intent.FLAG_ACTIVITY_MULTIPLE_TASK,
        Intent.FLAG_ACTIVITY_CLEAR_TOP,
        Intent.FLAG_ACTIVITY_FORWARD_RESULT,
        Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP,
        Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS,
        Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT,
        Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED,
        Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY,
        Intent.FLAG_ACTIVITY_NEW_DOCUMENT,
        Intent.FLAG_ACTIVITY_NO_USER_ACTION,
        Intent.FLAG_ACTIVITY_REORDER_TO_FRONT,
        Intent.FLAG_ACTIVITY_NO_ANIMATION,
        Intent.FLAG_ACTIVITY_CLEAR_TASK,
        Intent.FLAG_ACTIVITY_TASK_ON_HOME,
        Intent.FLAG_ACTIVITY_RETAIN_IN_RECENTS,
        Intent.FLAG_ACTIVITY_LAUNCH_ADJACENT
    )
    @Retention(AnnotationRetention.SOURCE)
    annotation class FlagInt

    class Navigator {

        var path: String
            private set
        var extras = Bundle()
            private set
        var enterAnim = -1
            private set
        var exitAnim = -1
            private set
        var flags = 0
            private set
        var requestCode = -1
            private set
        var onBeforeCallback: ((navigator: Navigator) -> Unit)? = null
            private set
        var onNotFoundCallback: ((navigator: Navigator) -> Unit)? = null
            private set
        var onArrivedCallback: ((navigator: Navigator) -> Unit)? = null
            private set
        var onInterceptCallback: ((navigator: Navigator) -> Unit)? = null
            private set
        var activity: Activity? = null
            private set
        var fragment: Fragment? = null
            private set
        var fragmentX: androidx.fragment.app.Fragment? = null
            private set
        var options: Bundle? = null
            private set
        var context: Context? = null
            private set
        var resultLauncher: ActivityResultLauncher<Intent>? = null
            private set

        internal constructor(uri: Uri) {
            uri.queryParameterNames.forEach {
                extras.putString(it, uri.getQueryParameter(it))
            }
            this.path = "${if (TextUtils.isEmpty(uri.scheme)) "" else uri.scheme!!.plus("://")}${uri.host ?: ""}${uri.path}"
        }

        internal constructor(path: String) {
            val uri = Uri.parse(path)
            uri.queryParameterNames.forEach {
                extras.putString(it, uri.getQueryParameter(it))
            }
            this.path = "${if (TextUtils.isEmpty(uri.scheme)) "" else uri.scheme!!.plus("://")}${uri.host ?: ""}${uri.path}"
        }

        fun redirect(path: String): Navigator {
            this.path = path
            return this
        }

        fun withExtras(bundle: Bundle): Navigator {
            this.extras.putAll(bundle)
            return this
        }

        fun withInt(key: String?, int: Int): Navigator {
            extras.putInt(key, int)
            return this
        }

        fun withIntArray(key: String?, intArray: IntArray): Navigator {
            extras.putIntArray(key, intArray)
            return this
        }

        fun withIntArrayList(key: String?, int: ArrayList<Int>?): Navigator {
            extras.putIntegerArrayList(key, int)
            return this
        }

        fun withLong(key: String?, long: Long): Navigator {
            extras.putLong(key, long)
            return this
        }

        fun withLongArray(key: String?, long: LongArray?): Navigator {
            extras.putLongArray(key, long)
            return this
        }

        fun withShort(key: String?, short: Short): Navigator {
            extras.putShort(key, short)
            return this
        }

        fun withShortArray(key: String?, short: ShortArray?): Navigator {
            extras.putShortArray(key, short)
            return this
        }

        fun withDouble(key: String?, double: Double): Navigator {
            extras.putDouble(key, double)
            return this
        }

        fun withDoubleArray(key: String?, double: DoubleArray?): Navigator {
            extras.putDoubleArray(key, double)
            return this
        }

        fun withFloat(key: String?, float: Float): Navigator {
            extras.putFloat(key, float)
            return this
        }

        fun withFloatArray(key: String?, float: FloatArray?): Navigator {
            extras.putFloatArray(key, float)
            return this
        }

        fun withString(key: String?, string: String?): Navigator {
            extras.putString(key, string)
            return this
        }

        fun withStringArray(key: String?, string: Array<String>?): Navigator {
            extras.putStringArray(key, string)
            return this
        }

        fun withStringList(key: String?, stringList: ArrayList<String>?): Navigator {
            extras.putStringArrayList(key, stringList)
            return this
        }

        fun withChar(key: String?, char: Char): Navigator {
            extras.putChar(key, char)
            return this
        }

        fun withCharArray(key: String?, char: CharArray?): Navigator {
            extras.putCharArray(key, char)
            return this
        }

        fun withCharSequence(key: String?, charSequence: CharSequence?): Navigator {
            extras.putCharSequence(key, charSequence)
            return this
        }

        fun withCharSequenceArray(key: String?, charSequence: Array<CharSequence>?): Navigator {
            extras.putCharSequenceArray(key, charSequence)
            return this
        }

        fun withCharSequenceArrayList(key: String?, charSequence: ArrayList<CharSequence>?): Navigator {
            extras.putCharSequenceArrayList(key, charSequence)
            return this
        }

        fun withBoolean(key: String?, boolean: Boolean): Navigator {
            extras.putBoolean(key, boolean)
            return this
        }

        fun withBooleanArray(key: String?, booleanArray: BooleanArray?): Navigator {
            extras.putBooleanArray(key, booleanArray)
            return this
        }

        fun withByte(key: String?, byte: Byte): Navigator {
            extras.putByte(key, byte)
            return this
        }

        fun withByteArray(key: String?, byte: ByteArray?): Navigator {
            extras.putByteArray(key, byte)
            return this
        }

        fun withParcelable(key: String?, parcelable: Parcelable?): Navigator {
            extras.putParcelable(key, parcelable)
            return this
        }

        fun withParcelableArray(key: String?, parcelable: Array<Parcelable>?): Navigator {
            extras.putParcelableArray(key, parcelable)
            return this
        }

        fun withParcelableArrayList(key: String?, parcelable: ArrayList<Parcelable>?): Navigator {
            extras.putParcelableArrayList(key, parcelable)
            return this
        }

        fun <T : Parcelable> withSparseParcelableArray(key: String?, parcelable: SparseArray<T>?): Navigator {
            extras.putSparseParcelableArray(key, parcelable)
            return this
        }

        fun withSerializable(key: String?, serializable: Serializable?): Navigator {
            extras.putSerializable(key, serializable)
            return this
        }

        @TargetApi(21)
        fun withSize(key: String?, size: Size?): Navigator {
            extras.putSize(key, size)
            return this
        }

        @TargetApi(21)
        fun withSizeF(key: String?, sizeF: SizeF?): Navigator {
            extras.putSizeF(key, sizeF)
            return this
        }

        fun withBundle(key: String?, bundle: Bundle?): Navigator {
            extras.putBundle(key, bundle)
            return this
        }

        /**
         * animation
         */
        fun withTransition(activity: Activity, enterAnim: Int, exitAnim: Int): Navigator {
            this.activity = activity
            this.enterAnim = enterAnim
            this.exitAnim = exitAnim
            return this
        }

        /**
         * Flags
         */
        fun withFlags(@FlagInt flag: Int): Navigator {
            flags = flags or flag
            return this
        }

        fun withOptions(options: Bundle?): Navigator {
            this.options = options
            return this
        }

        /**
         * callback
         */
        fun onBefore(block: ((navigator: Navigator) -> Unit)?): Navigator {
            this.onBeforeCallback = block
            return this
        }

        fun onArrived(block: ((navigator: Navigator) -> Unit)?): Navigator {
            this.onArrivedCallback = block
            return this
        }

        fun onNotFound(block: ((navigator: Navigator) -> Unit)?): Navigator {
            this.onNotFoundCallback = block
            return this
        }

        fun onIntercept(block: ((navigator: Navigator) -> Unit)?): Navigator {
            this.onInterceptCallback = block
            return this
        }

        /**
         * startActivityForResult
         */
        @JvmOverloads
        fun withRequestCode(activity: Activity, requestCode: Int, options: Bundle? = null): Navigator {
            this.activity = activity
            this.requestCode = requestCode
            this.options = options
            return this
        }

        @JvmOverloads
        fun withRequestCode(fragment: Fragment, requestCode: Int, options: Bundle? = null): Navigator {
            this.fragment = fragment
            this.requestCode = requestCode
            this.options = options
            return this
        }

        @JvmOverloads
        fun withRequestCode(
            fragment: androidx.fragment.app.Fragment,
            requestCode: Int,
            options: Bundle? = null
        ): Navigator {
            fragmentX = fragment
            this.requestCode = requestCode
            this.options = options
            return this
        }

        /**
         * 废弃过时的startActivityForResult,不再需要管理requestCode
         * @param resultLauncher 通过[ActivityResultCaller.registerForActivityResult]获取
         * 可以搭配base_common的扩展方法ActivityResultCaller.startActivityLauncher使用
         * 注意：必须在创建 fragment 或 activity 之前调用 registerForActivityResult()，比如类加载或onCreate
         *
         * 例:
         * class xxActivity{
         *      val launcher=startActivityLauncher{
         *          //result回调逻辑
         *      }
         *
         *      fun jumpPage(){
         *          DOFRouter.create(xxx).withActivityResult(launcher).navigate()
         *      }
         * }
         * */
        fun withActivityResult(
            resultLauncher:ActivityResultLauncher<Intent>
        ): Navigator {
            this.resultLauncher = resultLauncher
            return this
        }

        /**
         * Initiate a routing navigation to the router.
         */
        fun navigate(): Any? {
            return Router.getInstance().navigator(this)
        }

        fun navigate(context: Context): Any? {
            this.context = context
            return Router.getInstance().navigator(this)
        }
    }
}