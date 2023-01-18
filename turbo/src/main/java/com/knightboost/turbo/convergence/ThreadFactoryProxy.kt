package com.knightboost.turbo.convergence

import java.util.concurrent.ThreadFactory

open class ThreadFactoryProxy(val delegate: ThreadFactory, val enableType: Int) : ThreadFactory {

    companion object {

        @JvmStatic
        fun proxy(threadFactory: ThreadFactory?, enableType: Int): ThreadFactory? {
            if (threadFactory == null || threadFactory is ThreadFactoryProxy) {
                return threadFactory
            } else {
                return ThreadFactoryProxy(threadFactory, enableType)
            }
        }
    }


    fun getDelegateType(): String {
        return this.delegate.javaClass.name
    }

    open fun isProxyEnable(): Boolean {
        return SuperThreadPoolManager.isEnable(this.enableType)

    }

    override fun newThread(r: Runnable?): Thread {
        val newThread = delegate.newThread(r)
        if (SuperThreadPoolManager.isEnable(this.enableType)) {
            return ThreadProxy(newThread)
        } else {
            return newThread
        }
    }

}