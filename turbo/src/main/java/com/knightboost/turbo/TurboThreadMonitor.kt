package com.knightboost.turbo

import java.util.concurrent.BlockingQueue
import java.util.concurrent.RejectedExecutionHandler
import java.util.concurrent.ThreadFactory
import java.util.concurrent.TimeUnit

abstract class TurboThreadMonitor {
    public abstract fun onCoreThreadPoolCallExecute(runnable:Runnable,i:Int)
    public abstract fun onCoreThreadPoolWorkExecute(runnable: Runnable,z:Boolean)
    public abstract fun onHandleThreadCreate(name:String,i:Int)
    public abstract fun onScheduleThreadPoolCreate(i:Int,threadFactory: ThreadFactory,rejectedExecutionHandler: RejectedExecutionHandler);
    public abstract fun onThreadPoolCallExecute(runnable: Runnable,i: Int)
    public abstract fun onThreadPollCreate(i:Int,i2:Int,j:Long,timeUnit: TimeUnit,blockingQueue: BlockingQueue<Runnable>,
    threadFactory: ThreadFactory,rejectedExecutionHandler: RejectedExecutionHandler)
    public abstract fun onThreadStart(thread: Thread)
}