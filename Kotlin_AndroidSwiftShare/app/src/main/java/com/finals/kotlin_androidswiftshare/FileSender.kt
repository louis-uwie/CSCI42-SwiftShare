package com.finals.kotlin_androidswiftshare

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.io.File

class FileSender : AppCompatActivity() {

    private lateinit var fileRecyclerView: RecyclerView // RECYCLER VIEW TO DISPLAY LOCAL FILES
    private lateinit var previewTextView: TextView // TEXT PREVIEW FOR SELECTED FILE
    private lateinit var bluetoothDeviceRecyclerView: RecyclerView // RECYCLER VIEW TO DISPLAY BLUETOOTH DEVICES
    private lateinit var bluetoothManager: BluetoothManager // BLUETOOTH MANAGER INSTANCE
    private val fileList = mutableListOf<File>() // LIST OF FILES TO DISPLAY
    private val bluetoothDevices = mutableListOf<BluetoothDevice>() // LIST OF DEVICES TO SEND TO


    private val filePermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.values.all { it }) {
            loadFilesFromStorage() // ✅ Only load files when permission is granted
        } else {
            Toast.makeText(this, "File permission denied", Toast.LENGTH_SHORT).show()
        }
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_file_sender)

        // INITIALIZE UI COMPONENTS
        fileRecyclerView = findViewById(R.id.fileRecyclerView)
        previewTextView = findViewById(R.id.filePreview)
        bluetoothDeviceRecyclerView = findViewById(R.id.bluetoothDeviceRecyclerView)

        // SETUP FILE RECYCLERVIEW
        fileRecyclerView.layoutManager = LinearLayoutManager(this)
        val fileAdapter = FileAdapter(fileList) { selectedFile ->
            previewTextView.text = "Preview: ${selectedFile.name}"
            startBluetoothDiscovery() // START BLUETOOTH SCAN ON FILE SELECT
        }
        fileRecyclerView.adapter = fileAdapter

        // SETUP BLUETOOTH DEVICE RECYCLERVIEW
        bluetoothDeviceRecyclerView.layoutManager = LinearLayoutManager(this)
        val deviceAdapter = BluetoothDeviceAdapter(bluetoothDevices)
        bluetoothDeviceRecyclerView.adapter = deviceAdapter

        // INITIALIZE BLUETOOTH MANAGER
        bluetoothManager = BluetoothManager(this)
        bluetoothManager.onDeviceDiscovered = { device ->
            runOnUiThread {
                bluetoothDevices.add(device)
                deviceAdapter.notifyItemInserted(bluetoothDevices.size - 1)
            }
        }
        //REQUEST FILE PERMISSIONS
        requestFilePermissions()
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

    // REQUEST BLUETOOTH PERMISSIONS AND START DISCOVERY
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
                bluetoothManager.startDiscovery()
            }
        } else {
            bluetoothManager.startDiscovery()
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

}