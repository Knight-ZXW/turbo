package com.knightboost.turbo.proxy.plugin

import com.knightboost.turbo.convergence.PluginThreadFactoryProxy
import com.knightboost.turbo.proxy.PThreadPoolExecutor
import java.util.concurrent.*

class PluginPThreadPoolExecutor : PThreadPoolExecutor {
    constructor(
        corePoolSize: Int, maximumPoolSize: Int, keepAliveTime: Long, unit: TimeUnit, workQueue: BlockingQueue<Runnable>
    ) : super(
        corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, PluginThreadFactoryProxy.proxy(Executors.defaultThreadFactory(), 2)
    )

    constructor(
        corePoolSize: Int, maximumPoolSize: Int, keepAliveTime: Long, unit: TimeUnit, workQueue: BlockingQueue<Runnable>, threadFactory: ThreadFactory
    ) : super(
        corePoolSize,
        maximumPoolSize,
        keepAliveTime,
        unit,
        workQueue,
        PluginThreadFactoryProxy.proxy(threadFactory, 2),

        )

    constructor(
        corePoolSize: Int, maximumPoolSize: Int, keepAliveTime: Long, unit: TimeUnit, workQueue: BlockingQueue<Runnable>, handler: RejectedExecutionHandler
    ) : super(
        corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, PluginThreadFactoryProxy.proxy(Executors.defaultThreadFactory(), 2), handler
    )

    constructor(
        corePoolSize: Int, maximumPoolSize: Int, keepAliveTime: Long, unit: TimeUnit, workQueue: BlockingQueue<Runnable>, threadFactory: ThreadFactory, handler: RejectedExecutionHandler
    ) : super(
        corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, PluginThreadFactoryProxy.proxy(threadFactory, 2), handler
    )

}