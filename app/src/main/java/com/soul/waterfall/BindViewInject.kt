package com.soul.waterfall

import android.app.Activity
import android.view.View
import android.widget.Toast
import java.lang.annotation.RetentionPolicy


/**
 *     author : yangzy33
 *     time   : 2024-02-21
 *     desc   :
 *     version: 1.0
 */
object BindViewInject {


    /**
     * 注入
     *
     * @param activity
     */
    @JvmStatic
    fun inject(activity: Activity) {
        inject(activity, false)
    }

    fun inject(activity: Activity, isSetOnClickListener: Boolean) {
        //第一步 获取class对象
        val aClass: Class<out Activity> = activity.javaClass
        //第二步 获取类本身定义的所有成员变量
        val declaredFields = aClass.declaredFields
        //第三步 遍历找出有注解的属性
        for (i in declaredFields.indices) {
            val field = declaredFields[i]
            //判断是否用BindView进行注解
            if (field.isAnnotationPresent(BindView::class.java)) {
                //得到注解对象
                val bindView = field.getAnnotation(BindView::class.java)
                //得到注解对象上的id值 这个就是view的id
                val id = bindView.id
                if (id <= 0) {
                    Toast.makeText(activity, "请设置正确的id", Toast.LENGTH_LONG).show()
                    return
                }
                //建立映射关系，找出view
                val view = activity.findViewById<View>(id)
                //修改权限
                field.isAccessible = true
                //第四步 给属性赋值
                try {
                    field[activity] = view
                } catch (e: IllegalAccessException) {
                    e.printStackTrace()
                }
                //第五步 设置点击监听
                if (isSetOnClickListener) {
                    //这里用反射实现 增加练习
                    //第一步 获取这个属性的值
                    val button = field.get(activity)
                    //第二步 获取其class对象
                    val javaClass = button.javaClass
                    //第三步 获取其 setOnClickListener 方法
                    val method =
                        javaClass.getMethod("setOnClickListener", View.OnClickListener::class.java)
                    //第四步 执行此方法
                    method.invoke(button, activity)
                }
            }
        }
    }
}

@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class BindView( //value是默认的，如果只有一个参数，并且名称是value，外面传递时可以直接写值，否则就要通过键值对来传值（例如：value = 1）
    //    int value() default 0;
    val id: Int = 0
)