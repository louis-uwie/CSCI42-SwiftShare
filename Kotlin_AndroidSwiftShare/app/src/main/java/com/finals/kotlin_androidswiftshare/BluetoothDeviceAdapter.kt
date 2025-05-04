package com.finals.kotlin_androidswiftshare

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RequiresPermission
import androidx.recyclerview.widget.RecyclerView

class BluetoothDeviceAdapter(
    private val devices: List<BluetoothDevice>, // LIST OF BLUETOOTH DEVICES TO DISPLAY
    private val onDeviceClick: (BluetoothDevice) -> Unit // CALLBACK WHEN A DEVICE IS CLICKED
) : RecyclerView.Adapter<BluetoothDeviceAdapter.DeviceViewHolder>() {

    // HOLDER CLASS FOR EACH DEVICE ROW
    inner class DeviceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val deviceNameTextView: TextView = itemView.findViewById(android.R.id.text1)
    }

    // CREATE VIEW HOLDER FOR DEVICE ROW
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(android.R.layout.simple_list_item_1, parent, false)
        return DeviceViewHolder(view)
    }

    // BIND DEVICE TO TEXTVIEW AND SET CLICK ACTION
    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    override fun onBindViewHolder(holder: DeviceViewHolder, position: Int) {
        val device = devices[position]
        val displayName = device.name ?: device.address ?: "Unknown Device"
        holder.deviceNameTextView.text = displayName
        holder.itemView.setOnClickListener {
            onDeviceClick(device)
        }
    }

    // RETURN TOTAL DEVICE COUNT
    override fun getItemCount(): Int = devices.size
}
