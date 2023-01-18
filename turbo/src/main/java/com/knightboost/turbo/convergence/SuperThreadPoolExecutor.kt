package com.knightboost.turbo.convergence

object SuperThreadPoolExecutor {
    val mExecutor = DefaultThreadPoolExecutor()

    fun execute(runnable: Runnable,priority:Int){
        mExecutor.execute(runnable,priority)

    }

}