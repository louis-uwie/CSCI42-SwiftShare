package com.finals.kotlin_androidswiftshare

import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.core.app.ActivityCompat
import android.Manifest


class BluetoothManager(private val context: Context) {
    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private val discoveredDevices = mutableListOf<BluetoothDevice>()

    var onDeviceDiscovered: ((BluetoothDevice) -> Unit)? = null

    /**
     * BroadcastReceiver to handle Bluetooth discovery events.
     * - Listens for ACTION_FOUND when a device is discovered.
     * - Listens for ACTION_DISCOVERY_FINISHED when discovery is complete.
     */
    private val discoveryReceiver = object : BroadcastReceiver() {
        @SuppressLint("MissingPermission")
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                /**
                 * Triggered when a Bluetooth device is found.
                 * Extracts the device from the intent and adds it to discoveredDevices if not already present.
                 */
                BluetoothDevice.ACTION_FOUND -> {
                    val device: BluetoothDevice? = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    device?.let {
                        if (!discoveredDevices.contains(it)) {
                            discoveredDevices.add(it)
                            Log.d("BluetoothManager", "Discovered: ${it.name} - ${it.address}")

                            onDeviceDiscovered?.invoke(it)
                        }
                    }
                }
                /**
                 * Triggered when the discovery process is finished.
                 * Logs that the discovery is complete.
                 */
                BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                    Log.d("BluetoothManager", "Discovery finished")
                }
            }
        }
    }

    init {
        // Register the receiver to listen for discovery events
        val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
        context.registerReceiver(discoveryReceiver, filter)
    }

    /**
     * Starts scanning for Bluetooth devices nearby.
     * Ensures any ongoing discovery is stopped before starting a new one.
     */
    @SuppressLint("MissingPermission")
    fun startDiscovery() {
        bluetoothAdapter?.let {
            if (it.isDiscovering) it.cancelDiscovery()
            it.startDiscovery()
        }
    }

    /**
     * Stops any ongoing Bluetooth discovery process.
     */
    @SuppressLint("MissingPermission")
    fun stopDiscovery() {
        bluetoothAdapter?.cancelDiscovery()
    }

    /**
     * Initiates pairing with a given Bluetooth device.
     *
     * @param device The Bluetooth device to pair with.
     */
    @SuppressLint("MissingPermission")
    fun pairDevice(device: BluetoothDevice) {
        device.createBond()
    }

    /**
     * Makes the device discoverable to others via Bluetooth for a specified duration.
     *
     * @param duration The time in seconds the device will be discoverable (default: 300 seconds, max: 3600 seconds).
     */
    fun makeDeviceDiscoverable(duration: Int = 300) {
        val discoverableIntent = Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE).apply {
            putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, duration)
        }
        context.startActivity(discoverableIntent)
    }

    /**
     * Cleans up resources by unregistering the Bluetooth discovery broadcast receiver.
     * Should be called when the activity or app is closing.
     */
    fun cleanup() {
        context.unregisterReceiver(discoveryReceiver)
    }

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

/**
 * =====================================================================================
 * Example Usage for UI Development
 * =====================================================================================
 * This section provides example snippets of how to integrate BluetoothManager into an
 * Android Activity or ViewModel.
 *
 * -- Initialize BluetoothManager inside an Activity or ViewModel
 * val bluetoothManager = BluetoothManager(this)
 *
 * -- Start scanning for Bluetooth devices when a button is clicked
 * bluetoothManager.startDiscovery()
 *
 *  Stop scanning when needed
 * bluetoothManager.stopDiscovery()
 *
 * -- Make the device discoverable for 2 minutes
 * bluetoothManager.makeDeviceDiscoverable(120)
 *
 * -- Pair with a selected device (assuming you have a reference to a BluetoothDevice)
 * val selectedDevice: BluetoothDevice = discoveredDevices[0] // Example selection
 * bluetoothManager.pairDevice(selectedDevice)
 *
 * -- Clean up resources when done (e.g., in onDestroy of an Activity)
 * bluetoothManager.cleanup()
 * =====================================================================================
 */