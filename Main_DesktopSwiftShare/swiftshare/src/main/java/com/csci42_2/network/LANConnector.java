package com.csci42_2.network;

import com.csci42_2.util.Constants;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;

public class LANConnector {

    public void connectToDevice(String ip) {
        try (SocketChannel channel = SocketChannel.open()) {
            channel.connect(new InetSocketAddress(ip, Constants.TCP_PORT));
            System.out.println("Connected to " + ip);

            ByteBuffer buffer = ByteBuffer.allocate(1024);
            channel.read(buffer);
            buffer.flip();
            System.out.println("Received: " + StandardCharsets.UTF_8.decode(buffer).toString());

            channel.write(ByteBuffer.wrap("Hi from Sender!".getBytes()));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
