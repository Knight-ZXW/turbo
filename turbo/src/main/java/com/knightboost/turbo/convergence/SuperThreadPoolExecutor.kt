package com.knightboost.turbo.convergence

import android.os.Handler
import android.os.HandlerThread
import android.os.Looper

object SuperThreadPoolExecutor {
    private var mExecutor: IExecutor = DefaultThreadPoolExecutor()

    private var asyncExecute =true

    private val asyncExecuteHandler  by lazy {
        val worker = HandlerThread("asyncExecuteWorker")
        worker.start()
        return@lazy Handler(worker.looper)
    }


    fun execute(runnable: Runnable, priority: Int) {
        if (Looper.getMainLooper().thread == Thread.currentThread() && asyncExecute
        ){
            //异步执行
            asyncExecuteHandler.post {
                mExecutor.execute(runnable,priority)
            }
        }else{
            mExecutor.execute(runnable, priority)
        }

    }

    fun prepare(): Boolean {
        val executor = mExecutor
        if (executor is IDredgeAbility) {
            return executor.prepare()
        }
        return false
    }

    fun setExecutor(executor: IExecutor) {
        mExecutor = executor
    }

    fun dredgeTask():Boolean{
        val executor = mExecutor
        if (executor is IDredgeAbility){
            return executor.dredge()
        }
        return false

    }

}