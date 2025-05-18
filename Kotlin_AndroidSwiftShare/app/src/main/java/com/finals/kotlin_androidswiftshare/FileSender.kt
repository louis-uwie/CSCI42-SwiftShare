package com.finals.kotlin_androidswiftshare

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.content.ContentUris
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.webkit.MimeTypeMap
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

class FileSender : AppCompatActivity() {

    private lateinit var previewTextView: TextView
    private lateinit var bluetoothManager: BluetoothManager
    private lateinit var bluetoothDeviceAdapter: BluetoothDeviceAdapter
    private lateinit var deviceRecyclerView: RecyclerView
    private val bluetoothDevices = mutableListOf<BluetoothDevice>()
    private var selectedFile: File? = null
    private var discoveryHandler: Handler? = null

    /**
     * Handles the file picker for selecting a file to send.
     */
    private val filePickerLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            val fileName = uri.lastPathSegment?.substringAfterLast('/') ?: "document"
            val file = createLocalCopyFromUri(uri, fileName)
            if (file != null) {
                previewTextView.text = "Preview: ${file.name}"
                selectedFile = file
            }
        }
    }

    /**
     * Requests Bluetooth-related permissions.
     */
    private val requestBluetoothPermissions = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        if (allGranted) {
            startBluetoothDiscoveryLoop()
        } else {
            Toast.makeText(this, "Bluetooth permissions denied", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * CLEANUP BLUETOOTH RESOURCES ON EXIT
     */
    override fun onDestroy() {
        super.onDestroy()
        bluetoothManager.cleanup()
        discoveryHandler?.removeCallbacksAndMessages(null)
    }

    /**
     * ON CREATE — Main logic and initialization
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_file_sender)

        previewTextView = findViewById(R.id.filePreview)
        val bluetoothSendFileButton = findViewById<Button>(R.id.SendFileBTN)
        val SelectFileButton = findViewById<Button>(R.id.SelectFileBTN2)
        deviceRecyclerView = findViewById(R.id.deviceRecyclerView)
        deviceRecyclerView.layoutManager = LinearLayoutManager(this)

        // INITIALIZE BLUETOOTH MANAGER & ADAPTER
        bluetoothManager = BluetoothManager(this)
        bluetoothDeviceAdapter = BluetoothDeviceAdapter(bluetoothDevices) { device ->
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
                sendFileToDevice(device)
            } else {
                Toast.makeText(this, "Missing permission to connect to device.", Toast.LENGTH_SHORT).show()
            }
        }
        deviceRecyclerView.adapter = bluetoothDeviceAdapter

        // HANDLE DEVICE DISCOVERY CALLBACK
        bluetoothManager.onDeviceDiscovered = { device ->
            runOnUiThread {
                val hasPermission = ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED
                val name = if (hasPermission) device.name else "Unnamed Device"

                if (!bluetoothDevices.any { it.address == device.address }) {
                    bluetoothDevices.add(device)
                    bluetoothDeviceAdapter.notifyItemInserted(bluetoothDevices.size - 1)
                    println("Discovered device: $name - ${device.address}")
                }
            }
        }

        // HANDLE DISCOVERY COMPLETION
        bluetoothManager.onDiscoveryFinished = {
            runOnUiThread {
                bluetoothDeviceAdapter.notifyDataSetChanged()
            }
        }

        // HANDLE BLUETOOTH SEND BUTTON
        bluetoothSendFileButton.setOnClickListener {
            if (selectedFile == null) {
                Toast.makeText(this, "Please select a file first.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            bluetoothDevices.clear()
            bluetoothDeviceAdapter.notifyDataSetChanged()
            startBluetoothDiscoveryLoop()
        }


        // OPEN FILE PICKER
        filePickerLauncher.launch(arrayOf("application/pdf", "image/jpeg", "image/png", "text/plain"))
    }

    /**
     * CREATES A LOCAL COPY OF THE FILE FOR SHARING
     */
    private fun createLocalCopyFromUri(uri: Uri, originalName: String): File? {
        return try {
            val mimeType = contentResolver.getType(uri) ?: "application/octet-stream"
            val extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType) ?: "bin"

            val safeName = originalName
                .substringAfterLast('/')
                .substringBeforeLast('.')
                .replace(Regex("[^a-zA-Z0-9_-]"), "_") + "." + extension

            val file = File(cacheDir, safeName)
            contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(file).use { output ->
                    input.copyTo(output)
                }
            }
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * LOOP TO KEEP DISCOVERY RUNNING PERIODICALLY
     */
    private fun startBluetoothDiscoveryLoop() {
        discoveryHandler = Handler(Looper.getMainLooper())
        discoveryHandler?.post(object : Runnable {
            override fun run() {
                startBluetoothDiscovery()
                discoveryHandler?.postDelayed(this, 15000)
            }
        })
    }

    /**
     * TRIGGER BLUETOOTH DISCOVERY AND HANDLE PERMISSIONS
     */
    private fun startBluetoothDiscovery() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
            ) {
                requestBluetoothPermissions.launch(
                    arrayOf(
                        Manifest.permission.BLUETOOTH_SCAN,
                        Manifest.permission.BLUETOOTH_CONNECT,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    )
                )
            } else {
                bluetoothManager.startDiscovery()
            }
        } else {
            bluetoothManager.startDiscovery()
        }
    }

    /**
     * USES SYSTEM BLUETOOTH INTENT TO SEND FILE TO DEVICE
     */
    private fun sendFileToDevice(device: BluetoothDevice) {
        if (selectedFile == null) {
            Toast.makeText(this, "No file selected to send.", Toast.LENGTH_SHORT).show()
            return
        }

        val uri: Uri = FileProvider.getUriForFile(
            this,
            "$packageName.provider",
            selectedFile!!
        )

        val name = device.name ?: device.address

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = contentResolver.getType(uri)
            putExtra(Intent.EXTRA_STREAM, uri)
            `package` = "com.android.bluetooth"
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        }

        try {
            startActivity(Intent.createChooser(intent, "Send file to $name via Bluetooth"))
        } catch (e: Exception) {
            Toast.makeText(this, "Bluetooth sharing app not available.", Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }
    }
}
