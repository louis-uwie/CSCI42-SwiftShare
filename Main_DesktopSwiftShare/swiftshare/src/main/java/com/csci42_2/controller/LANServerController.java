package com.csci42_2.controller;

import java.io.IOException;

import com.csci42_2.network.LANServer;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

public class LANServerController {

    @FXML private Button startListeningButton;
    @FXML private Label statusLabel;
    @FXML private TextArea logArea;
    @FXML private Button backButton;

    private Thread serverThread;
    private LANServer server;
    private boolean isListening = false;

    @FXML
    private void initialize() {
        startListeningButton.setOnAction(e -> {
            if (!isListening) {
                startServer();
            } else {
                stopServer();
            }
        });

        backButton.setOnAction(e -> goBack());
    }

    private void startServer() {
        isListening = true;
        server = new LANServer() {
            @Override
            public void log(String message) {
                Platform.runLater(() -> logArea.appendText(message + "\n"));
            }
        };
        statusLabel.setText("Status: Listening...");
        startListeningButton.setText("❌ Cancel Listening");

        serverThread = new Thread(server::start);
        serverThread.setDaemon(true);
        serverThread.start();
    }

    private void stopServer() {
        if (isListening && server != null) {
            server.stop();
            isListening = false;
            Platform.runLater(() -> {
                statusLabel.setText("Status: Not Listening");
                startListeningButton.setText("📡 Start Listening for LAN");
            });
        }
    }

    private void goBack() {
            stopServer();  // Ensure server is stopped on back
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
