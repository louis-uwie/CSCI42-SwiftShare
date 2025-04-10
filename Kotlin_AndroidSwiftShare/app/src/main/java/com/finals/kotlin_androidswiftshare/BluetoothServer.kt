package com.finals.kotlin_androidswiftshare

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.net.Uri
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.*
import kotlin.concurrent.thread

/**
 * BluetoothServer is responsible for receiving files via Bluetooth.
 * It opens a server socket, accepts a client connection, and saves the received file to local storage.
 */
class BluetoothServer(private val context: Context) {

    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private var serverSocket: BluetoothServerSocket? = null

    // UUID must match the one used by the client
    private val APP_UUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    private val APP_NAME = "KotlinAndroidSwiftShare"

    /**
     * Starts the Bluetooth server to listen for incoming file transfers.
     *
     * @param onFileReceived Called when a file is successfully received with the saved file's URI.
     * @param onError Called if any error occurs during receiving.
     */
    @SuppressLint("MissingPermission")
    fun startServer(
        onFileReceived: (Uri) -> Unit,
        onError: (Exception) -> Unit
    ) {
        thread {
            try {
                serverSocket = bluetoothAdapter?.listenUsingRfcommWithServiceRecord(APP_NAME, APP_UUID)
                Log.d("BluetoothServer", "Waiting for connection...")

                val socket: BluetoothSocket = serverSocket?.accept() ?: throw Exception("Socket accept failed")
                Log.d("BluetoothServer", "Connection accepted from ${socket.remoteDevice.name}")

                val inputStream: InputStream = socket.inputStream
                val outputFile = File.createTempFile("received_", ".bin", context.cacheDir)

                val outputStream = FileOutputStream(outputFile)
                val buffer = ByteArray(1024)
                var bytesRead: Int

                while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                    outputStream.write(buffer, 0, bytesRead)
                }

                outputStream.flush()
                outputStream.close()
                inputStream.close()
                socket.close()
                serverSocket?.close()

                onFileReceived(Uri.fromFile(outputFile))

            } catch (e: Exception) {
                Log.e("BluetoothServer", "Error receiving file", e)
                onError(e)
                try {
                    serverSocket?.close()
                } catch (ex: Exception) {
                    Log.e("BluetoothServer", "Error closing server socket", ex)
                }
            }
        }
    }

    /**
     * Stops the server socket if running.
     */
    fun stopServer() {
        try {
            serverSocket?.close()
        } catch (e: Exception) {
            Log.e("BluetoothServer", "Error stopping server socket", e)
        }
    }
}

/*

  USAGE GUIDE

  --RECEIVING FILE GENERAL--

    val bluetoothServer = BluetoothServer(requireContext())

    bluetoothServer.startServer(
        onFileReceived = { uri ->
            Log.d("Frontend", "File received at: $uri")
            // Show download complete UI, open file, etc.
        },
        onError = { error ->
            Log.e("Frontend", "File receive failed: ${error.message}")
            Toast.makeText(context, "Failed to receive file", Toast.LENGTH_LONG).show()
        }
    )

  --STOP SERVER--

    override fun onDestroy() {
        super.onDestroy()
        bluetoothServer.stopServer()
    }

 */
