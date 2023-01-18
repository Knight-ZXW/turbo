package com.knightboost.turbo.proxy;

import android.os.HandlerThread;
import android.os.Looper;

import com.knightboost.turbo.PThreadThreadPoolCache;
import com.knightboost.turbo.convergence.SuperThreadPoolManager;
import com.knightboost.turbo.convergence.ThreadProxy;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class PthreadHandlerThread extends HandlerThread {
    private boolean isStartSuccess;
    private final ReentrantLock lock;
    private final Condition condition;
    private ThreadProxy proxy;

    public PthreadHandlerThread(String name) {
        super(name);
        this.lock = new ReentrantLock();
        this.condition = this.lock.newCondition();
    }

    public PthreadHandlerThread(String name, int priority) {
        super(name, priority);
        this.lock = new ReentrantLock();
        this.condition = this.lock.newCondition();
    }

    @Override
    public Looper getLooper() {
        if (!this.isStartSuccess){
            ReentrantLock reentrantLock = this.lock;
            reentrantLock.lock();
            while (!this.isStartSuccess){
                try {
                    try {
                        this.condition.await();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }finally {
                    reentrantLock.unlock();
                }
            }

        }
        Looper looper = super.getLooper();
        return looper;
    }

    @Override
    public void interrupt() {
        ThreadProxy threadProxy = this.proxy;
        if (threadProxy!=null){
            threadProxy.interrupt();
        }else {
            super.interrupt();
        }
    }

    @Override
    public synchronized void start() {
        try {
            if (SuperThreadPoolManager.INSTANCE.isEnable(4)){
                if (this.proxy == null){
                    this.proxy = new ThreadProxy(this);
                }
                this.proxy.start();
            }else {
                super.start();
            }
            this.isStartSuccess = true;
            this.lock.lock();
        }catch (OutOfMemoryError unused){
            PThreadThreadPoolCache.INSTANCE.
        }


        super.start();
    }
}
