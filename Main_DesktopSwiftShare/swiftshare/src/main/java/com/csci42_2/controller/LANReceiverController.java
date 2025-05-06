package com.csci42_2.controller;

import java.io.IOException;

import com.csci42_2.network.LANReceiver;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

public class LANReceiverController {

    @FXML private Button startListeningButton;
    @FXML private Label statusLabel;
    @FXML private TextArea logArea;
    @FXML private Button backButton;

    private Thread receiverThread;
    private LANReceiver receiver;
    private boolean isListening = false;

    @FXML
    private void initialize() {
        startListeningButton.setOnAction(e -> {
            if (!isListening) {
                startReceiver();
            } else {
                stopReceiver();
            }
        });

        backButton.setOnAction(e -> goBack());
    }

    private void startReceiver() {
        isListening = true;
        receiver = new LANReceiver() {
            @Override
            public void log(String message) {
                Platform.runLater(() -> logArea.appendText(message + "\n"));
            }
        };
        statusLabel.setText("Status: Listening...");
        startListeningButton.setText("❌ Cancel Listening");

        receiverThread = new Thread(receiver::start);
        receiverThread.setDaemon(true);
        receiverThread.start();
    }

    private void stopReceiver() {
        if (isListening && receiver != null) {
            receiver.stop();
            isListening = false;
            Platform.runLater(() -> {
                statusLabel.setText("Status: Not Listening");
                startListeningButton.setText("📡 Start Listening for LAN");
            });
        }
    }

    private void goBack() {
            stopReceiver();  // Ensure server is stopped on back
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/com/csci42_2/server_selector.fxml"));
            try {
                javafx.scene.Scene scene = new javafx.scene.Scene(loader.load());
                javafx.stage.Stage stage = (javafx.stage.Stage) backButton.getScene().getWindow();
                stage.setScene(scene);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
    }
}
