package com.wxson.blt_rssi.bt

import android.annotation.SuppressLint
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.content.Intent
import android.util.Log
import com.wxson.blt_rssi.MyApplication
import com.wxson.blt_rssi.MyApplication.Companion.runOnUiThread

@SuppressLint("MissingPermission")
class BluetoothCallback : BluetoothGattCallback()  {

    private val tag = this.javaClass.simpleName

    companion object {
        const val PACKAGE_NAME = "com.wxson.blt_rssi"
        const val ON_CONNECTED = "com.wxson.blt_rssi.action.OnConnected"
        const val ON_DISCONNECTED = "com.wxson.blt_rssi.action.OnDisconnected"
        const val ON_READ_RSSI_SUCCESS = "com.wxson.blt_rssi.action.OnReadRssiSuccess"
        const val ON_GATT_FAILED = "com.wxson.blt_rssi.action.OnGattFailed"
    }

    override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
        super.onConnectionStateChange(gatt, status, newState)
        Log.i(tag, "onConnectionStateChange()")
        if (status == BluetoothGatt.GATT_SUCCESS) {
            if (newState == BluetoothGatt.STATE_CONNECTED) {
                runOnUiThread {
                    onConnected()
                }
            } else if (newState == BluetoothGatt.STATE_DISCONNECTED) {
                gatt.close()
                runOnUiThread{
                    onDisconnected()
                }
            }
        } else {
            runOnUiThread {
                onGattFailed()
                onFinish()
            }
            gatt.close()
        }
    }

    override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
        super.onServicesDiscovered(gatt, status)
    }

    override fun onReadRemoteRssi(gatt: BluetoothGatt, rssi: Int, status: Int) {
        super.onReadRemoteRssi(gatt, rssi, status)
        Log.i(tag, "onReadRemoteRssi()")
        if (status == BluetoothGatt.GATT_SUCCESS) {
            runOnUiThread {
                onReadRssiSuccess(rssi)
                onFinish()
            }
        } else {
            runOnUiThread {
                onGattFailed()
                onFinish()
            }
        }
        gatt.close()
    }

    private fun onConnected() {
        Log.i(tag, "onConnected()")
        BluetoothService.isConnected = true
        sendMyBroadcast(intentOnConnected)
    }

    private fun onDisconnected() {
        Log.i(tag, "onDisconnected()")
        BluetoothService.isConnected = false
        sendMyBroadcast(intentOnDisconnected)
    }
    private fun onGattFailed() {
        Log.i(tag,"onGattFailed()")
        BluetoothService.isConnected = false
        sendMyBroadcast(intentOnDisconnected)
//        sendMyBroadcast(intentOnGattFailed)
        onFailure()
    }

    private fun onFailure() {
        Log.i(tag,"onFailure()")
    }

    private fun onReadRssiSuccess(rssi: Int) {
        Log.i(tag,"onReadRssiSuccess() rssi=${rssi}")
        sendMyBroadcast(intentOnReadRssiSuccess.putExtra("rssi", rssi))
    }

    private fun onFinish() {
        Log.i(tag,"onFinish()")
        BluetoothService.connectFinished()
    }

    // Notification to caller
    private val intentOnConnected = Intent(ON_CONNECTED).apply { `package` = PACKAGE_NAME }
    private val intentOnDisconnected = Intent(ON_DISCONNECTED).apply { `package` = PACKAGE_NAME }
    private val intentOnReadRssiSuccess = Intent(ON_READ_RSSI_SUCCESS).apply { `package` = PACKAGE_NAME }
    private val intentOnGattFailed = Intent(ON_GATT_FAILED).apply { `package` = PACKAGE_NAME }
    private fun sendMyBroadcast(intent: Intent) {
        MyApplication.context.sendBroadcast(intent)
    }

}