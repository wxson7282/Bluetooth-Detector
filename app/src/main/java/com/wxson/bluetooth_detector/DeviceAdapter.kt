package com.wxson.bluetooth_detector

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class DeviceAdapter(private val deviceList: List<MyDevice>) : RecyclerView.Adapter<DeviceAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view){
        val deviceName: TextView = view.findViewById(R.id.tvDeviceName)
        val deviceAddress: TextView = view.findViewById(R.id.tvDeviceAddress)
        val rssi: TextView = view.findViewById(R.id.tvRssi)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.device_item, parent, false)
        return ViewHolder(view)
    }

    @SuppressLint("MissingPermission")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val myDevice = deviceList[position]
        holder.deviceName.text = if (myDevice.device.name == null) "Unknown" else myDevice.device.name
        holder.deviceAddress.text = myDevice.device.address
        holder.rssi.text = String.format(MyApplication.context.resources.getString(R.string.rssi), myDevice.rssi)
    }

    override fun getItemCount() = deviceList.size
}