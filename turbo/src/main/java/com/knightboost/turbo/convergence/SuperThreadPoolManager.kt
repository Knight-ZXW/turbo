package com.knightboost.turbo.convergence

object SuperThreadPoolManager {

    @Volatile
    var enable: Int = 0
    var isPluginEnable = false

    //TODO threadNameTransformer

    var threadNameTransformer: IThreadNameTransformer? = null

    var enableBlockFetchStack = true

    val defaultScheduledThreadKeepAliveTime = 1

    val mDredgeHandler = DredgeHandler()

    var enablePriority = true

    fun isEnable(flag: Int): Boolean {
        return (enable and flag) == flag
    }

}