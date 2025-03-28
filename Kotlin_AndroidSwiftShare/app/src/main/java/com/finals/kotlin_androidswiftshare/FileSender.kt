package com.finals.kotlin_androidswiftshare

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.io.File

class FileSender : AppCompatActivity() {

    /**
     * All Variables utilized in the Program
     */
    private lateinit var fileRecyclerView: RecyclerView // RECYCLER VIEW TO DISPLAY LOCAL FILES
    private lateinit var previewTextView: TextView // TEXT PREVIEW FOR SELECTED FILE
    private lateinit var bluetoothManager: BluetoothManager // BLUETOOTH MANAGER INSTANCE
    private lateinit var bluetoothDeviceAdapter: BluetoothDeviceAdapter // ADAPTER FOR BLUETOOTH DEVICES
    private val fileList = mutableListOf<File>() // LIST OF FILES TO DISPLAY
    private val bluetoothDevices = mutableListOf<BluetoothDevice>() // LIST OF DEVICES TO SEND TO

    /** ----------------------------------------[World Border]------------------------------------------------------- */

    /**
     * ALL LOCAL FILE REQUEST FUNCTIONALITIES -----------------------------------------------------------------------------------------------
     */
    private val filePermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.values.all { it }) {
            loadFilesFromStorage() // ✅ Only load files when permission is granted
        } else {
            Toast.makeText(this, "File permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    // LOAD FILES FROM INTERNAL/EXTERNAL STORAGE (BASIC IMPLEMENTATION)
    private fun loadFilesFromStorage() {
        val dir = Environment.getExternalStorageDirectory()
        val files = dir.listFiles()
        if (files != null) {
            fileList.addAll(files.filter { it.isFile && it.extension in listOf("txt", "pdf", "jpg", "png") })
            fileRecyclerView.adapter?.notifyDataSetChanged()
        }
    }

    private fun requestFilePermissions() {
        val permissionsToRequest = mutableListOf<String>()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionsToRequest.add(Manifest.permission.READ_MEDIA_IMAGES)
            permissionsToRequest.add(Manifest.permission.READ_MEDIA_VIDEO)
        } else {
            permissionsToRequest.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        filePermissionLauncher.launch(permissionsToRequest.toTypedArray())
    }

    /**
     * END LOCAL FILE FUNCTIONALITIES -----------------------------------------------------------------------------------------------
     */

    /** ----------------------------------------[World Border]------------------------------------------------------- */

    /**
     * ALL BLUETOOTH FUNCTIONALITIES -----------------------------------------------------------------------------------------------
     */
    private fun startBluetoothDiscovery() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED
            ) {
                requestBluetoothPermissions.launch(arrayOf(
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_CONNECT
                ))
            } else {
                Toast.makeText(this, "Finding Bluetooth devices...", Toast.LENGTH_SHORT).show()
                bluetoothManager.startDiscovery()
            }
        } else {
            Toast.makeText(this, "Finding Bluetooth devices...", Toast.LENGTH_SHORT).show()
            bluetoothManager.startDiscovery()
        }
    }

    private fun showBluetoothDevicesPopup() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_bluetooth_devices, null)
        val recyclerView = dialogView.findViewById<RecyclerView>(R.id.deviceRecyclerView)

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = bluetoothDeviceAdapter

        val dialog = AlertDialog.Builder(this)
            .setTitle("Available Bluetooth Devices")
            .setView(dialogView)
            .setNegativeButton("Cancel", null)
            .create()

        dialog.show()

        // 🔁 Update UI even after dialog is shown
        bluetoothManager.onDeviceDiscovered = { device ->
            runOnUiThread {
                try {
                    val hasPermission = ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED
                    val name = if (hasPermission) device.name else "Unnamed Device"

                    if (!bluetoothDevices.contains(device)) {
                        bluetoothDevices.add(device)
                        bluetoothDeviceAdapter.notifyItemInserted(bluetoothDevices.size - 1)
                        println("Discovered device in dialog: $name")
                    }
                } catch (e: SecurityException) {
                    e.printStackTrace()
                }
            }
        }
    }


    private val requestBluetoothPermissions = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        if (allGranted) {
            bluetoothManager.startDiscovery()
        } else {
            Toast.makeText(this, "Bluetooth permissions denied", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        bluetoothManager.cleanup() // UNREGISTER RECEIVER
    }

    /**
     * END BLUETOOTH FUNCTIONALITIES -----------------------------------------------------------------------------------------------
     */

    /** ----------------------------------------[World Border]------------------------------------------------------- */

    /**
     * ON-CREATE Initializes the functionalities on launch of FileSender.kt
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_file_sender)

        // INITIALIZE UI COMPONENTS
        fileRecyclerView = findViewById(R.id.fileRecyclerView)
        previewTextView = findViewById(R.id.filePreview)
        val bluetoothSendFileButton = findViewById<Button>(R.id.SendFileBTN)

        // SETUP FILE RECYCLERVIEW
        fileRecyclerView.layoutManager = LinearLayoutManager(this)
        val fileAdapter = FileAdapter(fileList) { selectedFile ->
            previewTextView.text = "Preview: ${selectedFile.name}"
        }
        fileRecyclerView.adapter = fileAdapter

        // INITIALIZE BLUETOOTH MANAGER & ADAPTER
        bluetoothManager = BluetoothManager(this)
        bluetoothDeviceAdapter = BluetoothDeviceAdapter(bluetoothDevices)

        bluetoothManager.onDeviceDiscovered = { device ->
            runOnUiThread {
                try {
                    val hasPermission = ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED
                    val name = if (hasPermission) device.name else "Unnamed Device"

                    if (!bluetoothDevices.contains(device)) {
                        bluetoothDevices.add(device)
                        bluetoothDeviceAdapter.notifyItemInserted(bluetoothDevices.size - 1)

                        println("Discovered device: $name - ${device.address}")
                    }
                } catch (e: SecurityException) {
                    e.printStackTrace()
                }
            }
        }


        bluetoothManager.onDiscoveryFinished = {
            runOnUiThread {
                showBluetoothDevicesPopup()
            }
        }

        bluetoothSendFileButton.setOnClickListener {
            bluetoothDevices.clear()
            bluetoothDeviceAdapter.notifyDataSetChanged()
            startBluetoothDiscovery()

            Handler(Looper.getMainLooper()).postDelayed({
                showBluetoothDevicesPopup()
            }, 2000) // Adjust delay as needed
        }


        requestFilePermissions() // Call File Access / Permission Request
    }
}
