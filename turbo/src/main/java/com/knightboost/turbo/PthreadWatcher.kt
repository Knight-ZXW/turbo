package com.knightboost.turbo

import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

object PthreadWatcher {

    //TODO 默认值取多少？
    class WaitThreadInfo(var sleepCount: Int = 1) {

        fun increase() {
            sleepCount++
        }

        fun reset() {
            sleepCount = 1
        }
    }

    var delayTime = 12000L
    var maxTimeCount = 5

    var maxThreadCount = Runtime.getRuntime().availableProcessors() * 2;
    val sleepThreadMap = linkedMapOf<String, WaitThreadInfo>()
    val hasReportSickThreadList = mutableListOf<String>()
    var scheduledFuture: ScheduledFuture<String>? = null

     var emptyThreadListener : ((String, String, Int) -> Unit)? =null




    //todo 实现 printThread
    fun printThread() {

    }

    fun startWatch(delayTime: Long,
        maxTimeCount: Int,
        maxThreadCount: Int,
        emptyThreadListener: ((String, String, Int) -> Unit),) {
        this.delayTime = delayTime
        this.maxTimeCount = maxTimeCount
        this.maxThreadCount = maxThreadCount
        this.emptyThreadListener = emptyThreadListener
        scheduledFuture?.cancel(true)
        PThreadThreadPoolCache.workPool.scheduleWithFixedDelay({
            printThread()
        }, delayTime, delayTime, TimeUnit.MINUTES)

    }

}