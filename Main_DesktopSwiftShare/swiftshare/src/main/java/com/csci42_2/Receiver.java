package com.csci42_2;

import java.io.*;
import java.net.*;
import java.nio.*;
import java.nio.channels.*;
import java.nio.charset.StandardCharsets;

public class Receiver {

    private static final int DISCOVERY_PORT = 8888;
    private static final int TCP_PORT = 9999;
    private static final String DISCOVERY_MESSAGE = "HELLO_FILESHARE";
    private static final String ACK_MESSAGE = "ACK_FILESHARE";

    public static void main(String[] args) {
        new Receiver().start();
    }

    public void start() {
        new Thread(this::listenForDiscovery).start();
        new Thread(this::startTCPServer).start();
    }

    private void listenForDiscovery() {
        try (DatagramChannel channel = DatagramChannel.open()) {
            channel.bind(new InetSocketAddress(DISCOVERY_PORT));
            ByteBuffer buffer = ByteBuffer.allocate(1024);

            System.out.println("Receiver: Listening for discovery messages...");

            while (true) {
                buffer.clear();
                SocketAddress senderAddr = channel.receive(buffer);
                buffer.flip();

                String msg = StandardCharsets.UTF_8.decode(buffer).toString().trim();
                if (DISCOVERY_MESSAGE.equals(msg)) {
                    InetSocketAddress sender = (InetSocketAddress) senderAddr;
                    System.out.println("Receiver: Discovery received from " + sender.getAddress().getHostAddress());

                    // Send ACK
                    ByteBuffer ackBuffer = ByteBuffer.wrap(ACK_MESSAGE.getBytes());
                    channel.send(ackBuffer, senderAddr);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void startTCPServer() {
        try (ServerSocketChannel serverChannel = ServerSocketChannel.open()) {
            serverChannel.bind(new InetSocketAddress(TCP_PORT));
            System.out.println("Receiver: TCP server started on port " + TCP_PORT);

            while (true) {
                SocketChannel client = serverChannel.accept();
                System.out.println("Receiver: TCP connection established with " + client.getRemoteAddress());

                // Optional: send READY
                ByteBuffer ready = ByteBuffer.wrap("READY".getBytes());
                client.write(ready);

                // Handle client (just echo for now)
                ByteBuffer buffer = ByteBuffer.allocate(1024);
                client.read(buffer);
                buffer.flip();
                String message = StandardCharsets.UTF_8.decode(buffer).toString();
                System.out.println("Receiver: Received message: " + message);

                client.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

