package com.finals.kotlin_androidswiftshare

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.content.ContentUris
import android.content.Intent
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
//    private val bluetoothDevices = mutableListOf<BluetoothDevice>()
    private var selectedFile: File? = null

    /**
     * PERMISSION HANDLERS FOR BLUETOOTH + FILE ACCESS
     */
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

    private val filePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val dataUri = result.data?.data
            if (dataUri != null) {
                val fileName = getFileNameFromUri(dataUri)
                val file = createLocalCopyFromUri(dataUri, fileName)
                if (file != null) {
                    fileList.add(0, file)
                    selectedFile = file
                    previewTextView.text = "Preview: ${file.name}"
                    deviceRecyclerView.adapter?.notifyItemInserted(0)
                    deviceRecyclerView.scrollToPosition(0)
                    Toast.makeText(this, "File selected: ${file.name}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun getFileNameFromUri(uri: Uri): String {
        var name = "selected_file"
        val cursor = contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            val nameIndex = cursor.getColumnIndex(MediaStore.Files.FileColumns.DISPLAY_NAME)
            if (cursor.moveToFirst() && nameIndex != -1) {
                name = cursor.getString(nameIndex)
            }
        }
        return name
    }


    /**
     * CLEANUP BLUETOOTH RESOURCES ON EXIT
     */
    override fun onDestroy() {
        super.onDestroy()
        bluetoothManager.cleanup()
    }

    /**
     * ON CREATE — Main logic and initialization
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_file_sender)

        deviceRecyclerView = findViewById(R.id.deviceRecyclerView)
        previewTextView = findViewById(R.id.filePreview)
        val bluetoothSendFileButton = findViewById<Button>(R.id.SendFileBTN)

        // SETUP RECYCLERVIEW FOR FILE LISTING
        deviceRecyclerView.layoutManager = LinearLayoutManager(this)
        val fileAdapter = FileAdapter(fileList) { selected ->
            previewTextView.text = "Preview: ${selected.name}"
            selectedFile = selected
        }
        deviceRecyclerView.adapter = fileAdapter

        // INITIALIZE BLUETOOTH LOGIC
        bluetoothManager = BluetoothManager(this)

        bluetoothDeviceAdapter = BluetoothDeviceAdapter(bluetoothManager.getDiscoveredDevices()) { device ->
            selectedFile?.let { file ->
                val fileUri = Uri.fromFile(file)
                bluetoothManager.sendFileToDevice(device, fileUri, ::onBluetoothSendComplete)
            } ?: Toast.makeText(this, "No file selected to send.", Toast.LENGTH_SHORT).show()
        }



        val selectFileButton = findViewById<Button>(R.id.SelectFileBTN2)
        selectFileButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "*/*" // You can limit this to "application/pdf" etc.
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            filePickerLauncher.launch(intent)
        }


        bluetoothManager.onDeviceDiscovered = {
            runOnUiThread {
                bluetoothDeviceAdapter.notifyDataSetChanged()
            }
        }



        bluetoothManager.onDiscoveryFinished = {
            runOnUiThread { showBluetoothDevicesPopup() }
        }

        // SEND FILE VIA BLUETOOTH BUTTON
        bluetoothSendFileButton.setOnClickListener {
            if (selectedFile == null) {
                Toast.makeText(this, "Please select a file first.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

//            bluetoothDevices.clear()
            bluetoothDeviceAdapter.notifyDataSetChanged()

            startBluetoothDiscovery()
        }


        /**
         * AUTO-TRIGGER FILE LOAD IF INTENT EXTRA IS SET
         */
        val autoLoadFiles = intent.getBooleanExtra("AUTO_LOAD_FILES", false)
        if (autoLoadFiles) {
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
    }

    /**
     * CALLBACK FOR BLUETOOTH FILE TRANSFER STATUS
     */
    private fun onBluetoothSendComplete(success: Boolean, error: String?) {
        val message = if (success) {
            "File sent successfully!"
        } else {
            "Failed to send file: ${error ?: "Unknown error"}"
        }

        runOnUiThread {
            Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        }
    }

    /**
     * LOAD FILES FROM DEVICE MEDIASTORE
     */
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

    /**
     * CONVERT URI TO LOCAL FILE COPY
     */
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

    /**
     * DISPLAY POPUP OF DISCOVERED BLUETOOTH DEVICES
     */
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


    /**
     * HANDLE BLUETOOTH DEVICE DISCOVERY & PERMISSIONS
     */
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
