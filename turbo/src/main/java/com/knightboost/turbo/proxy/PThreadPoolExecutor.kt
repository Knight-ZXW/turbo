package com.knightboost.turbo.proxy

import android.os.Build
import com.knightboost.turbo.*
import com.knightboost.turbo.PthreadUtil.isEnableHook
import com.knightboost.turbo.convergence.SuperThreadPoolManager
import com.knightboost.turbo.convergence.ThreadFactoryProxy
import java.util.concurrent.*

open class PThreadPoolExecutor : DefaultThreadPoolProxy {

    constructor(
        corePoolSize: Int, maximumPoolSize: Int, keepAliveTime: Long, unit: TimeUnit, workQueue: BlockingQueue<Runnable>
    ) : super(
        corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue
    ) {
        settingCoreThreadTimeOut()
    }

    constructor(corePoolSize: Int, maximumPoolSize: Int, keepAliveTime: Long, unit: TimeUnit, workQueue: BlockingQueue<Runnable>, threadFactory: ThreadFactory) : super(
        corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory
    ) {
        settingCoreThreadTimeOut()

    }

    constructor(corePoolSize: Int, maximumPoolSize: Int, keepAliveTime: Long, unit: TimeUnit, workQueue: BlockingQueue<Runnable>, handler: RejectedExecutionHandler) : super(
        corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, handler
    ) {
        settingCoreThreadTimeOut()
    }

    constructor(
        corePoolSize: Int, maximumPoolSize: Int, keepAliveTime: Long, unit: TimeUnit, workQueue: BlockingQueue<Runnable>, threadFactory: ThreadFactory, handler: RejectedExecutionHandler
    ) : super(
        corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, handler
    ) {
        settingCoreThreadTimeOut()
    }

    private fun settingCoreThreadTimeOut() {
        if (isEnableHook()) {
            if (!allowsCoreThreadTimeOut()) { //强制允许 coreThread TimeOut
                setKeepAliveTime(30L.coerceAtLeast(getKeepAliveTime(TimeUnit.SECONDS)), TimeUnit.SECONDS)
                try {
                    allowCoreThreadTimeOut(true)
                } catch (e: Throwable) {
                }
            }
            PThreadThreadPoolCache.addCache(this)
        }
    }

    //TODO 这个函数应该被 lancet 插桩处理
    override fun allowCoreThreadTimeOut(allow: Boolean) {
        try {
            var allowCoreTimeOut = true
            if (SuperThreadPoolManager.enableBlockFetchStack && !allow) {
                allowCoreTimeOut = false
            }
            super.allowCoreThreadTimeOut(allowCoreTimeOut)
        } catch (e: Throwable) {
        }
    }

    override fun execute(runnable: Runnable) {
        if (!isEnableHook()) {
            super.execute(runnable)
            return
        }
        try {
            super.execute(runnable)
        } catch (e: OutOfMemoryError) {
            val freeExecutor = PThreadThreadPoolCache.findFreeExecutorWhenOom(
                "PThreadPoolExecutor", PThreadThreadPoolCache.getQueueType(queue)
            ) ?: throw OutOfMemoryError(e.localizedMessage)

            freeExecutor.execute(runnable)
        }
    }

    override fun finalize() {
        super.finalize()
        shutdown()
        PThreadThreadPoolCache.removeCache(this)
    }

    fun isWorkQueueEmpty(): Boolean {
        return queue.isEmpty()
    }

    override fun setThreadFactory(threadFactory: ThreadFactory) {
        super.setThreadFactory(ThreadFactoryProxy.proxy(threadFactory, 2))

    }

    override fun shutdownNow(): List<Runnable> {
        PThreadThreadPoolCache.removeCache(this)
        return super.shutdownNow()
    }

    override fun submit(runnable: Runnable): Future<*> {
        if (isEnableHook()) {
            try {
                return super.submit(runnable)
            } catch (e: OutOfMemoryError) {
                //todo
                val findFreeExecutor: PThreadPoolExecutor? = PThreadThreadPoolCache. findFreeExecutorWhenOom(
                    "PThreadPoolExecutor", PThreadThreadPoolCache.getQueueType(queue)
                )
                val future = findFreeExecutor?.submit(runnable)
                if (future != null) {
                    return future
                }
                throw OutOfMemoryError(e.localizedMessage)
            }
        }
        return super.submit(runnable)
    }

    override fun <T> submit(callable: Callable<T>): Future<T> {
        if (isEnableHook()) {
            try {
                return super.submit(callable)
            } catch (e: OutOfMemoryError) {
                //todo
                val findFreeExecutor: PThreadPoolExecutor? = PThreadThreadPoolCache.findFreeExecutorWhenOom(
                    "PThreadPoolExecutor", PThreadThreadPoolCache.getQueueType(queue)
                )
                val future = findFreeExecutor?.submit(callable)
                if (future != null) {
                    return future
                }
                throw OutOfMemoryError(e.localizedMessage)
            }
        }
        return super.submit(callable)
    }

}