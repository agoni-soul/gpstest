package com.soul.base


/**
 *     author : yangzy33
 *     time   : 2024-08-13
 *     desc   :
 *     version: 1.0
 */
object ActivityCollector {
    private val activitiesList = mutableListOf<BaseActivity>()

    fun addActivity(activity: BaseActivity) {
        activitiesList.add(activity)
    }

    fun removeActivity(activity: BaseActivity) {
        activitiesList.remove(activity)
    }

    fun finishAll() {
        activitiesList.forEach {
            if (!it.isFinishing) {
                it.finish()
            }
        }
    }
}