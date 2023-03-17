package com.knightboost.turbo.demo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.SystemClock
import android.util.Log

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onResume() {
        super.onResume()
        val t = Thread({})
        val t1 = SystemClock.elapsedRealtimeNanos()
        t.start()
        val t2 = SystemClock.elapsedRealtimeNanos()
        val t3 = SystemClock.elapsedRealtimeNanos()
        val t4 = SystemClock.elapsedRealtimeNanos()
        Log.e("zxw","cost1 =${t2-t1}  ,cost2 = ${t4-t3}" )


    }
}