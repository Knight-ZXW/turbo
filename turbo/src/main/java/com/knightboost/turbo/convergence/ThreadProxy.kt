package com.knightboost.turbo.convergence

import com.knightboost.turbo.proxy.plugin.PluginPthreadThread

class ThreadProxy(val delegate: Thread) : Thread() {

    var attachThread: Thread? = null
    var isStarted = false

    override fun interrupt() {
        val thread = attachThread
        thread?.interrupt()
    }

    override fun isInterrupted(): Boolean {
        val thread = attachThread
        return thread?.isInterrupted ?: true
    }

    override fun start() {
        if (this.isStarted){
            return
        }
        isStarted = true
        SuperThreadPoolExecutor.execute({
            val r3 = Thread.currentThread()
            var r5 = ""
            val r2 = r3.name

            if (delegate is PluginPthreadThread){
                val sb = StringBuilder()


            }else{

            }



        },priority)




    }

}