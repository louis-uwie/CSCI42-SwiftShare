package com.csci42_2.util;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;

public class NetworkUtils {

    private static final int BUFFER_SIZE = 1024;

    // Text message utilities
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

    // Real file transfer logic
    public static void sendFile(SocketChannel channel, File file) throws IOException {
        String fileName = file.getName();
        long fileSize = file.length();

        // Header: [filename length][filename][file size]
        ByteBuffer header = ByteBuffer.allocate(4 + fileName.getBytes().length + 8);
        header.putInt(fileName.getBytes().length);
        header.put(fileName.getBytes());
        header.putLong(fileSize);
        header.flip();
        channel.write(header);

        // Body: file content
        try (FileInputStream fis = new FileInputStream(file)) {
            FileChannel fileChannel = fis.getChannel();
            long position = 0;
            while (position < fileSize) {
                long transferred = fileChannel.transferTo(position, fileSize - position, channel);
                if (transferred <= 0) break;
                position += transferred;
            }
        }
    }

    public static void receiveFile(SocketChannel channel, String saveDirectory) throws IOException {
        ByteBuffer headerBuffer = ByteBuffer.allocate(1024);
        channel.read(headerBuffer);
        headerBuffer.flip();

        int fileNameLength = headerBuffer.getInt();
        byte[] nameBytes = new byte[fileNameLength];
        headerBuffer.get(nameBytes);
        String fileName = new String(nameBytes, StandardCharsets.UTF_8);
        long fileSize = headerBuffer.getLong();

        File dir = new File(saveDirectory);
        if (!dir.exists()) dir.mkdirs();

        File outputFile = new File(dir, fileName);
        try (FileOutputStream fos = new FileOutputStream(outputFile)) {
            FileChannel outChannel = fos.getChannel();
            ByteBuffer buffer = ByteBuffer.allocate(8192);
            long remaining = fileSize;

            while (remaining > 0) {
                buffer.clear();
                int bytesRead = channel.read(buffer);
                if (bytesRead == -1) break;
                buffer.flip();
                outChannel.write(buffer);
                remaining -= bytesRead;
            }
        }
    }
}
