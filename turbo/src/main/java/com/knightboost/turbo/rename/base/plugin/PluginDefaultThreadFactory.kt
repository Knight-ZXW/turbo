package com.knightboost.turbo.rename.base.plugin

import com.knightboost.turbo.proxy.plugin.PluginPthreadThread
import com.knightboost.turbo.rename.base.DefaultThreadFactory

class PluginDefaultThreadFactory(prefix: String) : DefaultThreadFactory(prefix) {

    override fun createThread(threadGroup: ThreadGroup?, runnable: Runnable, name: String, stackSize: Long): Thread {
        return PluginPthreadThread(threadGroup,runnable, name,stackSize)
    }
}