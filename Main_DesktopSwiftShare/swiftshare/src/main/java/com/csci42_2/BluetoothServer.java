package com.csci42_2;

import javax.bluetooth.DiscoveryAgent;
import javax.bluetooth.LocalDevice;
import javax.bluetooth.UUID;
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;
import javax.microedition.io.StreamConnectionNotifier;

public class BluetoothServer implements Runnable {
    private static final String serviceName = "Bluetooth Reciever";
    private static final String uuid = "1b00b86bd41144e492fa526bfddd8330";
    private UUID myUUID;
    private String url;

    public BluetoothServer () {
    }

    @Override
    public void run() {
        makeConnection();
    }

    private void makeConnection () {
        LocalDevice local = null;
        StreamConnectionNotifier notifier;
        StreamConnection conn = null;

        try {
            local = LocalDevice.getLocalDevice();
            local.setDiscoverable(DiscoveryAgent.GIAC);
            myUUID = new UUID(uuid, false);

            url = "btspp://localhost:" + myUUID.toString() + ";name=" + serviceName + ";authenticate=false";
            notifier = (StreamConnectionNotifier) Connector.open(url);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        while(true) {
            try {
                System.out.println("Waiting for Connections: " + url);
                conn = notifier.acceptAndOpen();
                System.out.println("Connection found");
                
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
        }
    }
}
