package com.knightboost.turbo.convergence

import com.knightboost.turbo.Turbo.enable

object SuperThreadPoolManager {

    public final val THREAD__FLAG = 1;
    public final val THREAD_POOL_EXECUTOR_FLAG = 1 shl 1;
    public final val HANDLER_THREAD_FLAG = 1 shl 2;
    public final val SCHEDULED_THREAD_POOL_EXECUTOR_FLAG = 1 shl 3;


    @Volatile
    var enable: Int = 0
    @Volatile
    var threadFactoryProxyEnable = false
    var scheduleThreadPoolFactoryProxyEnable = false

    @Volatile
    var isPluginEnable = false

    //TODO threadNameTransformer

    var threadNameTransformer: IThreadNameTransformer? = null

    var enableBlockFetchStack = true

    val defaultScheduledThreadKeepAliveTime = 1


    var enablePriority = true

    fun isEnable(flag: Int): Boolean {
        return (enable and flag) == flag
    }

}