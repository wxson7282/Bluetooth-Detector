package com.wxson.blt_rssi

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.permissionx.guolindev.PermissionX
import com.wxson.blt_rssi.databinding.ActivityMainBinding
import com.wxson.blt_rssi.util.SpacesItemDecoration

class MainActivity : AppCompatActivity() {

    private lateinit var viewModel: MainViewModel

    private val resultLauncher = registerForActivityResult(
        ActivityResultContracts
        .StartActivityForResult()) {
        if (it.resultCode != Activity.RESULT_OK) {  //if the user has rejected the request
            this.finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        requestBluetoothPermission()
        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)
        if (!viewModel.bluetoothAdapter.isEnabled) {    // 蓝牙未打开
            // 转移到系统蓝牙开启activity
            resultLauncher.launch(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
        }
        // set button listener
        binding.toggleBtnRssi.setOnCheckedChangeListener(viewModel.toggleBtnRssiOnCheckedChangeListener)
        // set adapter
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            addItemDecoration(SpacesItemDecoration(10))     // 设置item间距
            adapter = viewModel.deviceAdapter
        }
        // liveData observer
        viewModel.msgLiveData.observe(this) { msg ->
            when (msg.type) {
                "showMsg" -> showMsg(msg.obj as String)
                "connectStatus" -> {
                    val isConnected = msg.obj as Boolean
                    binding.toggleBtnRssi.isEnabled = isConnected
                    binding.ivConnectState.setImageDrawable(
                        ResourcesCompat.getDrawable(resources, if (isConnected) R.drawable.ic_connected else R.drawable.ic_disconnected, this.theme)
                    )
                }
                "rssi" -> {
                    binding.txtRssi.text = String.format(MyApplication.context.resources.getString(R.string.rssi_format), msg.obj as Int)
                }
            }
        }
    }

    // 申请权限
    private fun requestBluetoothPermission() {
        val requestList = ArrayList<String>()
        requestList.apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {     // version >= api31(android12)
                add(Manifest.permission.BLUETOOTH_SCAN)
                add(Manifest.permission.BLUETOOTH_ADVERTISE)
                add(Manifest.permission.BLUETOOTH_CONNECT)
            } else {
                add(Manifest.permission.ACCESS_COARSE_LOCATION)
            }
        }

        if (requestList.isNotEmpty()) {
            PermissionX.init(this)
                .permissions(requestList)
                .explainReasonBeforeRequest()
                .onExplainRequestReason {scope, deniedList ->
                    val message = "PermissionX需要您同意以下权限才能正常使用"
                    scope.showRequestReasonDialog(deniedList, message, "允许", "拒绝")
                }
                .request { allGranted, _, deniedList ->
                    if (allGranted) {
                        Toast.makeText(this, "所有申请的权限都已通过", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "您拒绝了如下权限：$deniedList", Toast.LENGTH_SHORT).show()
                        if (!this.isFinishing) {
                            this.finish()
                        }
                    }
                }
        }
    }

    private fun showMsg(msg: String){
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
    }
}