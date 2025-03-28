package com.finals.kotlin_androidswiftshare

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.content.pm.PackageManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RequiresPermission
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.RecyclerView

class BluetoothDeviceAdapter(private val devices: List<BluetoothDevice>) :
    RecyclerView.Adapter<BluetoothDeviceAdapter.DeviceViewHolder>() {

    class DeviceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val deviceName: TextView = itemView.findViewById(android.R.id.text1)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(android.R.layout.simple_list_item_1, parent, false)
        return DeviceViewHolder(view)
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    override fun onBindViewHolder(holder: DeviceViewHolder, position: Int) {
        val device = devices[position]
        val name = if (ActivityCompat.checkSelfPermission(holder.itemView.context, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
            device.name ?: "Unnamed Device"
        } else {
            "Unknown Device"
        }
        holder.deviceName.text = "$name\n${device.address}"
    }


    override fun getItemCount(): Int = devices.size
}