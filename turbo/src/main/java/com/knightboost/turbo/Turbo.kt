package com.knightboost.turbo

import android.os.Handler
import android.os.HandlerThread
import com.knightboost.turbo.core.TurboCoreThreadPool
import com.knightboost.turbo.core.TurboNamedThreadFactory
import java.lang.ref.SoftReference
import java.util.Collections
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

object Turbo {

    val defaultThreadPoolCache
    = Collections.synchronizedList(mutableListOf<SoftReference<ThreadPoolExecutor>>())

    @Volatile
    var disableRejectHandler = false
    @Volatile
    var enable = false
    @Volatile
    var enableAsyncExecute = false
    @Volatile
    var enableThreadAsyncCreate = false
    @Volatile
    var isDebug = false

    var maxThread = 150

    var keepAliveTime = 30L
        set(value) {
            field = value
            coreThreadPool.setKeepAliveTime(value,TimeUnit.SECONDS)
        }

    val mSchedulerThread: HandlerThread by lazy {
        val handlerThread = HandlerThread("ElasticSchedulerThread")
        handlerThread.priority = 10
        handlerThread.start()
        return@lazy handlerThread
    }

    val coreThreadPool by lazy {
        return@lazy TurboCoreThreadPool(Turbo.maxThread,Turbo.maxThread,
            Turbo.keepAliveTime,TimeUnit.MILLISECONDS,LinkedBlockingQueue(),
            TurboNamedThreadFactory("c")
        )
    }


    val schedulerHandler by lazy {
        return@lazy Handler(mSchedulerThread.looper)
    }

    val monitor by lazy {
        return@lazy MonitorImpl()
    }


    fun trimDefault(){
        val iterator = defaultThreadPoolCache.iterator()
        while (iterator.hasNext()){
            val executorRef = iterator.next()
            val executor = executorRef.get()
            if (executor!=null){
                executor.setKeepAliveTime(1L,TimeUnit.SECONDS)
                //TODO Lancet It
                executor.allowCoreThreadTimeOut(true)
            }
        }

    }


}