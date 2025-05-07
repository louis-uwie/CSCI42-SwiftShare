package com.finals.kotlin_androidswiftshare

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.content.ContentUris
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
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
import java.io.FileOutputStream
import java.io.InputStream

class FileSender : AppCompatActivity() {

    private lateinit var deviceRecyclerView: RecyclerView
    private lateinit var previewTextView: TextView
    private lateinit var bluetoothManager: BluetoothManager
    private lateinit var bluetoothDeviceAdapter: BluetoothDeviceAdapter
    private val fileList = mutableListOf<File>()
    private val bluetoothDevices = mutableListOf<BluetoothDevice>()
    private var selectedFile: File? = null

    private val requestBluetoothPermissions = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.values.all { it }) {
            bluetoothManager.startDiscovery()
        } else {
            Toast.makeText(this, "Bluetooth permissions denied", Toast.LENGTH_SHORT).show()
        }
    }

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.values.all { it }) {
            loadFilesFromMediaStore()
        } else {
            Toast.makeText(this, "Some permissions were denied.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        bluetoothManager.cleanup()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_file_sender)

        deviceRecyclerView = findViewById(R.id.deviceRecyclerView)
        previewTextView = findViewById(R.id.filePreview)
        val bluetoothSendFileButton = findViewById<Button>(R.id.SendFileBTN)
        val lanSendFileButton = findViewById<Button>(R.id.SendFileLanBTN)

        deviceRecyclerView.layoutManager = LinearLayoutManager(this)
        val fileAdapter = FileAdapter(fileList) { selected ->
            previewTextView.text = "Preview: ${selected.name}"
            selectedFile = selected
        }
        deviceRecyclerView.adapter = fileAdapter

        bluetoothManager = BluetoothManager(this)
        bluetoothDeviceAdapter = BluetoothDeviceAdapter(bluetoothDevices) { device ->
            Toast.makeText(this, "Selected: ${device.name}", Toast.LENGTH_SHORT).show()
        }


        bluetoothManager.onDeviceDiscovered = { device ->
            runOnUiThread {
                val hasPermission = ActivityCompat.checkSelfPermission(
                    this, Manifest.permission.BLUETOOTH_CONNECT
                ) == PackageManager.PERMISSION_GRANTED
                val name = if (hasPermission) device.name else "Unnamed Device"

                if (!bluetoothDevices.contains(device)) {
                    bluetoothDevices.add(device)
                    bluetoothDeviceAdapter.notifyItemInserted(bluetoothDevices.size - 1)
                    println("Discovered device: $name - ${device.address}")
                }
            }
        }

        bluetoothManager.onDiscoveryFinished = {
            runOnUiThread { showBluetoothDevicesPopup() }
        }

        bluetoothSendFileButton.setOnClickListener {
            if (selectedFile == null) {
                Toast.makeText(this, "Please select a file first.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            bluetoothDevices.clear()
            bluetoothDeviceAdapter.notifyDataSetChanged()
            startBluetoothDiscovery()
            android.os.Handler(Looper.getMainLooper()).postDelayed({ showBluetoothDevicesPopup() }, 2000)
        }

        lanSendFileButton.setOnClickListener {
            if (selectedFile == null) {
                Toast.makeText(this, "Please select a file first.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val lanManager = LanManager(this)
            val connectivityManager = getSystemService(CONNECTIVITY_SERVICE) as android.net.ConnectivityManager
            val network = connectivityManager.activeNetwork
            val capabilities = connectivityManager.getNetworkCapabilities(network)
            val isConnectedToLAN = capabilities?.hasTransport(android.net.NetworkCapabilities.TRANSPORT_WIFI) == true

            if (isConnectedToLAN) {
                Toast.makeText(this, "Connected to LAN. Sending file...", Toast.LENGTH_SHORT).show()

                lanManager.startServer { fileName, content ->
                    runOnUiThread {
                        Toast.makeText(this, "Received: $fileName (${content.size} bytes)", Toast.LENGTH_SHORT).show()
                    }
                    val receivedFile = File(filesDir, fileName)
                    receivedFile.writeBytes(content)
                }

                lanManager.sendFileTo("192.168.1.5", selectedFile!!)
            } else {
                Toast.makeText(this, "Not connected to LAN.", Toast.LENGTH_LONG).show()
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.READ_MEDIA_IMAGES,
                    Manifest.permission.READ_MEDIA_VIDEO,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                )
            )
        } else {
            permissionLauncher.launch(
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
            )
        }
    }

    private fun loadFilesFromMediaStore() {
        val projection = arrayOf(
            MediaStore.Files.FileColumns._ID,
            MediaStore.Files.FileColumns.DISPLAY_NAME,
            MediaStore.Files.FileColumns.MIME_TYPE
        )

        val uri = MediaStore.Files.getContentUri("external")
        val sortOrder = MediaStore.Files.FileColumns.DATE_ADDED + " DESC"

        val selection = MediaStore.Files.FileColumns.MIME_TYPE + " IN (?, ?, ?, ?, ?, ?, ?, ?)"
        val selectionArgs = arrayOf(
            "application/pdf",
            "image/jpeg",
            "image/png",
            "text/plain",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/vnd.ms-excel",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
        )

        val cursor = contentResolver.query(uri, projection, selection, selectionArgs, sortOrder)
        cursor?.use {
            val idColumn = it.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID)
            val nameColumn = it.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DISPLAY_NAME)

            fileList.clear()

            while (it.moveToNext()) {
                val id = it.getLong(idColumn)
                val name = it.getString(nameColumn)
                val contentUri = ContentUris.withAppendedId(uri, id)
                val file = createLocalCopyFromUri(contentUri, name)
                if (file != null) {
                    fileList.add(file)
                }
            }
        }

        Log.d("MediaStore", "Loaded ${fileList.size} files from MediaStore")

        if (fileList.isEmpty()) {
            Toast.makeText(this, "No supported files found. Try uploading PDFs, images, or documents.", Toast.LENGTH_SHORT).show()
        }

        deviceRecyclerView.adapter?.notifyDataSetChanged()
    }

    private fun createLocalCopyFromUri(uri: Uri, fileName: String): File? {
        return try {
            val file = File(cacheDir, fileName)
            val inputStream: InputStream? = contentResolver.openInputStream(uri)
            val outputStream = FileOutputStream(file)
            inputStream?.copyTo(outputStream)
            inputStream?.close()
            outputStream.close()
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
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
    }

    private fun startBluetoothDiscovery() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED
            ) {
                requestBluetoothPermissions.launch(
                    arrayOf(
                        Manifest.permission.BLUETOOTH_SCAN,
                        Manifest.permission.BLUETOOTH_CONNECT
                    )
                )
            } else {
                Toast.makeText(this, "Finding Bluetooth devices...", Toast.LENGTH_SHORT).show()
                bluetoothManager.startDiscovery()
            }
        } else {
            Toast.makeText(this, "Finding Bluetooth devices...", Toast.LENGTH_SHORT).show()
            bluetoothManager.startDiscovery()
        }
    }
}