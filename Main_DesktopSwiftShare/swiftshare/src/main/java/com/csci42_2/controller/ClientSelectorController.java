package com.csci42_2.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;

public class ClientSelectorController {

    @FXML private Button lanButton;
    @FXML private Button bluetoothButton;
    @FXML private Button backButton;
    @FXML private Label fileLabel;

    private File selectedFile;

    public void setSelectedFile(File file) {
        this.selectedFile = file;
        if (fileLabel != null) {
            fileLabel.setText("Selected file: " + file.getName());
        }
    }

    @FXML
    public void initialize() {
        if (selectedFile != null && fileLabel != null) {
            fileLabel.setText("Selected file: " + selectedFile.getName());
        }

        lanButton.setOnAction(e -> loadLANSender());
        bluetoothButton.setOnAction(e -> loadBluetoothSender());
        backButton.setOnAction(e -> goBack());
    }

    private void loadLANSender() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/csci42_2/lan_client.fxml"));
            Scene scene = new Scene(loader.load());

            LANClientController controller = loader.getController();
            controller.setSelectedFile(selectedFile);

            Stage stage = (Stage) lanButton.getScene().getWindow();
            stage.setScene(scene);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void loadBluetoothSender() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/csci42_2/bt_client.fxml"));
            Scene scene = new Scene(loader.load());

            BTClientController controller = loader.getController();
            // controller.setSelectedFile(selectedFile); for when the BTClient works better.

            Stage stage = (Stage) bluetoothButton.getScene().getWindow();
            stage.setScene(scene);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void goBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/csci42_2/file_selector.fxml"));
            Scene scene = new Scene(loader.load());
            Stage stage = (Stage) backButton.getScene().getWindow();
            stage.setScene(scene);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
