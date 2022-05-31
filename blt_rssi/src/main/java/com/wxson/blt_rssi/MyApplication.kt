package com.wxson.blt_rssi

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.os.Handler
import android.os.Looper

class MyApplication: Application() {
    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var context: Context
        private val mainThreadHandler = Handler(Looper.getMainLooper())
        fun runOnUiThread(runnable: Runnable) = mainThreadHandler.post(runnable)
    }
    override fun onCreate() {
        super.onCreate()
        context = applicationContext
    }
}