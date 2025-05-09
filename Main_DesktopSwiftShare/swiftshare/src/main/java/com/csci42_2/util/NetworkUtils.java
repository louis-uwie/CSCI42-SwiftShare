package com.csci42_2.util;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;

public class NetworkUtils {

    private static final int BUFFER_SIZE = 1024;

    public static void sendMessage(SocketChannel channel, String message) throws IOException {
        ByteBuffer buffer = ByteBuffer.wrap((message + "\n").getBytes(StandardCharsets.UTF_8));
        channel.write(buffer);
    }

    public static String receiveMessage(SocketChannel channel) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
        channel.read(buffer);
        buffer.flip();
        return StandardCharsets.UTF_8.decode(buffer).toString().trim();
    }

    public static void sendStubFile(SocketChannel channel) throws IOException {
        ByteBuffer buffer = ByteBuffer.wrap("This is a stub file.".getBytes(StandardCharsets.UTF_8));
        channel.write(buffer);
    }

    public static String receiveStubFile(SocketChannel channel) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
        channel.read(buffer);
        buffer.flip();
        return StandardCharsets.UTF_8.decode(buffer).toString().trim();
    }
}
