package com.knightboost.turbo.convergence

import android.os.HandlerThread
import java.util.concurrent.atomic.AtomicInteger
import java.util.logging.Handler

class DredgeHandler {

    public var handler:Handler? = null
    var defaultHandlerThread:HandlerThread? =null

    @Volatile
    var enable:Boolean = false

    @Volatile
    var mHandler:android.os.Handler? = null

    var interval = 100


    val dredgeWorker = DredgeWorker()

    val dredgeState = AtomicInteger()



    class DredgeWorker:Runnable{
        override fun run() {


        }

    }
}