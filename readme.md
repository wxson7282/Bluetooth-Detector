@[TOC](获取蓝牙rssi的实例)
有一次蓝牙耳机不知滚落何方，遍寻不得，因此想到可否利用蓝牙发射信号强度rssi来寻找蓝牙设备，着手写了以下三个实例。
# 读取蓝牙rssi的基本方法
基本方法有两种，一种是通过扫描或搜索的方式获取rssi，另一种是连接以后连续读取rssi。
## 读取低功耗蓝牙rssi
用扫描低功耗蓝牙的方法，在回调中读取蓝牙rssi。

首先取得BluetoothAdapter的实例

```kotlin
    val bluetoothAdapter: BluetoothAdapter = (MyApplication.context
        .getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager)
        .adapter
```
低功耗蓝牙用扫描方法获得蓝牙设备信息，需要定义扫描器。

```kotlin
	scanner = bluetoothAdapter.bluetoothLeScanner
```
启动扫描器时，需要注入一个回调函数，在回调函数的onScanResult()中取得蓝牙设备信息以及rssi值。

```kotlin
    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            addDeviceList(MyDevice(result.device,result.rssi))
        }
    }
```
启动扫描器

```kotlin
	scanner.startScan(scanCallback)
```
其余工作把回调函数中取得的设备名称和rssi值放在deviceList里，通过recyclerView显示。
## 读取常规蓝牙rssi
获取常规蓝牙rssi的方法与低功耗蓝牙相似，具体实现略有不同。没有扫描器和回调，取而代之的时Discovery和BroadcastReceiver。

同样取得BluetoothAdapter的实例

```kotlin
    val bluetoothAdapter: BluetoothAdapter = (MyApplication.context
        .getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager)
        .adapter
```
定义一个蓝牙相关的广播接收器，用于获得蓝牙操作的结果。
当接受到BluetoothDevice.ACTION_FOUND时，从intent中读取蓝牙设备信息以及rssi值。
```kotlin
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
```
注册上述蓝牙广播接收器。
```kotlin
        val filter = IntentFilter()
        filter.addAction(BluetoothDevice.ACTION_FOUND)
        filter.addAction(ACTION_DISCOVERY_FINISHED)
        bluetoothReceiver = BluetoothReceiver()
        MyApplication.context.registerReceiver(bluetoothReceiver, filter)
```
启动蓝牙搜寻

```kotlin
        bluetoothAdapter.startDiscovery()
```
其余工作把广播接收器中取得的设备名称和rssi值放在deviceList里，通过recyclerView显示。
## 连续读取BluetoothGatt蓝牙rssi
对于已经连接的蓝牙设备，目前只找到BluetoothGatt协议支持的方法。

同样取得BluetoothAdapter的实例

```kotlin
    val bluetoothAdapter: BluetoothAdapter = (MyApplication.context
        .getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager)
        .adapter
```
同样需要一个回调，用于处理连接过程的各种事件。

```kotlin
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
```

首先与指定的蓝牙设备连接，上述回调是连接必须的参数。

```kotlin
    fun connectGatt(device: BluetoothDevice, callback: BluetoothCallback) {
        Log.i(tag, "connectGatt()")
        if (!isConnected) {
            MyApplication.runOnUiThread {
                bluetoothGatt = device.connectGatt(MyApplication.context, false, callback)
            }
        }
    }
```
定义一个蓝牙相关的广播接收器，用于获得蓝牙操作的结果。
```kotlin
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
```
连接成功以后，启动连续读取rssi线程或协程，这里用的是协程。

```kotlin
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

    private fun readRssi() {
            bluetoothGatt?.let{
                if (it.readRemoteRssi()) {
                    Log.i(tag, "readRemoteRssi success")
                } else {
                    Log.i(tag, "readRemoteRssi failed")
                }
            }
    }

```
如果读取成功，会在广播接收器中收到BluetoothCallback.ON_READ_RSSI_SUCCESS消息，此时可以从intent中读取rssi值。
三个实例的源码都在github上，[请参考 https://github.com/wxson7282/Bluetooth-Detector](https://github.com/wxson7282/Bluetooth-Detector)。
欢迎交流。
