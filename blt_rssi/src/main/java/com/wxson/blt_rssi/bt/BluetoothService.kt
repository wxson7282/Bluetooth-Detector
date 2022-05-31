package com.wxson.blt_rssi.bt

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.util.Log
import com.wxson.blt_rssi.MyApplication

@SuppressLint("MissingPermission")
object BluetoothService {
    private val tag = this.javaClass.simpleName
    private var isConnected: Boolean = false
    private lateinit var bluetoothGatt: BluetoothGatt

    fun connectGatt(device: BluetoothDevice, callback: BluetoothCallback) {
        Log.i(tag, "connectGatt()")
        if (!isConnected) {
            isConnected = true
            MyApplication.runOnUiThread {
                bluetoothGatt = device.connectGatt(MyApplication.context, false, callback)
            }
        }
    }

    fun connectFinished() {
        Log.i(tag, "connectFinished()")
    }

    fun readRssi() {
        if (this::bluetoothGatt.isInitialized)
            bluetoothGatt.readRemoteRssi()
    }

}