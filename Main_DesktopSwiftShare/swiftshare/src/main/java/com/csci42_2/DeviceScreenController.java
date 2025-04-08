package com.csci42_2;

import java.io.File;
import java.io.IOException;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import javax.bluetooth.*;

import java.util.ArrayList;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.ListView;

public class DeviceScreenController {

    double w = 1024;
    double h = 768;

    ArrayList<Device> knownDevices = new ArrayList<>();
    ArrayList<Device> discoveredDevices = new ArrayList<>();
    ArrayList<Service> services = new ArrayList<>();

    Device currentDevice;

    @FXML
    VBox mainVBox;
    @FXML
    AnchorPane topAnchorPane;
    @FXML
    GridPane gridPane;
    @FXML
    VBox centerVBox;
    @FXML
    VBox knownVBox;
    @FXML
    VBox discoverVBox;
    @FXML
    Text deviceIdText;
    @FXML
    Text deviceNameText;

    ListView<Device> knownDeviceList;
    ListView<Device> discoveredDeviceList;
    ListView<Service> serviceList;
    
    @FXML
    public void initialize() throws BluetoothStateException {

        knownDeviceList = new ListView<>();
        discoveredDeviceList = new ListView<>();
        serviceList = new ListView<>();
        knownVBox.getChildren().add(knownDeviceList);
        discoverVBox.getChildren().add(serviceList);

        LocalDevice localDevice = LocalDevice.getLocalDevice();

        mainVBox.setPrefSize(w, h);
        topAnchorPane.setMaxHeight(h*0.1);

        gridPane.setMaxSize(w,h);
        gridPane.setAlignment(Pos.CENTER);
 
        centerVBox.setPrefWidth(w/4);

        deviceNameText.setText("Device Name: " + localDevice.getFriendlyName());
        deviceIdText.setText("Device ID: " + localDevice.getBluetoothAddress());

        GetKnownDevices();
        DiscoverDevices();
        addListeners();
 
    }
    
    @FXML
    private void Accept() throws IOException {
        App.setRoot("home");
    }

    @FXML
    private void Reject() throws IOException {
        App.setRoot("home");
    }

    @FXML
    private void SettingsButton() throws IOException {
        System.out.println("Settings Button Pressed");
    }

    @FXML
    private void GetKnownDevices() throws BluetoothStateException {
        LocalDevice localDevice = null;
        DiscoveryAgent discoveryAgent = null;
        localDevice = LocalDevice.getLocalDevice();
        discoveryAgent = localDevice.getDiscoveryAgent();
   	 
    	System.out.println("Local Bluetooth Device: " + localDevice.getFriendlyName());
   	 
    	RemoteDevice[] devices = discoveryAgent.retrieveDevices(DiscoveryAgent.PREKNOWN);

        if (devices != null) {
            for (RemoteDevice rd : devices) {
                try {
                    knownDevices.add(new Device(rd));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        
        if (knownDevices != null) {
            knownDeviceList.getItems().clear();
            knownDeviceList.getItems().addAll(knownDevices);
        }
        else {
            System.out.println("no known devices");
        }
    
    }

    @FXML
    public void DiscoverDevices() throws BluetoothStateException {
        DiscoveryListener discoveryListener;

        LocalDevice localDevice = null;
        DiscoveryAgent discoveryAgent = null;
        discoveryListener = new DeviceListener();
        localDevice = LocalDevice.getLocalDevice();
        discoveryAgent = localDevice.getDiscoveryAgent();
    
        try {
            System.out.println("Discovering Devices");
            discoveryAgent.startInquiry(DiscoveryAgent.GIAC, discoveryListener);
        } catch (Exception e) {
            discoveryAgent.cancelInquiry(discoveryListener);
        }

	}

    private class DeviceListener implements DiscoveryListener {
        @Override
        public void deviceDiscovered(RemoteDevice btDevice, DeviceClass cod) {
            try {
                discoveredDevices.add(new Device(btDevice));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        @Override
        public void inquiryCompleted(int discType) {
            if (discoveredDevices != null) {
                discoveredDeviceList.getItems().clear();
                discoveredDeviceList.getItems().addAll(discoveredDevices);
            } 
            else {
                System.out.println("no devices");
            }
        }
        @Override
        public void servicesDiscovered(int transID, ServiceRecord[] servRecord) {
            for (ServiceRecord sr : servRecord) {
                services.add(new Service(sr));
            }
            serviceList.getItems().clear();
            serviceList.getItems().addAll(services);
        }
        @Override
        public void serviceSearchCompleted(int transID, int respCode) {}
    }


    private void addListeners() {
        knownDeviceList.setOnMouseClicked(event -> {
            Device device = knownDeviceList.getSelectionModel().getSelectedItem();
            if (device != null) {
                deviceNameText.setText("Device Name: " + device);
                deviceIdText.setText("Device ID: " + device.getId());
            }
        });
        discoveredDeviceList.setOnMouseClicked(event -> {
            Device device = discoveredDeviceList.getSelectionModel().getSelectedItem();
            if (device != null) {
                deviceNameText.setText("Device Name: " + device);
                deviceIdText.setText("Device ID: " + device.getId());
            }
        });
    }
}
