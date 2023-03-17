package com.knightboost.turbo.convergence


class ThreadProxy(private val delegate: Thread) : Thread() {

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
            val currentThread = currentThread()
            val name = currentThread.name
            try {
                val threadNameTransformer = SuperThreadPoolManager.threadNameTransformer
                val threadProxy = this@ThreadProxy
                var delegateThreadName = threadProxy.delegate.name
                threadNameTransformer?.let { delegateThreadName = it.transform(delegateThreadName) }
                currentThread.name = delegateThreadName
                threadProxy.attachThread = currentThread
                if (SuperThreadPoolManager.enablePriority && currentThread.priority !=
                    threadProxy.delegate.priority){
                    currentThread.priority = threadProxy.delegate.priority
                }
                threadProxy.delegate.run()
            }finally {
                //还原 原线程名
                currentThread.name = name
            }
        },this.delegate.priority)




    }

}