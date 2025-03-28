package com.finals.kotlin_androidswiftshare

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity() {

    private lateinit var bluetoothManager: BluetoothManager // INITIALIZING BluetoothManager.class INTO MainAactivity

    private lateinit var recyclerView: RecyclerView // UI ELEMENT FOR DISPLAYING BLUETOOTH DEVICES
    private val foundDevices = mutableListOf<BluetoothDevice>() // DYNAMIC LIST OF DISCOVERED DEVICES
    private lateinit var adapter: BluetoothDeviceAdapter // ADAPTER FOR THE RECYCLERVIEW

    private val requestPermissionsLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            permissions.entries.forEach {
                val permission = it.key
                val granted = it.value
                if (granted) {
                    println("Permission granted: $permission")
                } else {
                    println("Permission denied: $permission")
                }
            }
        }

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initial Request App Permissions
        requestStoragePermissions()

        bluetoothManager = BluetoothManager(this) // INITIALIZE BLUETOOTH MANAGER

        val fileLocatorButton = findViewById<Button>(R.id.LocateFileBTN)
        val startDiscoveryButton = findViewById<Button>(R.id.deviceDiscovery)
        recyclerView = findViewById(R.id.deviceRecyclerView) // LINK TO RECYCLERVIEW IN XML

        adapter = BluetoothDeviceAdapter(foundDevices)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        // CALLBACK: WHEN DEVICE IS DISCOVERED, UPDATE UI
        bluetoothManager.onDeviceDiscovered = { device ->
            runOnUiThread {
                foundDevices.add(device)
                adapter.notifyItemInserted(foundDevices.size - 1)
            }
        }

        fileLocatorButton.setOnClickListener{
            //TODO: Open File Locator
            Toast.makeText(this, "Locate File button clicked!", Toast.LENGTH_SHORT).show()
            openFileLocator()
        }

        startDiscoveryButton.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                requestBluetoothLauncher.launch(bluetoothPermissions)
            } else {
                try {
                    bluetoothManager.startDiscovery()
                } catch (e: SecurityException) {
                    e.printStackTrace()
                    Toast.makeText(this, "Missing Bluetooth permission", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    /**
     * LOCAL FILE ACCESS IS HERE
     */
    private fun requestStoragePermissions() {
        val permissionsToRequest = mutableListOf<String>()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES)
                != PackageManager.PERMISSION_GRANTED
            ) {
                permissionsToRequest.add(Manifest.permission.READ_MEDIA_IMAGES)
            }

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_VIDEO)
                != PackageManager.PERMISSION_GRANTED
            ) {
                permissionsToRequest.add(Manifest.permission.READ_MEDIA_VIDEO)
            }

        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED
            ) {
                permissionsToRequest.add(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }

        if (permissionsToRequest.isNotEmpty()) {
            requestPermissionsLauncher.launch(permissionsToRequest.toTypedArray())
        }
    }

    private fun openFileLocator(){
        // TODO: Implement system file picker logic
    }

    /**
     * BLUETOOTH REQUEST PERMISSIONS ARE HERE
     */
    val bluetoothPermissions = arrayOf(
        Manifest.permission.BLUETOOTH_SCAN,
        Manifest.permission.BLUETOOTH_CONNECT
    )

    val requestBluetoothLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions.values.all { it }
        if (granted) {
            bluetoothManager.startDiscovery()
        } else {
            Toast.makeText(this, "Bluetooth permissions denied", Toast.LENGTH_SHORT).show()
        }
    }
}