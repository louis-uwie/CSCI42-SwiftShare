package com.csci42_2;

import java.io.IOException;

import javax.bluetooth.RemoteDevice;

public class Device {
    String name = "";
    private String id = "";

    public Device (RemoteDevice rd) throws IOException {
        name = rd.getFriendlyName(false);
        id = rd.getBluetoothAddress();
    }

    @Override
    public String toString() {
        if (name.equals("")) {
            return "No Name";
        }
        return name;
    }

    public String getId () {
        return id;
    }
}
