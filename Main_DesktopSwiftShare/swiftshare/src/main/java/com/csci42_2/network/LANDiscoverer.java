package com.csci42_2.network;

import com.csci42_2.util.Constants;

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class LANDiscoverer {

    public List<String> discoverDevices() {
        List<String> discoveredIPs = new ArrayList<>();

        try (DatagramChannel channel = DatagramChannel.open()) {
            channel.setOption(StandardSocketOptions.SO_BROADCAST, true);
            channel.bind(null);

            ByteBuffer buffer = ByteBuffer.wrap(Constants.DISCOVERY_MESSAGE.getBytes());
            InetSocketAddress broadcast = new InetSocketAddress("255.255.255.255", Constants.DISCOVERY_PORT);
            channel.send(buffer, broadcast);

            channel.configureBlocking(false);
            long start = System.currentTimeMillis();

            ByteBuffer receiveBuffer = ByteBuffer.allocate(1024);
            while (System.currentTimeMillis() - start < Constants.DISCOVERY_TIMEOUT_MS) {
                receiveBuffer.clear();
                SocketAddress addr = channel.receive(receiveBuffer);
                if (addr != null) {
                    receiveBuffer.flip();
                    String response = StandardCharsets.UTF_8.decode(receiveBuffer).toString().trim();
                    if (Constants.ACK_MESSAGE.equals(response)) {
                        String ip = ((InetSocketAddress) addr).getAddress().getHostAddress();
                        if (!discoveredIPs.contains(ip)) discoveredIPs.add(ip);
                    }
                }
                Thread.sleep(100);
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

        return discoveredIPs;
    }
}
