package com.wxson.blt_rssi

import android.annotation.SuppressLint
import android.bluetooth.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import android.widget.CompoundButton
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.wxson.blt_rssi.bt.BluetoothService
import com.wxson.blt_rssi.bt.BluetoothCallback
import com.wxson.blt_rssi.util.Msg
import kotlin.concurrent.thread

@SuppressLint("MissingPermission")
class MainViewModel : ViewModel() {
    private val tag = this.javaClass.simpleName
    val bluetoothAdapter: BluetoothAdapter = (MyApplication.context
        .getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager)
        .adapter
    private val deviceList: MutableList<BluetoothDevice> = ArrayList()
    val deviceAdapter = DeviceAdapter(deviceList, this::connectGattAction)
    private val bluetoothReceiver : BroadcastReceiver

    val toggleBtnRssiOnCheckedChangeListener = CompoundButton.OnCheckedChangeListener {
            _, isChecked ->
        if (isChecked) {
            BluetoothService.startReadRssiCoroutine()
        } else {
            BluetoothService.stopReadRssiCoroutine()
        }
    }

    private inner class BluetoothReceiver : BroadcastReceiver() {
        @SuppressLint("NotifyDataSetChanged")
        override fun onReceive(context: Context, intent: Intent) {
            Log.i(tag, "onReceive() intent=${intent.action}")
            when (intent.action) {
                BluetoothCallback.ON_GATT_FAILED ->{
                    _msgLiveData.value = Msg("showMsg", "ON_GATT_FAILED")
                }
                BluetoothCallback.ON_CONNECTED -> {
                    // set item color red
                    deviceAdapter.notifyDataSetChanged()
                    // set connection flag connected
                    _msgLiveData.value = Msg("connectStatus", true)
                }
                BluetoothCallback.ON_DISCONNECTED -> {
                    // set item color black
                    deviceAdapter.currentPosition = -1
                    deviceAdapter.notifyDataSetChanged()
                    // set connection flag disconnected
                    _msgLiveData.value = Msg("connectStatus", false)
                }
                BluetoothCallback.ON_READ_RSSI_SUCCESS -> {
                    // get rssi value to display
                    val rssi = intent.extras?.getInt("rssi", 0)
                    // display rssi
                    _msgLiveData.value = Msg("rssi", rssi)
                }
            }
        }
    }

    val msgLiveData: LiveData<Msg>
        get() = _msgLiveData
    private val _msgLiveData = MutableLiveData<Msg>()

    init {
        Log.i(tag, "init")
        // set BroadcastReceiver
        val filter = IntentFilter().apply {
            addAction(BluetoothCallback.ON_CONNECTED)
            addAction(BluetoothCallback.ON_DISCONNECTED)
            addAction(BluetoothCallback.ON_READ_RSSI_SUCCESS)
        }
        bluetoothReceiver = BluetoothReceiver()
        MyApplication.context.registerReceiver(bluetoothReceiver, filter)
        // get bonded bluetooth devices
        getBondedDevices()
        _msgLiveData.value = Msg("connectState", false)
    }

    private fun connectGattAction(device: BluetoothDevice) {
        if (BluetoothService.isConnected)
            BluetoothService.disconnectGatt()
        else
            BluetoothService.connectGatt(device, BluetoothCallback())
    }

    private fun getBondedDevices() {
        deviceList.apply {
            if (isNotEmpty()) clear()
            for (device in bluetoothAdapter.bondedDevices) {
                add(device)
            }
        }
    }
}