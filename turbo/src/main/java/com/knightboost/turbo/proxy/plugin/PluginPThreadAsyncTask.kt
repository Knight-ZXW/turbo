package com.knightboost.turbo.proxy.plugin

import android.os.AsyncTask
import com.knightboost.turbo.proxy.PThreadAsyncTask
import com.knightboost.turbo.rename.base.plugin.PluginDefaultThreadFactory
import java.util.concurrent.*

abstract class PluginPThreadAsyncTask<Params,Progress,Result> : PThreadAsyncTask<Params, Progress, Result>() {
    var THREAD_POOL_EXECUTOR: Executor = PluginPThreadPoolExecutor(
        0,
        20,
        3,
        TimeUnit.MILLISECONDS,
        SynchronousQueue(),
        PluginDefaultThreadFactory("PluginPThreadAsyncTask")
    )

    companion object{
//        @JvmStatic
//        fun execute(runnable:Runnable){
//            THREAD_POOL_EXECUTOR.execute(runnable)
//        }
    }

    override fun execute1(vararg params: Params): AsyncTask<Params, Progress, Result> {
        return executeOnExecutor(THREAD_POOL_EXECUTOR, *params)
    }



}