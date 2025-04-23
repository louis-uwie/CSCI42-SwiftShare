package com.finals.kotlin_androidswiftshare

import java.io.*
import java.net.*
import kotlinx.coroutines.*

class LanManager {

    private val PORT = 8888
    private val bufferSize = 1024

    /**
     * Requests LAN access - could be used to prompt for permissions
     */
    fun requestLAN_Access() {
        // For Android 13+, permissions may be runtime depending on app purpose
        // You can leave this blank unless using NEARBY_WIFI_DEVICES
    }

    /**
     * Starts a server to listen for incoming LAN connections.
     */
    fun startServer(onFileReceived: (String) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val serverSocket = ServerSocket(PORT)
                println("Server started on port $PORT")
                while (true) {
                    val client = serverSocket.accept()
                    handleClient(client, onFileReceived)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun handleClient(socket: Socket, onFileReceived: (String) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val input = BufferedReader(InputStreamReader(socket.getInputStream()))
                val message = input.readLine()
                onFileReceived(message)
                socket.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    /**
     * Discovers and connects to a known device IP
     */
    fun sendFileTo(ip: String, message: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val socket = Socket(ip, PORT)
                val writer = BufferedWriter(OutputStreamWriter(socket.getOutputStream()))
                writer.write(message)
                writer.newLine()
                writer.flush()
                socket.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    /**
     * LAN Device discovery can be done via NSD or predefined IP scanning
     * Placeholder method
     */
    fun locateLAN_Devices() {
        // Later you can implement NSD (Network Service Discovery) or mDNS here
        println("Discovery logic not implemented yet.")
    }
}
