package com.wxson.blt_detector

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothAdapter.ACTION_DISCOVERY_FINISHED
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import android.widget.CompoundButton
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

@SuppressLint("MissingPermission")
class MainViewModel: ViewModel() {
    private val tag = this.javaClass.simpleName
    val bluetoothAdapter: BluetoothAdapter = (MyApplication.context
        .getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager)
        .adapter
    private val bluetoothReceiver : BroadcastReceiver

    private val deviceList = mutableListOf<MyDevice>()
    val deviceAdapter = DeviceAdapter(deviceList)

    val msgLiveData: LiveData<Msg>
        get() = _msgLiveData
    private val _msgLiveData = MutableLiveData<Msg>()

    private var btnScanIsChecked = false

    val btnScanOnCheckedChangeListener =
        CompoundButton.OnCheckedChangeListener { _, isChecked ->
            btnScanIsChecked = isChecked
            if (isChecked) {
                cleanList()
                startScan()
            }
            else stopScan()
        }

    private inner class BluetoothReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            Log.i(tag, "onReceive() intent=${intent?.action}")
            when (intent?.action) {
                BluetoothDevice.ACTION_FOUND -> {
                    //发现设备
                    val device: BluetoothDevice? =
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    val rssi: Int = (intent.extras?.getShort(BluetoothDevice.EXTRA_RSSI) ?: 0).toInt()
                    device?.let {
                        val index = indexOf(it, deviceList)
                        if (index == -1) {  // deviceList中没有这个device，则追加
                            deviceList.add(MyDevice(it, rssi))
                            deviceAdapter.notifyItemInserted(deviceList.size - 1)
                            Log.i(tag, "onReceive() ItemInserted device.name=${device.name}")
                        } else { // 如果deviceList中存在该device，则修改rssi值
                            deviceList[index].rssi = rssi
                            deviceAdapter.notifyItemChanged(index)
                            Log.i(tag, "onReceive() ItemChanged(${index}) device.name=${device.name} rssi=${rssi}")
                        }
                    }
                }
                ACTION_DISCOVERY_FINISHED -> {
                    if (btnScanIsChecked) startScan()
                }
            }
        }
    }

    init {
        Log.i(tag, "init")
        val filter = IntentFilter()
        filter.addAction(BluetoothDevice.ACTION_FOUND)
        filter.addAction(ACTION_DISCOVERY_FINISHED)
        filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
        filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED)
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED)
        bluetoothReceiver = BluetoothReceiver()
        MyApplication.context.registerReceiver(bluetoothReceiver, filter)
    }

    private fun indexOf(foundDevice: BluetoothDevice, deviceList: List<MyDevice>): Int {
        var index = 0
        for (device in deviceList) {
            if (foundDevice.address == device.device.address) return index
            index += 1
        }
        return -1
    }

    private fun startScan() {
        Log.i(tag, "startScan()")
        if (bluetoothAdapter.isDiscovering)  bluetoothAdapter.cancelDiscovery()
        bluetoothAdapter.startDiscovery()
    }

    private fun stopScan() {
        Log.i(tag, "stopScan()")
        if (bluetoothAdapter.isDiscovering)  bluetoothAdapter.cancelDiscovery()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun cleanList() {
        deviceList.clear()
        deviceAdapter.notifyDataSetChanged()
    }

}