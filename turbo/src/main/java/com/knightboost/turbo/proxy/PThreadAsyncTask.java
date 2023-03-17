package com.knightboost.turbo.proxy;

import android.os.AsyncTask;

import com.knightboost.turbo.rename.base.DefaultThreadFactory;

import java.util.concurrent.Executor;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;

public abstract class PThreadAsyncTask<Params,Progress,Result> extends AsyncTask<Params,Progress,Result>{

    private static final Executor THREAD_POOL_EXECUTOR = new PThreadPoolExecutor(0,20,
            3, TimeUnit.MILLISECONDS,
            new SynchronousQueue<>(),new DefaultThreadFactory("PThreadAsyncTask"));


    public static void execute(Runnable runnable){
        THREAD_POOL_EXECUTOR.execute(runnable);
    }

    /**
     * TODO 使用插桩 将所有 execute 函数调用替换为 execute1
     * @param params  The parameters of the task.
     * @return This instance of AsyncTask.
     */
    public AsyncTask<Params, Progress, Result> execute1(Params... params) {
        return executeOnExecutor(THREAD_POOL_EXECUTOR,params);
    }


}
