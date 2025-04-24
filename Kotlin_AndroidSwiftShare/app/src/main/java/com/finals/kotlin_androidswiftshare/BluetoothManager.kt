package com.finals.kotlin_androidswiftshare

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.*
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.core.app.ActivityCompat
import java.io.File
import java.io.OutputStream
import java.util.*
import kotlin.concurrent.thread

/**
 * BluetoothManager handles Bluetooth discovery, pairing, permissions,
 * and now file sending between paired devices.
 */
class BluetoothManager(private val context: Context) {

    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private val discoveredDevices = mutableListOf<BluetoothDevice>()

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

    /**
     * Sends a file (via content URI) to a paired Bluetooth device.
     * Automatically opens a socket, streams file content, and closes the connection.
     *
     * @param device The target Bluetooth device.
     * @param fileUri URI to the file selected via content picker or file explorer.
     * @param onComplete Callback for success/failure.
     */
    private val APP_UUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

    @SuppressLint("MissingPermission")
    fun sendFileToDevice(
        device: BluetoothDevice,
        fileUri: Uri,
        onComplete: (success: Boolean, error: String?) -> Unit
    ) {
        thread {
            try {
                val socket = device.createRfcommSocketToServiceRecord(APP_UUID)
                bluetoothAdapter?.cancelDiscovery()
                socket.connect()

                val outputStream: OutputStream = socket.outputStream
                val inputStream = context.contentResolver.openInputStream(fileUri)

                if (inputStream == null) {
                    onComplete(false, "File input stream is null")
                    socket.close()
                    return@thread
                }

                val buffer = ByteArray(1024)
                var bytesRead: Int

                while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                    outputStream.write(buffer, 0, bytesRead)
                }

                outputStream.flush()
                inputStream.close()
                socket.close()

                onComplete(true, null)
            } catch (e: Exception) {
                Log.e("BluetoothClient", "Error sending file", e)
                onComplete(false, e.message)
            }
        }
    }

    /**
     * Sends a file (via File object) to a paired Bluetooth device.
     * This is useful when SAF returns a File path instead of content Uri.
     */
    @SuppressLint("MissingPermission")
    fun sendFileTo(
        device: BluetoothDevice,
        file: File,
        onComplete: (success: Boolean, error: String?) -> Unit
    ) {
        thread {
            try {
                if (device.bondState != BluetoothDevice.BOND_BONDED) {
                    onComplete(false, "Device is not paired. Please pair first.")
                    return@thread
                }

                bluetoothAdapter?.cancelDiscovery()

                // Create socket securely
                val socket = device.createRfcommSocketToServiceRecord(APP_UUID)

                socket.connect()

                val outputStream: OutputStream = socket.outputStream
                val inputStream = file.inputStream()

                val buffer = ByteArray(1024)
                var bytesRead: Int

                while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                    outputStream.write(buffer, 0, bytesRead)
                }

                outputStream.flush()
                inputStream.close()
                socket.close()

                onComplete(true, null)

            } catch (e: Exception) {
                Log.e("BluetoothClient", "Error sending file", e)
                onComplete(false, e.message)
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
