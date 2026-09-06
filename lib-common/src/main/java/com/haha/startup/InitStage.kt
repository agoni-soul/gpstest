package com.haha.startup

/**
 * 启动任务阶段。表按阶段切片，调度器只跑当前阶段。
 */
enum class InitStage {
    /** attachBaseContext：日志、Activity 栈、打点，必须最早 */
    ATTACH_BASE,

    /** onCreate：首屏前要就绪的组件（路由、服务） */
    ON_CREATE,

    /** 主线程空闲：可延后（LeakCanary、预加载） */
    IDLE
}
