package com.csci42_2.controller;

import com.csci42_2.network.LANConnector;
import com.csci42_2.network.LANDiscoverer;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class SenderController {

    @FXML private Button scanButton;
    @FXML private Button connectButton;
    @FXML private Button cancelButton;
    @FXML private Button backButton;  // Add back button
    @FXML private ListView<String> deviceListView;
    @FXML private Label fileInfoLabel;

    private final LANDiscoverer discoverer = new LANDiscoverer();
    private final LANConnector connector = new LANConnector();
    private Task<List<String>> scanTask;

    private File selectedFile;

    @FXML
    public void initialize() {
        connectButton.setDisable(true);
        backButton.setDisable(false);  // Initially, backButton should be enabled

        deviceListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            connectButton.setDisable(newVal == null);
        });

        scanButton.setOnAction(e -> scanForDevices());
        connectButton.setOnAction(e -> connectToDevice());
        cancelButton.setOnAction(e -> cancelScan());
        backButton.setOnAction(e -> backToFiles());
    }

    private void scanForDevices() {
        scanButton.setDisable(true);
        backButton.setDisable(true);  // Disable back button during device discovery
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
            backButton.setDisable(false);  // Enable back button after scan
        });

        scanTask.setOnFailed(e -> {
            deviceListView.getItems().add("Discovery failed.");
            scanButton.setDisable(false);
            backButton.setDisable(false);  // Enable back button after failure
        });

        new Thread(scanTask).start();
    }

    private void connectToDevice() {
        String ip = deviceListView.getSelectionModel().getSelectedItem();
        if (ip != null) {
            new Thread(() -> connector.connectToDevice(ip)).start();
        }
    }

    public void setSelectedFile(File file) {
        this.selectedFile = file;
        if (fileInfoLabel != null && file != null) {
            fileInfoLabel.setText("Selected file: " + file.getName());
        }
    }

    private void cancelScan() {
        if (scanTask != null && scanTask.isRunning()) {
            scanTask.cancel();
        }

        // Reset the UI
        scanButton.setDisable(false);
        backButton.setDisable(false);  // Enable back button after cancellation

        // Clear device list and show cancellation message
        deviceListView.getItems().clear();
        deviceListView.getItems().add("Discovery cancelled.");
    }

    private void backToFiles() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/csci42_2/file_selector.fxml"));
            Scene scene = new Scene(loader.load());
            Stage stage = (Stage) backButton.getScene().getWindow();
            stage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
