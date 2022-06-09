package com.wxson.blt_rssi.bt

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import com.wxson.blt_rssi.MyApplication
import kotlinx.coroutines.delay

@SuppressLint("MissingPermission")
object BluetoothService {
    private val tag = this.javaClass.simpleName
    var isConnected = false
    private var bluetoothGatt: BluetoothGatt? = null

    fun connectGatt(device: BluetoothDevice, callback: BluetoothCallback) {
        Log.i(tag, "connectGatt()")
        if (!isConnected) {
            MyApplication.runOnUiThread {
                bluetoothGatt = device.connectGatt(MyApplication.context, false, callback)
            }
        }
    }

    fun disconnectGatt() {
        Log.i(tag, "disconnectGatt()")
        if (isConnected) {
            Thread.sleep(300)
            try {
                if (job.isActive) job.cancel()
                bluetoothGatt?.disconnect()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun connectFinished() {
        Log.i(tag, "connectFinished()")
    }

    private fun readRssi() {
            bluetoothGatt?.let{
                if (it.readRemoteRssi()) {
//                    Log.i(tag, "readRemoteRssi success")
                } else {
                    Log.i(tag, "readRemoteRssi failed")
                }
            }
    }

    private lateinit var job: Job   // background job for coroutine
    private lateinit var scope: CoroutineScope
    fun startReadRssiCoroutine() {
        job = Job()
        scope = CoroutineScope(job)
        scope.launch {
            Log.i(tag, "RssiCoroutine start")
            while (true) {
                readRssi()
                delay(500)
            }
        }
    }

    fun stopReadRssiCoroutine() {
        if (this::job.isInitialized) {
            job.cancel()
            Log.i(tag, "RssiCoroutine stop")
        }
    }

}