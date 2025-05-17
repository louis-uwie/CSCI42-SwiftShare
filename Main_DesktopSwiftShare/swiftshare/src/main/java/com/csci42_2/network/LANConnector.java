package com.csci42_2.network;

import com.csci42_2.util.Constants;
import com.csci42_2.util.NetworkUtils;

import java.io.IOException;
import java.io.File;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;

public class LANConnector {

    public void connectToDevice(String ip, File file) {
        try (SocketChannel channel = SocketChannel.open()) {
            channel.connect(new InetSocketAddress(ip, Constants.TCP_PORT));
            System.out.println("🌐 Connected to " + ip);

            // 1. Confirm identity
            NetworkUtils.sendMessage(channel, "HELLO_RECEIVER");
            String response = NetworkUtils.receiveMessage(channel);
            System.out.println("🗨 Received: " + response);

            if ("ACK_RECEIVER".equals(response)) {
                // 2. Proceed with file transfer
                NetworkUtils.sendMessage(channel, "SEND_FILE");
                NetworkUtils.sendFile(channel, file);
                System.out.println("📤 Stub file sent!");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
