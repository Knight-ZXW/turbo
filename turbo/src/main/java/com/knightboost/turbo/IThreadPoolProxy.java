package com.knightboost.turbo;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

/**
 * Proxy class for ThreadPoolExecutor
 */
public interface IThreadPoolProxy {
    void allowCoreThreadTimeOut(boolean value);

    boolean allowsCoreThreadTimeOut();

    boolean awaitTermination(long timeout, TimeUnit timeUnit) throws InterruptedException ;

    void execute(Runnable runnable);

    int getActiveCount();

    long getCompletedTaskCount();

    int getCorePoolSize();

    long getKeepAliveTime(TimeUnit timeUnit);

    int getLargestPoolSize();

    int getMaximumPoolSize();

    int getPoolSize();

    BlockingQueue<Runnable> getQueue();

    RejectedExecutionHandler getRejectedExecutionHandler();

    long getTaskCount();

    ThreadFactory getThreadFactory();

    boolean isShutdown();

    boolean isTerminated();

    boolean isTerminating();

    int prestartAllCoreThreads();

    boolean prestartCoreThread();

    void purge();

    boolean remove(Runnable runnable);

    void setCorePoolSize(int i);

    void setKeepAliveTime(long j, TimeUnit timeUnit);

    void setMaximumPoolSize(int i);

    void setRejectedExecutionHandler(RejectedExecutionHandler rejectedExecutionHandler);

    void setThreadFactory(ThreadFactory threadFactory);

    void shutdown();

    List<Runnable> shutdownNow();

    String toString();
}
