package com.wxson.blt_rssi

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class DeviceAdapter(private val deviceList: List<BluetoothDevice>, val actionFun : (BluetoothDevice) -> Unit) : RecyclerView.Adapter<DeviceAdapter.ViewHolder>() {

    var currentPosition = -1

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view){
        val deviceName: TextView = view.findViewById(R.id.tvDeviceName)
        val deviceAddress: TextView = view.findViewById(R.id.tvDeviceAddress)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.device_item, parent, false)
        //add item click listener
        val viewHolder = ViewHolder(view)
        viewHolder.itemView.setOnClickListener {
            val device = deviceList[viewHolder.adapterPosition]
            actionFun(device)
            currentPosition = viewHolder.adapterPosition
        }
        return viewHolder
    }

    @SuppressLint("MissingPermission")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val device = deviceList[position]
        holder.deviceName.text = device.name
        holder.deviceAddress.text = device.address
        if (position == currentPosition) {
            holder.deviceName.setTextColor(Color.RED)
            holder.deviceAddress.setTextColor(Color.RED)
        } else {
            holder.deviceName.setTextColor(Color.BLACK)
            holder.deviceAddress.setTextColor(Color.BLACK)
        }
    }

    override fun getItemCount() = deviceList.size

}