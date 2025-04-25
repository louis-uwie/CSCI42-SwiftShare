package com.csci42_2.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.*;

import java.io.File;
import java.io.IOException;

public class FileSelectorController {

    @FXML private Label fileLabel;
    @FXML private Button browseButton;
    @FXML private Button nextButton;
    @FXML private Button backButton;

    private File selectedFile;

    @FXML
    public void initialize() {
        browseButton.setOnAction(e -> browseFile());
        nextButton.setOnAction(e -> goToDiscovery());
        backButton.setOnAction(e -> goBack());
    }

    private void browseFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select a File to Send");
        selectedFile = fileChooser.showOpenDialog(browseButton.getScene().getWindow());

        if (selectedFile != null) {
            fileLabel.setText("Selected: " + selectedFile.getName());
            nextButton.setDisable(false);
        } else {
            fileLabel.setText("No file selected");
            nextButton.setDisable(true);
        }
    }

    private void goToDiscovery() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/csci42_2/sender.fxml"));
            Scene senderScene = new Scene(loader.load());

            // Pass file to SenderController
            SenderController controller = loader.getController();
            controller.setSelectedFile(selectedFile);

            Stage stage = (Stage) nextButton.getScene().getWindow();
            stage.setScene(senderScene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void goBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/csci42_2/role_selector.fxml"));
            Scene scene = new Scene(loader.load());
            Stage stage = (Stage) backButton.getScene().getWindow();
            stage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
