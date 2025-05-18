package com.finals.kotlin_androidswiftshare

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.*
import android.content.pm.PackageManager
import android.util.Log
import android.widget.Toast

import androidx.activity.result.ActivityResultLauncher
import androidx.core.app.ActivityCompat

import java.util.*

/**
 * BluetoothManager handles Bluetooth discovery, pairing, permissions,
 * and now file sending between paired devices.
 *
 * Developer Note:  Some features are un-used for future changes in the project.
 *                  > SendFileToDevice() was moved to FileSender.kt
 *                  > pairDevice() was not used as it is ideal that the devices are already paired.
 *                  > makeDeviceDiscoverable() was not used as the app only sends. Receiving is available without opening the application.
 *                      It just requires BT connection which can be done outside the app.
 */
class BluetoothManager(private val context: Context) {

    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private val discoveredDevices = mutableListOf<BluetoothDevice>()

    fun getDiscoveredDevices(): List<BluetoothDevice> = discoveredDevices

    var onDeviceDiscovered: ((BluetoothDevice) -> Unit)? = null
    var onDiscoveryFinished: (() -> Unit)? = null



    /**
     * Handles events for discovering new Bluetooth devices and when discovery ends.
     */
    private val discoveryReceiver = object : BroadcastReceiver() {
        @SuppressLint("MissingPermission")
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                BluetoothDevice.ACTION_FOUND -> {
                    val device: BluetoothDevice? = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    device?.let {
                        Log.d("BluetoothManager", "Device Found: name=${it.name}, address=${it.address}")
                        Log.d("BTDebug", "Found device: ${device.name} - ${device.address}")
                        Toast.makeText(context, "Found: ${device.name}", Toast.LENGTH_SHORT).show()

                        if (!discoveredDevices.contains(it)) {
                            discoveredDevices.add(it)
                            onDeviceDiscovered?.invoke(it)
                        }

                    } ?: Log.d("BluetoothManager", "Device found but is null.")
                }

                BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                    Log.d("BluetoothManager", "Discovery finished")
                    onDiscoveryFinished?.invoke()
                }
            }
        }
    }


    init {
        val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
        context.registerReceiver(discoveryReceiver, filter)
    }



    /**
     * Begins Bluetooth device discovery. Stops any ongoing scan first.
     */
    @SuppressLint("MissingPermission")
    fun startDiscovery() {
        bluetoothAdapter?.let {
            if (it.isDiscovering) it.cancelDiscovery()
            val started = it.startDiscovery()
            Log.d("BluetoothManager", "Discovery started: $started")
        }
    }



    /**
     * Stops current Bluetooth discovery if active.
     */
    @SuppressLint("MissingPermission")
    fun stopDiscovery() {
        bluetoothAdapter?.cancelDiscovery()
    }



    /**
     * Starts pairing with the given device.
     */
    @SuppressLint("MissingPermission")
    fun pairDevice(device: BluetoothDevice) {
        device.createBond()
    }



    /**
     * Makes the current device discoverable to others for the specified number of seconds.
     */
    fun makeDeviceDiscoverable(duration: Int = 300) {
        val discoverableIntent = Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE).apply {
            putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, duration)
        }
        context.startActivity(discoverableIntent)
    }



    /**
     * Unregisters discovery broadcast receiver. Call during cleanup (e.g., onDestroy).
     */
    fun cleanup() {
        context.unregisterReceiver(discoveryReceiver)
    }



    /**
     * Checks and requests Bluetooth-related permissions if not already granted.
     */
    fun requestBluetoothPermissions(launcher: ActivityResultLauncher<Array<String>>) {
        if (context is Activity && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            val missingPermissions = mutableListOf<String>()

            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                missingPermissions.add(Manifest.permission.BLUETOOTH_SCAN)
            }
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                missingPermissions.add(Manifest.permission.BLUETOOTH_CONNECT)
            }

            if (missingPermissions.isNotEmpty()) {
                launcher.launch(missingPermissions.toTypedArray())
            }
        }
    }
}

/*

  USAGE GUIDE

  --START DISCOVERY--

      bluetoothManager.onDeviceDiscovered = { device ->
        Log.d("Frontend", "Found: ${device.name} (${device.address})")
        // show in list or auto-select
    }
    bluetoothManager.onDiscoveryFinished = {
        Log.d("Frontend", "Discovery complete")
    }

  --PAIR WITH DEVICE--

      bluetoothManager.pairDevice(selectedDevice)

  --SEND FILE TO PAIRED DEVICE--

      val file: File = selectedFile  // from SAF or internal cache
      bluetoothManager.sendFileToDevice(selectedDevice, file) { success, error ->
        if (success) {
            Toast.makeText(context, "File sent!", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Failed: $error", Toast.LENGTH_LONG).show()
        }
    }

  --CLEANUP ON DESTROY--

    override fun onDestroy() {
        super.onDestroy()
        bluetoothManager.cleanup()
    }

 */
