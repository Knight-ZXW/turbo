package com.knightboost.turbo.core;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class TurboNamedThreadFactory implements ThreadFactory {
    public final ThreadGroup group;
    public final String namePrefix;
    public final AtomicInteger threadNumber = new AtomicInteger(1);

    public TurboNamedThreadFactory(String namePrefix) {
        SecurityManager securityManager = System.getSecurityManager();
        this.group = securityManager != null ? securityManager.getThreadGroup() : Thread.currentThread().getThreadGroup();
        this.namePrefix = namePrefix;
    }

    @Override
    public Thread newThread(Runnable r) {
        Thread thread = new Thread(group, r, this.namePrefix + "#" + this.threadNumber.getAndIncrement());

        if (thread.isDaemon()) {
            thread.setDaemon(false);
        }

        if (thread.getPriority() != 5) {
            thread.setPriority(5);
        }
        return thread;
    }
}
