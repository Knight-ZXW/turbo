package com.knightboost.turbo.proxy

import com.knightboost.turbo.PThreadThreadPoolCache
import com.knightboost.turbo.PthreadUtil
import com.knightboost.turbo.convergence.SuperThreadPoolManager.isEnable
import com.knightboost.turbo.convergence.ThreadProxy
import java.util.concurrent.TimeUnit
import kotlin.jvm.Synchronized

open class PthreadThread : Thread {
    var proxy: ThreadProxy? = null

    constructor() {}
    constructor(runnable: Runnable) : super(runnable)
    constructor(runnable: Runnable, name: String) : super(runnable, name)
    constructor(name: String) : super(name)
    constructor(threadGroup: ThreadGroup?, runnable: Runnable) : super(threadGroup, runnable)
    constructor(threadGroup: ThreadGroup?, runnable: Runnable, name: String) : super(threadGroup, runnable, name)
    constructor(threadGroup: ThreadGroup?, runnable: Runnable, name: String, stackSize: Long) : super(threadGroup, runnable, name, stackSize)
    constructor(threadGroup: ThreadGroup?, name: String) : super(threadGroup, name)

    override fun interrupt() {
        val threadProxy = proxy
        if (threadProxy != null) {
            threadProxy.interrupt()
        } else {
            super.interrupt()
        }
    }

    override fun isInterrupted(): Boolean {
        val curProxy = proxy
        return curProxy?.isInterrupted ?: super.isInterrupted()
    }



    open fun isProxyEnable():Boolean{
        return  isEnable(1)
    }

    @Synchronized override fun start() {
        try {
            if (!isProxyEnable()) {
                super.start()
                return
            }
            if (proxy == null){
                proxy = ThreadProxy(this)
            }
            proxy?.start()
        } catch (unused: OutOfMemoryError) {
            // Q:为什么这里可以进行拦截？A:在创建目标线程的底层实现中，虽然是新的线程创建过程中出现OOM，
            // 但系统会主动抛出OOM到当前线程

            //优化线程
            PThreadThreadPoolCache.trimFirstEmptyPool("PthreadThread")
            //尝试重新进行调度
            PThreadThreadPoolCache.workPool.schedule(
                {
                    //不再进行代理
                    super.start()
                },
                PthreadUtil.delayTime,
                TimeUnit.MILLISECONDS
            )
        }
    }
}