package com.knightboost.turbo.proxy.plugin;

import androidx.annotation.Nullable;

import com.knightboost.turbo.convergence.PluginThreadFactoryProxy;
import com.knightboost.turbo.proxy.PThreadScheduledThreadPoolExecutor;

import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;

public class PluginPThreadScheduledExecutor extends PThreadScheduledThreadPoolExecutor {
    public PluginPThreadScheduledExecutor(int corePoolSize) {
        super(corePoolSize);
    }

    public PluginPThreadScheduledExecutor(int corePoolSize, @Nullable ThreadFactory threadFactory) {
        super(corePoolSize, threadFactory);
    }

    public PluginPThreadScheduledExecutor(int corePoolSize, @Nullable RejectedExecutionHandler handler) {
        super(corePoolSize, handler);
    }

    public PluginPThreadScheduledExecutor(int corePoolSize, @Nullable ThreadFactory threadFactory, @Nullable RejectedExecutionHandler handler) {
        super(corePoolSize, threadFactory, handler);
    }

    @Override
    public void setThreadFactory(ThreadFactory threadFactory) {
        super.setThreadFactory(PluginThreadFactoryProxy.);
    }
}
