package com.csci42_2.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.control.Button;

import java.io.IOException;

public class ServerSelectorController {

    @FXML private Button lanButton;
    @FXML private Button bluetoothButton;
    @FXML private Button backButton;

    @FXML
    public void initialize() {
        lanButton.setOnAction(e -> loadReceiverScreen("/com/csci42_2/lan_server.fxml"));
        bluetoothButton.setOnAction(e -> loadReceiverScreen("/com/csci42_2/bluetooth_server.fxml"));
        backButton.setOnAction(e -> goBack());
    }

    private void loadReceiverScreen(String fxml) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            Scene scene = new Scene(loader.load());
            Stage stage = (Stage) lanButton.getScene().getWindow();
            stage.setScene(scene);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void goBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/csci42_2/role_selector.fxml"));
            Scene scene = new Scene(loader.load());
            Stage stage = (Stage) backButton.getScene().getWindow();
            stage.setScene(scene);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
