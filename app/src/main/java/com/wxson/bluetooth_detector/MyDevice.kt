package com.wxson.bluetooth_detector

import android.bluetooth.BluetoothDevice

data class MyDevice(val device: BluetoothDevice, var rssi: Int)
