package com.knightboost.turbo.proxy

import com.knightboost.turbo.PThreadThreadPoolCache
import com.knightboost.turbo.convergence.ThreadFactoryProxy.Companion.proxy
import com.knightboost.turbo.PthreadUtil.isEnableHook
import com.knightboost.turbo.convergence.SuperThreadPoolManager.enableBlockFetchStack
import com.knightboost.turbo.convergence.SuperThreadPoolManager.defaultScheduledThreadKeepAliveTime
import com.knightboost.turbo.convergence.ThreadFactoryProxy
import com.knightboost.turbo.PthreadUtil
import com.knightboost.turbo.convergence.SuperThreadPoolExecutor
import com.knightboost.turbo.convergence.SuperThreadPoolManager
import java.util.concurrent.*

open class PThreadScheduledThreadPoolExecutor : ScheduledThreadPoolExecutor {

    private val runnableMap = mutableMapOf<Runnable, RunnableScheduledFuture<*>>()

    constructor(corePoolSize: Int) : super(
        corePoolSize, proxy(Executors.defaultThreadFactory(), 8)
    ) {
        checkHook()
    }

    constructor(corePoolSize: Int, threadFactory: ThreadFactory?) : super(
        corePoolSize, proxy(
            Executors.defaultThreadFactory(), 8
        )
    ) {
    }

    constructor(corePoolSize: Int, handler: RejectedExecutionHandler?) : super(
        corePoolSize, proxy(
            Executors.defaultThreadFactory(), 8
        ), handler
    ) {
        checkHook()
    }

    constructor(
        corePoolSize: Int, threadFactory: ThreadFactory?, handler: RejectedExecutionHandler?
    ) : super(
        corePoolSize, proxy(
            Executors.defaultThreadFactory(), 8
        ), handler
    ) {
        checkHook()
    }

    private fun checkHook() {
        if (isEnableHook()) {
            if (!enableBlockFetchStack) {
                super.setKeepAliveTime(
                    defaultScheduledThreadKeepAliveTime.toLong(), TimeUnit.SECONDS
                )
                //TODO lancetCall This
                allowCoreThreadTimeOut(true)
            } else if (allowsCoreThreadTimeOut()) {
            } else {
                super.setKeepAliveTime(
                    Math.max(30L, getKeepAliveTime(TimeUnit.SECONDS)), TimeUnit.SECONDS
                )
                //todo lancetCall this
                allowCoreThreadTimeOut(true)
            }
        }
    }

    private fun <T> retryOOMLogicWithReturn(
        obj: Any,
        function: () -> ScheduledFuture<out T>?
    ): ScheduledFuture<out T>? {
        if (!isEnableHook()) {
            return function()
        }
        try {
            val future = function()
            this.runnableMap.remove(obj)
            return future
        } catch (unused: OutOfMemoryError) {
            PThreadThreadPoolCache.workPool.schedule(
                {
                    function()
                    runnableMap.remove(obj)
                }, PthreadUtil.delayTime, TimeUnit.MILLISECONDS
            )
            return runnableMap[obj] as ScheduledFuture<T>?
        }
    }

    override fun allowCoreThreadTimeOut(value: Boolean) {
        try {
            var allow = true
            if (enableBlockFetchStack && !value){
                allow =false
            }
            super.allowCoreThreadTimeOut(allow)
        }catch (unused:Throwable){
        }
    }

    override fun <V> decorateTask(
        runnable: Runnable,
        task: RunnableScheduledFuture<V>
    ): RunnableScheduledFuture<V> {

        if (!isEnableHook()){
            return super.decorateTask(runnable, task)
        }
        var runnableScheduledFuture:RunnableScheduledFuture<V>? = runnableMap[runnable] as RunnableScheduledFuture<V>?
        if (runnableScheduledFuture!=null){
            return runnableScheduledFuture
        }
        runnableScheduledFuture = super.decorateTask(runnable, task)
        runnableMap[runnable] = runnableScheduledFuture
        return runnableScheduledFuture

    }


    override fun schedule(command: Runnable, delay: Long, unit: TimeUnit): ScheduledFuture<*>? {
        return retryOOMLogicWithReturn(command) { super.schedule(command, delay, unit)
        }
    }



    override fun <V : Any> schedule(callable: Callable<V>, delay: Long, unit: TimeUnit): ScheduledFuture<V> {
        return  retryOOMLogicWithReturn<Any>(callable){
            super.schedule(callable,delay,unit)
        } as ScheduledFuture<V>
    }

    override fun scheduleAtFixedRate(command: Runnable, initialDelay: Long, period: Long, unit: TimeUnit): ScheduledFuture<*>? {
        return retryOOMLogicWithReturn(command){
            super.scheduleAtFixedRate(command, initialDelay, period, unit)
        }
    }

    override fun scheduleWithFixedDelay(command: Runnable, initialDelay: Long, delay: Long, unit: TimeUnit): ScheduledFuture<*>? {
        return retryOOMLogicWithReturn(command){
            super.scheduleWithFixedDelay(command, initialDelay, delay, unit)
        }

    }









}