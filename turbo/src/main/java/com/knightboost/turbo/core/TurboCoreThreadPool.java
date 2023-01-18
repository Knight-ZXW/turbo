package com.knightboost.turbo.core;

import android.os.Handler;
import android.os.HandlerThread;

import androidx.annotation.NonNull;

import com.knightboost.turbo.Turbo;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class TurboCoreThreadPool extends ThreadPoolExecutor {

    private final HandlerThread mSchedulerThread;
    private final Handler mSchedulerHandler;

    public TurboCoreThreadPool(int corePoolSize, int maximumPoolSize,
                               long keepAliveTime, TimeUnit unit,
                               BlockingQueue<Runnable> workQueue,
                               ThreadFactory threadFactory) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory);
    }{
        mSchedulerThread = new HandlerThread("elasticSchedulerThread");
        mSchedulerThread.setPriority(10);
        mSchedulerThread.start();
        mSchedulerHandler = new Handler(mSchedulerThread.getLooper());
    }

    private final Handler getMSchedulerHandler(){
        //todo support delegate
        return mSchedulerHandler;
    }


    public final void execute(@NonNull Runnable runnable){
        //TODO if enable ThreadAsyncCreate
        //线程池中 无可用的工具线程时，会在当前线程直接创建 新的线程， 而创建线程的过程在某些环境下耗时会较长
        //这里就做了优化
        if (Turbo.INSTANCE.getEnableThreadAsyncCreate()){
            Turbo.INSTANCE.getSchedulerHandler().post(new Runnable() {
                @Override
                public void run() {
                    TurboCoreThreadPool.this.execute(runnable);
                }
            });
        }else {
            super.execute(runnable);
        }

    }




    public final boolean execute(Runnable runnable,TurboThreadPoolProxy turboThreadPoolProxy,boolean z){
        execute(runnable);
        return true;
    }

    public final boolean offerWorkInQueue(Runnable runnable){
        execute(runnable);
        return true;
    }


}
