package com.wxson.blt_rssi.bt

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.content.Intent
import android.util.Log
import com.wxson.blt_rssi.MyApplication
import kotlin.concurrent.thread

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
                    Log.i(tag, "readRemoteRssi success")
                } else {
                    Log.i(tag, "readRemoteRssi failed")
                }
            }
    }

    private var isThreadRunning = false
    fun startReadRssiThread() {
        isThreadRunning = true
        thread {
            Log.i(tag, "readRssiThread start")
            while (isThreadRunning) {
                readRssi()
                Thread.sleep(500)
            }
            Log.i(tag, "readRssiThread end")
        }
    }

    fun stopReadRssiThread() {
        isThreadRunning = false
    }

}