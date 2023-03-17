package com.knightboost.turbo.convergence

import android.os.Handler
import android.os.HandlerThread
import java.util.concurrent.atomic.AtomicInteger

class DredgeHandler {

    private var mDefaultHandlerThread: HandlerThread? = null

    private var mDefaultHandler: Handler? = null


    @Volatile
    var enable: Boolean = false
        set(value) {
            field = value
            if (value) {
                return
            }else{
                releaseDefaultHandler()
            }

        }

    var interval = 100L

    val dredgeWorker = DredgeWorker()

    val dredgeState = AtomicInteger()

    inner class DredgeWorker : Runnable {
        override fun run() {
            if (dredgeState.compareAndSet(2, 1)) {
                if (SuperThreadPoolExecutor.prepare()) {
                    this@DredgeHandler.postDredgeWork(false)
                }else{
                    this@DredgeHandler.dredgeState.set(0)
                }
            }else if (dredgeState.get()!=1){
                throw RuntimeException("undesired state: ${dredgeState}")
            }else if (SuperThreadPoolExecutor.dredgeTask()){ //循环
                postDredgeWork(false)
            } else{
                this@DredgeHandler.dredgeState.set(0)
            }
        }

    }

    fun postDredgeWork(boolean: Boolean) {
        mDefaultHandler?.postDelayed(this.dredgeWorker, this.interval)
    }

    private fun getHandlerThread(): HandlerThread {
        if (mDefaultHandlerThread == null) {
            synchronized(this) {
                if (mDefaultHandlerThread == null) {
                    val thread = HandlerThread("DredgeHandler")
                    thread.start()
                    mDefaultHandlerThread = thread
                }
            }
        }
        return mDefaultHandlerThread!!
    }

    private fun getHandler(): Handler? {
        if (mDefaultHandler == null) {
            synchronized(this) {
                if (mDefaultHandler == null) {
                    val createHandler = Handler(getHandlerThread().looper)
                    mDefaultHandler = createHandler
                }
            }
        }
        return mDefaultHandler
    }

    fun requestDredgePrepare() {
        if (this.enable && this.dredgeState.compareAndSet(0, 2)) {
            postDredgeWork(true)
        }
    }

    fun releaseDefaultHandler() {
        synchronized(this) {
            val handlerThread = mDefaultHandlerThread
            handlerThread?.quitSafely()
            this.mDefaultHandlerThread = null
            this.mDefaultHandler = null
        }
    }
}