package com.knightboost.turbo.core

import java.util.concurrent.*

abstract class TurboThreadMonitor {
    open fun onCoreThreadPoolCallExecute(runnable: Runnable, i: Int) {}

    open fun onCoreThreadPoolWorkerExecute(runnable: Runnable, z: Boolean) {}

    open fun onHandlerThreadCreate(name: String, i: Int) {}

    open fun onScheduledThreadPoolCreate(i: Int, threadFactory: ThreadFactory,
        rejectedExecutionHandler: RejectedExecutionHandler) {}

    open fun onThreadPoolCallExecute(runnable: Runnable, i: Int) {}

    open fun onThreadPoolCreate(corePoolSIze: Int, maximumPoolSize: Int, keepAliveTime: Long,
        timeUnit: TimeUnit, blockingQueue: BlockingQueue<Runnable>,
        threadFactory: ThreadFactory, rejectedExecutionHandler: RejectedExecutionHandler) {}

    open fun onThreadStart(thread: Thread) {}
}