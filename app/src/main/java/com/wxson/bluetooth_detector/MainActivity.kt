package com.wxson.bluetooth_detector

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.permissionx.guolindev.PermissionX
import com.wxson.bluetooth_detector.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var viewModel: MainViewModel

    private val resultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode != Activity.RESULT_OK) {  //if the user has rejected the request
            this.finish()
        }
    }

    @SuppressLint("NotifyDataSetChanged")
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
        binding.btnScan.setOnCheckedChangeListener(viewModel.btnScanOnCheckedChangeListener)

        // set adapter
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            addItemDecoration(SpacesItemDecoration(10))     // 设置item间距
            adapter = viewModel.deviceAdapter
            adapter?.notifyDataSetChanged()
        }
        // liveData observer
        viewModel.msgLiveData.observe(this) { msg ->
            when (msg.type) {
                "showMsg" -> showMsg(msg.obj as String, true)
            }
        }

        viewModel.initScanner()
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
                add(Manifest.permission.ACCESS_FINE_LOCATION)
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
                        showMsg("所有申请的权限都已通过", false)
                    } else {
                        showMsg("您拒绝了如下权限：$deniedList", true)
                        if (!this.isFinishing) {
                            this.finish()
                        }
                    }
                }
        }
    }

    private fun showMsg(msg: String, isLong: Boolean) {
        Toast.makeText(this, msg, if (isLong) Toast.LENGTH_LONG else Toast.LENGTH_SHORT).show()
    }
}