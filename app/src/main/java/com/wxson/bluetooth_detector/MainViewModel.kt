package com.wxson.bluetooth_detector

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Message
import android.util.Log
import android.widget.CompoundButton
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

@SuppressLint("NotifyDataSetChanged", "MissingPermission")
class MainViewModel : ViewModel() {

    private val tag = this.javaClass.simpleName
    val bluetoothAdapter: BluetoothAdapter = (MyApplication.context
        .getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager)
        .adapter
    private lateinit var scanner: BluetoothLeScanner
    private val deviceList = mutableListOf<MyDevice>()
    val deviceAdapter = DeviceAdapter(deviceList)
    var isScanning = false

    val msgLiveData: LiveData<Msg>
        get() = _msgLiveData
    private val _msgLiveData = MutableLiveData<Msg>()

    val btnScanOnCheckedChangeListener =
        CompoundButton.OnCheckedChangeListener { _, isChecked ->
            if (isChecked) startScan() else stopScan()
        }

    private fun startScan() {
        Log.i(tag, "startScan()")
        if (!isScanning) {
            deviceList.clear()
            deviceAdapter.notifyDataSetChanged()
            scanner.startScan(scanCallback)
            isScanning = true
        }
    }

    private fun stopScan() {
        Log.i(tag, "stopScan()")
        if (isScanning) {
            scanner.stopScan(scanCallback)
            isScanning = false
        }
    }

    fun initScanner() {
        Log.i(tag, "initScanner()")
        scanner = bluetoothAdapter.bluetoothLeScanner
    }

    //扫描结果回调
    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            addDeviceList(MyDevice(result.device,result.rssi))
        }
    }

    private fun addDeviceList(device: MyDevice) {
        Log.i(tag, "addDeviceList()")
        val index = findDeviceIndex(device, deviceList)
        if (index == -1) {  // deviceList中没有这个device
            Log.i(tag, "name: ${device.device.name}, address: ${device.device.address}")
            deviceList.add(device)
            deviceAdapter.notifyDataSetChanged()
        } else {            // deviceList中有这个device
            Log.i(tag, "name: ${device.device.name}, rssi: ${device.rssi}")
            deviceList[index].rssi = device.rssi
            deviceAdapter.notifyItemChanged(index)
        }
    }

    private fun findDeviceIndex(scanDevice: MyDevice, deviceList: List<MyDevice>): Int {
        var index = 0
        for (device in deviceList) {
            if (scanDevice.device.address.equals(device.device.address)) return index
            index += 1
        }
        return -1
    }

//    private fun hasPermission(permission: String) =
//        checkSelfPermission(MyApplication.context, permission) == PackageManager.PERMISSION_GRANTED

}