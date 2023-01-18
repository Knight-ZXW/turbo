package com.knightboost.turbo.convergence

import java.util.concurrent.ThreadFactory

class PluginThreadFactoryProxy(delegate: ThreadFactory, enableType: Int) : ThreadFactoryProxy(delegate, enableType) {

    companion object{
        fun proxy(threadFactory: ThreadFactory,i:Int): ThreadFactoryProxy {
            return if (threadFactory is ThreadFactoryProxy) threadFactory else PluginThreadFactoryProxy(
                threadFactory,i
            )

        }
    }

    override fun isProxyEnable(): Boolean {
        return SuperThreadPoolManager.isPluginEnable &&  super.isProxyEnable()
    }

}