package com.knightboost.turbo

import android.annotation.SuppressLint

object PthreadUtil {

    var delayTime = 10L
    var enableHook = true
    var enableTrimThreadWhenOom = false
    var logImpl:ILog? =null
        private set

    fun getJavaThreadCount(): Int {
        val systemThreadGroup = getSystemThreadGroup()
        val activeCount = systemThreadGroup.activeCount()
        return systemThreadGroup.enumerate(arrayOfNulls<Thread>(activeCount + (activeCount/2)))
    }

    @SuppressLint("DiscouragedPrivateApi")
    fun getSystemThreadGroup(): ThreadGroup {
        val declaredField = ThreadGroup::class.java.getDeclaredField("systemThreadGroup")
        declaredField.isAccessible = true
        val obj = declaredField.get(ThreadGroup::class.java)
        return obj as ThreadGroup
    }



    fun isEnableHook():Boolean{
        return enableHook && Turbo.enable
    }

    fun log(tag:String, message:String){
        logImpl?.d(tag,message)
    }





}