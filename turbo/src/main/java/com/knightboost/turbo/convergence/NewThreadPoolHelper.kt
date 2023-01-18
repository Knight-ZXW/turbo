package com.knightboost.turbo.convergence

import java.util.concurrent.*

object NewThreadPoolHelper {

    fun newThreadPool(
        corePoolSize: Int,
        maximumPoolSize: Int,
        keepAliveTime: Long,
        unit: TimeUnit?,
        workQueue: BlockingQueue<Runnable?>?,
        threadFactory:ThreadFactory?,
        handler: RejectedExecutionHandler?
    ):ThreadPoolExecutor {
        return ThreadPoolExecutor(corePoolSize,maximumPoolSize,keepAliveTime,
        unit,workQueue,threadFactory,handler)
    }


}