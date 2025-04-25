package com.csci42_2;

import java.io.*;
import java.net.*;
import java.nio.*;
import java.nio.channels.*;
import java.nio.charset.StandardCharsets;

public class LANSender {

    private static final int DISCOVERY_PORT = 8888;
    private static final int TCP_PORT = 9999;
    private static final String DISCOVERY_MESSAGE = "HELLO_FILESHARE";
    private static final String ACK_MESSAGE = "ACK_FILESHARE";
    private static final int TIMEOUT = 5000; // ms

    public static void main(String[] args) {
        new LANSender().start();
    }

    public void start() {
        String receiverIP = discoverReceiver();
        if (receiverIP != null) {
            connectToReceiver(receiverIP);
        } else {
            System.out.println("Sender: No response received.");
        }
    }

    private String discoverReceiver() {
        try (DatagramChannel channel = DatagramChannel.open()) {
            channel.setOption(StandardSocketOptions.SO_BROADCAST, true);
            channel.bind(null); // auto-bind to any available port

            ByteBuffer buffer = ByteBuffer.wrap(DISCOVERY_MESSAGE.getBytes());
            InetSocketAddress broadcast = new InetSocketAddress("255.255.255.255", DISCOVERY_PORT);
            channel.send(buffer, broadcast);

            System.out.println("Sender: Broadcasted discovery message.");

            // Set timeout
            channel.configureBlocking(false);
            long startTime = System.currentTimeMillis();

            ByteBuffer receiveBuffer = ByteBuffer.allocate(1024);
            while (System.currentTimeMillis() - startTime < TIMEOUT) {
                receiveBuffer.clear();
                SocketAddress responseAddr = channel.receive(receiveBuffer);
                if (responseAddr != null) {
                    receiveBuffer.flip();
                    String response = StandardCharsets.UTF_8.decode(receiveBuffer).toString().trim();
                    if (ACK_MESSAGE.equals(response)) {
                        String ip = ((InetSocketAddress) responseAddr).getAddress().getHostAddress();
                        System.out.println("Sender: Received ACK from " + ip);
                        return ip;
                    }
                }
                Thread.sleep(100); // Polling interval
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void connectToReceiver(String ip) {
        try (SocketChannel channel = SocketChannel.open()) {
            channel.connect(new InetSocketAddress(ip, TCP_PORT));
            System.out.println("Sender: Connected to receiver at " + ip);

            // Optional: read READY
            ByteBuffer buffer = ByteBuffer.allocate(1024);
            channel.read(buffer);
            buffer.flip();
            String ready = StandardCharsets.UTF_8.decode(buffer).toString();
            System.out.println("Sender: Received: " + ready);

            // Send a test message
            ByteBuffer msg = ByteBuffer.wrap("Hi from Sender!".getBytes());
            channel.write(msg);

            channel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
