package com.haha.service.impl.service

import android.app.Application

/**
 * 服务实例首次创建后注入 [Application]。
 */
interface IApplicationAware {
    fun onAttach(app: Application)
}
