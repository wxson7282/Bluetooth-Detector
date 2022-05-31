package com.wxson.blt_rssi.bt

import android.app.IntentService
import android.content.Intent
import android.content.Context

private const val ACTION_READ = "com.wxson.blt_rssi.bt.action.READ"

class ReadRssiIntentService : IntentService("ReadRssiIntentService") {

    @Deprecated("Deprecated in Java")
    override fun onHandleIntent(intent: Intent?) {
        when (intent?.action) {
            ACTION_READ -> {
                handleActionRead()
            }
        }
    }

    private fun handleActionRead() {
        while (runningFlag) {
            BluetoothService.readRssi()
            Thread.sleep(500)
        }
    }

    companion object {
        private var runningFlag = false
        @JvmStatic
        fun startActionRead(context: Context) {
            runningFlag = true
            val intent = Intent(context, ReadRssiIntentService::class.java).apply {
                action = ACTION_READ
            }
            context.startService(intent)
        }
        @JvmStatic
        fun stopActionRead() {
            runningFlag = false
        }
    }
}