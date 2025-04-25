package com.csci42_2.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;

import java.io.IOException;

public class RoleSelectorController {

    @FXML private Button sendButton;
    @FXML private Button receiveButton;

    @FXML
    public void initialize() {
        sendButton.setOnAction(e -> loadScene("/com/csci42_2/sender.fxml"));
        receiveButton.setOnAction(e -> loadScene("/com/csci42_2/receiver.fxml")); // placeholder
    }

    private void loadScene(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Scene newScene = new Scene(loader.load());
            Stage stage = (Stage) sendButton.getScene().getWindow();
            stage.setScene(newScene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

