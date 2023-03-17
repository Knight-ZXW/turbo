package com.knightboost.turbo;


import com.knightboost.turbo.core.TurboThreadPoolProxy;

import java.lang.ref.SoftReference;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class DefaultThreadPoolProxy extends ThreadPoolExecutor implements IThreadPoolProxy {


    public static final RejectedExecutionHandler turboDefaultHandler =new RejectedExecutionHandler(){
        @Override
        public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
            Turbo.INSTANCE.getCoreThreadPool().execute(r);
        }
    };
    private static final RejectedExecutionHandler defaultHandler = new ThreadPoolExecutor.AbortPolicy();

    private IThreadPoolProxy realProxy;

    public DefaultThreadPoolProxy(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue) {
        this(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, Executors.defaultThreadFactory(),defaultHandler);
    }

    public DefaultThreadPoolProxy(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory) {
        this(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory,defaultHandler);
    }

    public DefaultThreadPoolProxy(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue, RejectedExecutionHandler handler) {
        this(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue,Executors.defaultThreadFactory(), handler);
    }

    public DefaultThreadPoolProxy(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory, RejectedExecutionHandler handler) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, handler);
        if (!Turbo.INSTANCE.getEnable()){
            DefaultThreadPoolExecutor defaultThreadPoolExecutor = new DefaultThreadPoolExecutor(
                    corePoolSize,maximumPoolSize,keepAliveTime,unit,workQueue,threadFactory,handler
            );
            this.realProxy = defaultThreadPoolExecutor;
            Turbo.INSTANCE.getDefaultThreadPoolCache().add(new SoftReference<>(defaultThreadPoolExecutor));
            return;
        }

        handler = (handler == defaultHandler || Turbo.INSTANCE.getDisableRejectHandler()) ? turboDefaultHandler : handler;
        this.realProxy = new TurboThreadPoolProxy(
                corePoolSize,
                maximumPoolSize,
                keepAliveTime,
                unit,
                workQueue,
                threadFactory,handler,
                this
        );
    }

    @Override
    public void allowCoreThreadTimeOut(boolean value) {
        this.realProxy.allowCoreThreadTimeOut(value);
    }

    @Override
    public boolean allowsCoreThreadTimeOut() {
        return this.realProxy.allowsCoreThreadTimeOut();
    }

    @Override
    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        return  this.realProxy.awaitTermination(timeout,unit);
    }

    @Override
    public void execute(Runnable command) {
        this.realProxy.execute(command);
    }

    @Override
    public int getActiveCount() {
        return this.realProxy.getActiveCount();
    }

    @Override
    public long getCompletedTaskCount() {
        return this.realProxy.getCompletedTaskCount();
    }

    @Override
    public boolean isShutdown() {
        return this.realProxy.isShutdown();

    }

    @Override
    public boolean isTerminating() {
        return this.realProxy.isTerminating();

    }

    @Override
    public boolean isTerminated() {
        return this.realProxy.isTerminated();

    }



    @Override
    public ThreadFactory getThreadFactory() {
        return this.realProxy.getThreadFactory();
    }

    @Override
    public RejectedExecutionHandler getRejectedExecutionHandler() {
        return this.realProxy.getRejectedExecutionHandler();
    }

    @Override
    public int getCorePoolSize() {
        return this.realProxy.getCorePoolSize();
    }

    @Override
    public long getKeepAliveTime(TimeUnit unit) {
        return this.realProxy.getKeepAliveTime(unit);
    }

    @Override
    public BlockingQueue<Runnable> getQueue() {
        return this.realProxy.getQueue();
    }

    @Override
    public int getPoolSize() {
        return this.realProxy.getPoolSize();
    }

    @Override
    public int getLargestPoolSize() {
        return this.realProxy.getLargestPoolSize();
    }

    @Override
    public int getMaximumPoolSize() {
        return this.realProxy.getMaximumPoolSize();
    }

    @Override
    public long getTaskCount() {
        return this.realProxy.getTaskCount();
    }

    @Override
    public void shutdown() {
        this.realProxy.shutdown();
    }

    @Override
    public List<Runnable> shutdownNow() {
        return this.realProxy.shutdownNow();
    }

    @Override
    public void setThreadFactory(ThreadFactory threadFactory) {
        this.realProxy.setThreadFactory(threadFactory);
    }

    @Override
    public void setRejectedExecutionHandler(RejectedExecutionHandler handler) {
        this.realProxy.setRejectedExecutionHandler(handler);
    }

    @Override
    public void setCorePoolSize(int corePoolSize) {
        this.realProxy.setCorePoolSize(corePoolSize);
    }

    @Override
    public boolean prestartCoreThread() {
        return this.realProxy.prestartCoreThread();
    }

    @Override
    public int prestartAllCoreThreads() {
        return this.realProxy.prestartAllCoreThreads();
    }

    @Override
    public void setMaximumPoolSize(int maximumPoolSize) {
        this.realProxy.setMaximumPoolSize(maximumPoolSize);
    }

    @Override
    public void setKeepAliveTime(long time, TimeUnit unit) {
        this.realProxy.setKeepAliveTime(time,unit);
    }

    @Override
    public boolean remove(Runnable task) {
        return this.realProxy.remove(task);
    }

    @Override
    public void purge() {
        this.realProxy.purge();
    }

    @Override
    public String toString() {
       return this.realProxy.toString();
    }
}
