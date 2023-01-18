package com.knightboost.turbo.core;

import com.knightboost.turbo.IThreadPoolProxy;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

public class TurboThreadPoolProxy implements IThreadPoolProxy,TurboThreadCallback {



    @Override
    public void allowCoreThreadTimeOut(boolean value) {

    }

    @Override
    public boolean allowsCoreThreadTimeOut() {
        return false;
    }

    @Override
    public boolean awaitTermination(long timeout, TimeUnit timeUnit) {
        return false;
    }

    @Override
    public void execute(Runnable runnable) {

    }

    @Override
    public int getActiveCount() {
        return 0;
    }

    @Override
    public long getCompletedTaskCount() {
        return 0;
    }

    @Override
    public int getCorePoolSize() {
        return 0;
    }

    @Override
    public long getKeepAliveTime(TimeUnit timeUnit) {
        return 0;
    }

    @Override
    public int getLargestPoolSize() {
        return 0;
    }

    @Override
    public int getMaximumPoolSize() {
        return 0;
    }

    @Override
    public int getPoolSize() {
        return 0;
    }

    @Override
    public BlockingQueue<Runnable> getQueue() {
        return null;
    }

    @Override
    public RejectedExecutionHandler getRejectedExecutionHandler() {
        return null;
    }

    @Override
    public long getTaskCount() {
        return 0;
    }

    @Override
    public ThreadFactory getThreadFactory() {
        return null;
    }

    @Override
    public boolean isShutdown() {
        return false;
    }

    @Override
    public boolean isTerminated() {
        return false;
    }

    @Override
    public boolean isTerminating() {
        return false;
    }

    @Override
    public int prestartAllCoreThreads() {
        return 0;
    }

    @Override
    public boolean prestartCoreThread() {
        return false;
    }

    @Override
    public void purge() {

    }

    @Override
    public boolean remove(Runnable runnable) {
        return false;
    }

    @Override
    public void setCorePoolSize(int i) {

    }

    @Override
    public void setKeepAliveTime(long j, TimeUnit timeUnit) {

    }

    @Override
    public void setMaximumPoolSize(int i) {

    }

    @Override
    public void setRejectedExecutionHandler(RejectedExecutionHandler rejectedExecutionHandler) {

    }

    @Override
    public void setThreadFactory(ThreadFactory threadFactory) {

    }

    @Override
    public void shutdown() {

    }

    @Override
    public List<Runnable> shutdownNow() {
        return null;
    }

    @Override
    public void afterExecute(Runnable runnable) {

    }
}
