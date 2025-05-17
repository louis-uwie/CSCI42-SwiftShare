package com.csci42_2.network;

import com.csci42_2.util.*;

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.StandardCharsets;

public class LANServer {

    private volatile boolean running = false;
    private DatagramChannel udpChannel;
    private ServerSocketChannel tcpChannel;

    public void log(String message) {
        System.out.println(message);
    }

    public void start() {
        running = true;
        new Thread(this::startUDPListener, "UDP-Listener").start();
        new Thread(this::startTCPServer, "TCP-Server").start();
    }

    public void stop() {
        running = false;
        log("🔴 Stopped listening.");
    
        try {
            if (udpChannel != null && udpChannel.isOpen()) udpChannel.close();
            if (tcpChannel != null && tcpChannel.isOpen()) tcpChannel.close();
        } catch (IOException e) {
            log("⚠️ Error closing channels: " + e.getMessage());
        }
    }
    

    private void startUDPListener() {
        try (DatagramChannel channel = DatagramChannel.open()) {
            udpChannel = channel;
            channel.setOption(StandardSocketOptions.SO_REUSEADDR, true);
            channel.bind(new InetSocketAddress(Constants.DISCOVERY_PORT));
            log("📡 UDP listening on port " + Constants.DISCOVERY_PORT);
    
            ByteBuffer buffer = ByteBuffer.allocate(1024);
    
            while (running) {
                buffer.clear();
                SocketAddress sender = channel.receive(buffer);
                if (sender != null) {
                    buffer.flip();
                    String msg = StandardCharsets.UTF_8.decode(buffer).toString().trim();
                    if (Constants.DISCOVERY_MESSAGE.equals(msg)) {
                        buffer.clear();
                        buffer.put(Constants.ACK_MESSAGE.getBytes());
                        buffer.flip();
                        channel.send(buffer, sender);
                        log("✅ Discovery message received, ACK sent to " + sender);
                    }
                }
                Thread.sleep(100);
            }
        } catch (IOException | InterruptedException e) {
            if (running) log("❌ UDP Error: " + e.getMessage());
        }
    }
    

    private void startTCPServer() {
        try (ServerSocketChannel serverChannel = ServerSocketChannel.open()) {
            tcpChannel = serverChannel;
            serverChannel.bind(new InetSocketAddress(Constants.TCP_PORT));
            log("📥 TCP Server listening on port " + Constants.TCP_PORT);
    
            while (running) {
                SocketChannel clientChannel = serverChannel.accept();
                if (clientChannel != null) {
                    log("📶 TCP connection accepted from " + clientChannel.getRemoteAddress());
    
                    String hello = NetworkUtils.receiveMessage(clientChannel);
                    if ("HELLO_RECEIVER".equals(hello)) {
                        NetworkUtils.sendMessage(clientChannel, "ACK_RECEIVER");
    
                        String next = NetworkUtils.receiveMessage(clientChannel);
                        if ("SEND_FILE".equals(next)) {
                            NetworkUtils.receiveFile(clientChannel, Constants.RECEIVE_DIR);
                            log("📂 File saved to " + Constants.RECEIVE_DIR);
                        }
                    }
    
                    clientChannel.close();
                }
            }
        } catch (IOException e) {
            if (running) log("❌ TCP Error: " + e.getMessage());
        }
    }
}
