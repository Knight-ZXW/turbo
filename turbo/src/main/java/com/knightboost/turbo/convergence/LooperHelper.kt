package com.knightboost.turbo.convergence

import android.os.Looper

object LooperHelper {


    val looperLocal by lazy {
        val declaredField = Looper::class.java.getDeclaredField("sThreadLocal")
        declaredField.isAccessible = true
        val obj = declaredField.get(null)
        if (!(obj is ThreadLocal<*>)){
            return@lazy null
        }
        return@lazy obj as ThreadLocal<Looper>
    }

    fun clearLooper(){
        try {
            if (Looper.myLooper() == null){
                return
            }
            looperLocal?.set(null)
        }catch (unused:Exception){

        }

    }

}