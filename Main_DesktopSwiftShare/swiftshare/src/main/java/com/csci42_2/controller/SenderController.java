package com.csci42_2.controller;

import com.csci42_2.network.LANConnector;
import com.csci42_2.network.LANDiscoverer;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;

public class SenderController {

    @FXML private Button scanButton;
    @FXML private Button connectButton;
    @FXML private Button cancelButton;
    @FXML private ListView<String> deviceListView;

    private final LANDiscoverer discoverer = new LANDiscoverer();
    private final LANConnector connector = new LANConnector();
    private Task<List<String>> scanTask;

    @FXML
    public void initialize() {
        connectButton.setDisable(true);

        deviceListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            connectButton.setDisable(newVal == null);
        });

        scanButton.setOnAction(e -> scanForDevices());
        connectButton.setOnAction(e -> connectToSelectedDevice());
        cancelButton.setOnAction(e -> cancelAndReturn());
    }

    private void scanForDevices() {
        scanButton.setDisable(true);
        deviceListView.getItems().clear();

        scanTask = new Task<>() {
            @Override
            protected List<String> call() {
                return discoverer.discoverDevices();
            }
        };

        scanTask.setOnSucceeded(e -> {
            deviceListView.getItems().addAll(scanTask.getValue());
            scanButton.setDisable(false);
        });

        scanTask.setOnFailed(e -> {
            deviceListView.getItems().add("Discovery failed.");
            scanButton.setDisable(false);
        });

        new Thread(scanTask).start();
    }

    private void connectToSelectedDevice() {
        String ip = deviceListView.getSelectionModel().getSelectedItem();
        if (ip != null) {
            new Thread(() -> connector.connectToDevice(ip)).start();
        }
    }

    private void cancelAndReturn() {
        if (scanTask != null && scanTask.isRunning()) {
            scanTask.cancel();
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/csci42_2/role_selector.fxml"));
            Scene scene = new Scene(loader.load());
            Stage stage = (Stage) cancelButton.getScene().getWindow();
            stage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
