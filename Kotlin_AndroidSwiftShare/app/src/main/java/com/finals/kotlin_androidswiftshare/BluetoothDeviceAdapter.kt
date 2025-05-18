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
    private val devices: List<BluetoothDevice>,
    private val onDeviceClick: (BluetoothDevice) -> Unit
) : RecyclerView.Adapter<BluetoothDeviceAdapter.DeviceViewHolder>() {

    private var selectedPosition = RecyclerView.NO_POSITION

    inner class DeviceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textView: TextView = itemView.findViewById(R.id.deviceItemText)
        val rowLayout: View = itemView.findViewById(R.id.deviceRowLayout)

        init {
            itemView.setOnClickListener {
                val previous = selectedPosition
                selectedPosition = adapterPosition
                notifyItemChanged(previous)
                notifyItemChanged(selectedPosition)
                onDeviceClick(devices[adapterPosition])
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_bluetooth_device_selectable, parent, false)
        return DeviceViewHolder(view)
    }

    override fun onBindViewHolder(holder: DeviceViewHolder, position: Int) {
        val device = devices[position]
        val name = device.name ?: device.address ?: "Unknown Device"
        holder.textView.text = name

        if (position == selectedPosition) {
            holder.rowLayout.setBackgroundColor(0xFFB2EBF2.toInt()) // Highlight selected
        } else {
            holder.rowLayout.setBackgroundColor(0x00000000) // Transparent for others
        }
    }

    override fun getItemCount(): Int = devices.size
}
