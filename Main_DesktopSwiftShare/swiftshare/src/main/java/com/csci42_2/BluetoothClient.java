package com.csci42_2;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import javax.bluetooth.RemoteDevice;
import javax.microedition.io.StreamConnection;

public class BluetoothClient {
    private StreamConnection connection;
    private DataInputStream in;
    private DataOutputStream out;
    String deviceName;
    byte[] data;

    public BluetoothClient (StreamConnection conn) {
        connection = conn;
        try {
            deviceName = RemoteDevice.getRemoteDevice(connection).getFriendlyName(false);
        } catch (Exception e) {
            deviceName = "No Name";
        }
    }
    
    public void run() {
        try {
            in = connection.openDataInputStream();
            out = connection.openDataOutputStream();
            ReadFile();
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
    }

    private void ReadFile() {
        if (in != null) {
            try {
                data = in.readAllBytes();
                System.out.println("File received");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void sendFile(File f) throws IOException {
        if (f != null) {
            try {
                byte[] fileBytes = Files.readAllBytes(f.toPath());
                out.write(fileBytes);
                System.out.println("File sent");
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
        }
    }
}
