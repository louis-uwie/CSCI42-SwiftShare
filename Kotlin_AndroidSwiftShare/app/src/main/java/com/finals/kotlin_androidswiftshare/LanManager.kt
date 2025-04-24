package com.finals.kotlin_androidswiftshare

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.widget.Toast
import kotlinx.coroutines.*
import java.io.*
import java.net.*

class LanManager(private val context: Context) {

    private val PORT = 8888
    private val bufferSize = 4096

    /**
     * Checks if the device is connected to a LAN (via Wi-Fi)
     */
    fun requestLAN_Access() {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(network)

        val isWifi = capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true

        if (!isWifi) {
            Toast.makeText(context, "Please connect to a Wi-Fi network for LAN access.", Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(context, "LAN access ready. Connected via Wi-Fi.", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Starts a server socket to listen for incoming files/messages.
     */
    fun startServer(onFileReceived: (fileName: String, content: ByteArray) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val serverSocket = ServerSocket(PORT)
                println("LAN Server running on port $PORT")

                while (true) {
                    val clientSocket = serverSocket.accept()
                    handleClient(clientSocket, onFileReceived)
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun handleClient(socket: Socket, onFileReceived: (String, ByteArray) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val input = DataInputStream(socket.getInputStream())
                val fileName = input.readUTF()
                val fileLength = input.readInt()
                val fileBytes = ByteArray(fileLength)
                input.readFully(fileBytes)

                onFileReceived(fileName, fileBytes)

                input.close()
                socket.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    /**
     * Sends a file to a target LAN IP.
     */
    fun sendFileTo(ip: String, file: File) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val socket = Socket(ip, PORT)
                val output = DataOutputStream(socket.getOutputStream())

                val fileBytes = file.readBytes()
                output.writeUTF(file.name)
                output.writeInt(fileBytes.size)
                output.write(fileBytes)

                output.flush()
                output.close()
                socket.close()

                println("File sent to $ip successfully!")

            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    /**
     * Placeholder for device discovery. You can implement mDNS or NSD later.
     */
    fun locateLAN_Devices() {
        println("LAN device discovery not implemented.")
    }
}
