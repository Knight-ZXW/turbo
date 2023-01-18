package com.knightboost.turbo.proxy.plugin

import com.knightboost.turbo.convergence.SuperThreadPoolManager
import com.knightboost.turbo.proxy.PthreadThread

class PluginPthreadThread : PthreadThread {
    constructor() : super()
    constructor(runnable: Runnable) : super(runnable)
    constructor(runnable: Runnable, name: String) : super(runnable, name)
    constructor(name: String) : super(name)
    constructor(threadGroup: ThreadGroup?, runnable: Runnable) : super(threadGroup, runnable)
    constructor(threadGroup: ThreadGroup?, runnable: Runnable, name: String) : super(threadGroup, runnable, name)
    constructor(threadGroup: ThreadGroup?, runnable: Runnable, name: String, stackSize: Long) : super(threadGroup, runnable, name, stackSize)
    constructor(threadGroup: ThreadGroup?, name: String) : super(threadGroup, name)

    override fun isProxyEnable(): Boolean {
        return SuperThreadPoolManager.isPluginEnable && super.isProxyEnable()
    }

}