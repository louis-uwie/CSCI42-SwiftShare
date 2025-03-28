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

/**
 * ADAPTER CLASS FOR DISPLAYING BLUETOOTH DEVICES IN A RECYCLERVIEW
 */
class BluetoothDeviceAdapter(private val devices: List<BluetoothDevice>) :
    RecyclerView.Adapter<BluetoothDeviceAdapter.DeviceViewHolder>() {

    /**
     * VIEWHOLDER TO HOLD THE LAYOUT OF EACH BLUETOOTH DEVICE ITEM
     */
    class DeviceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val deviceName: TextView = itemView.findViewById(android.R.id.text1) // TEXTVIEW FOR DEVICE NAME & ADDRESS
    }

    /**
     * CREATES NEW VIEWHOLDER INSTANCE WHEN NEEDED BY RECYCLERVIEW
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(android.R.layout.simple_list_item_1, parent, false) // BUILT-IN LAYOUT FOR SIMPLE TEXT ITEM
        return DeviceViewHolder(view)
    }

    /**
     * BINDS DEVICE DATA TO EACH VIEWHOLDER ITEM
     */
    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    override fun onBindViewHolder(holder: DeviceViewHolder, position: Int) {
        val device = devices[position]

        val context = holder.itemView.context
        val hasPermission = ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED

        val name = if (hasPermission) device.name ?: "Unnamed Device" else "Unknown Device"
        holder.deviceName.text = "$name\n${device.address}" // DISPLAY DEVICE NAME AND MAC ADDRESS
    }

    /**
     * RETURNS TOTAL NUMBER OF DEVICES IN THE LIST
     */
    override fun getItemCount(): Int = devices.size
}
