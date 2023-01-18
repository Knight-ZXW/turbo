package com.knightboost.turbo.core;

import android.os.Handler;
import android.os.HandlerThread;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class TurboCoreThreadPool extends ThreadPoolExecutor {

    private final HandlerThread mSchedulerThread;
    private final Handler mSchedulerHandler;
    public TurboCoreThreadPool(int corePoolSize, int maximumPoolSize,
                               long keepAliveTime, TimeUnit unit,
                               BlockingQueue<Runnable> workQueue,
                               RejectedExecutionHandler handler) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, handler);
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


    public final void execute(Runnable runnable){
        //TODO if enable ThreadAsyncCreate
        getMSchedulerHandler().post(new Runnable() {
            @Override
            public void run() {

            }
        });
    }




    public final boolean execute(Runnable runnable,TurboThreadPoolProxy turboThreadPoolProxy){
        execute(runnable);
        return true;
    }

    //rename `in` to `to`
    public final boolean offerWorkInQueue(Runnable runnable){
        execute(runnable);
        return true;
    }


}
