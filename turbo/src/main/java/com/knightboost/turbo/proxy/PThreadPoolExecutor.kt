package com.knightboost.turbo.proxy

import android.os.Build
import com.knightboost.turbo.*
import com.knightboost.turbo.PthreadUtil.isEnableHook
import com.knightboost.turbo.convergence.ThreadFactoryProxy
import java.util.concurrent.*

open class PThreadPoolExecutor : DefaultThreadPoolProxy {

    constructor(corePoolSize: Int, maximumPoolSize: Int, keepAliveTime: Long, unit: TimeUnit, workQueue: BlockingQueue<Runnable>) : super(
        corePoolSize,
        maximumPoolSize,
        keepAliveTime,
        unit,
        workQueue
    ) {
        if (isEnableHook()) {
            if (!allowsCoreThreadTimeOut()) {
                setKeepAliveTime(30L.coerceAtLeast(getKeepAliveTime(TimeUnit.SECONDS)), TimeUnit.SECONDS)
                try {
                    if (Build.VERSION.SDK_INT <= 23) {
                        try {
                            allowCoreThreadTimeOut(true)
                        } catch (_: Throwable) {
                        }

                    }
                } catch (e2: Exception) {
                    if (e2 is IllegalArgumentException) {
                        //TODO  should never happen
                        return
                    }
                    throw e2
                }
            }
            PThreadThreadPoolCache.addCache(this)
        }
    }

    constructor(corePoolSize: Int, maximumPoolSize: Int, keepAliveTime: Long, unit: TimeUnit, workQueue: BlockingQueue<Runnable>, threadFactory: ThreadFactory) : super(
        corePoolSize,
        maximumPoolSize,
        keepAliveTime,
        unit,
        workQueue,
        threadFactory
    ) {
    }

    constructor(corePoolSize: Int, maximumPoolSize: Int, keepAliveTime: Long, unit: TimeUnit, workQueue: BlockingQueue<Runnable>, handler: RejectedExecutionHandler) : super(
        corePoolSize,
        maximumPoolSize,
        keepAliveTime,
        unit,
        workQueue,
        handler
    ) {
    }

    constructor(corePoolSize: Int, maximumPoolSize: Int, keepAliveTime: Long, unit: TimeUnit,
        workQueue: BlockingQueue<Runnable>, threadFactory: ThreadFactory, handler: RejectedExecutionHandler) : super(
        corePoolSize,
        maximumPoolSize,
        keepAliveTime,
        unit,
        workQueue,
        threadFactory,
        handler
    ) {
    }

    //TODO 这个函数应该被 lancet 插桩处理
    override fun allowCoreThreadTimeOut(value: Boolean) {
        try {
            if (Build.VERSION.SDK_INT > 23) {
                super.allowCoreThreadTimeOut(value)
                return
            }

            try {
                super.allowCoreThreadTimeOut(value)
            } catch (e: Exception) {
                if (e !is java.lang.ClassCastException) {
                    throw  e
                }

            }

        } catch (e: Exception) {
            if (e !is IllegalArgumentException) {
                throw e
            }
        }
    }

    override fun execute(runnable: Runnable) {

        if (!PthreadUtil.isEnableHook()) {
            super.execute(runnable)
            return
        }

        try {
            super.execute(runnable)
        } catch (e: OutOfMemoryError) {
            val freeExecutor = PThreadThreadPoolCache.findFreeExecutor("PThreadPoolExecutor", PThreadThreadPoolCache.getQueueType(queue))
                ?: throw OutOfMemoryError(e.localizedMessage)

            freeExecutor.execute(runnable)
        }
    }

    override fun finalize() {
        super.finalize()
        //TODO 这是做什么？
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

    override fun  submit(runnable: Runnable): Future<*> {
        if (isEnableHook()) {
            try {
                return super.submit(runnable)
            } catch (e: OutOfMemoryError) {
                //todo
                val findFreeExecutor: PThreadPoolExecutor? = PThreadThreadPoolCache.findFreeExecutor(
                    "PThreadPoolExecutor",
                    PThreadThreadPoolCache.getQueueType(queue)
                )
                val future = findFreeExecutor?.submit(runnable)
                if (future!=null){
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
                val findFreeExecutor: PThreadPoolExecutor? = PThreadThreadPoolCache.findFreeExecutor(
                    "PThreadPoolExecutor",
                    PThreadThreadPoolCache.getQueueType(queue)
                )
                val future = findFreeExecutor?.submit(callable)
                if (future!=null){
                    return future
                }
                throw OutOfMemoryError(e.localizedMessage)
            }
        }
        return super.submit(callable)
    }





}