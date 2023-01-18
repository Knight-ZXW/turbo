package com.knightboost.turbo.convergence

import java.util.concurrent.*

class DefaultThreadPoolExecutor : IExecutor {
    val executor: ThreadPoolExecutor = ThreadPoolExecutor(
        0,
        Integer.MAX_VALUE,
        0,
        TimeUnit.MILLISECONDS,
        SynchronousQueue(),
        Executors.defaultThreadFactory()
    )

    override fun execute(runnable: Runnable, priority: Int) {
        this.executor.execute(runnable)
    }
}