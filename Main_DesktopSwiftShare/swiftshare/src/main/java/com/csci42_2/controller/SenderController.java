package com.csci42_2.controller;

import com.csci42_2.network.LANConnector;
import com.csci42_2.network.LANDiscoverer;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.util.List;

public class SenderController {

    @FXML private Button scanButton;
    @FXML private Button connectButton;
    @FXML private ListView<String> deviceListView;

    private final LANDiscoverer discoverer = new LANDiscoverer();
    private final LANConnector connector = new LANConnector();

    @FXML
    public void initialize() {
        connectButton.setDisable(true);

        deviceListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            connectButton.setDisable(newVal == null);
        });

        scanButton.setOnAction(e -> scanForDevices());
        connectButton.setOnAction(e -> connectToSelectedDevice());
    }

    private void scanForDevices() {
        scanButton.setDisable(true);
        deviceListView.getItems().clear();

        Task<List<String>> task = new Task<>() {
            @Override
            protected List<String> call() {
                return discoverer.discoverDevices();
            }
        };

        task.setOnSucceeded(e -> {
            deviceListView.getItems().addAll(task.getValue());
            scanButton.setDisable(false);
        });

        task.setOnFailed(e -> {
            deviceListView.getItems().add("Discovery failed.");
            scanButton.setDisable(false);
        });

        new Thread(task).start();
    }

    private void connectToSelectedDevice() {
        String ip = deviceListView.getSelectionModel().getSelectedItem();
        if (ip != null) {
            new Thread(() -> connector.connectToDevice(ip)).start();
        }
    }
}
