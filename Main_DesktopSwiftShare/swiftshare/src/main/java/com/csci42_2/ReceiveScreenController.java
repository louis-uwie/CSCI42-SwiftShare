package com.csci42_2;

import java.io.IOException;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import javax.bluetooth.*;

public class ReceiveScreenController {

    double w = 1024;
    double h = 768;

    @FXML
    VBox mainVBox;
    @FXML
    AnchorPane topAnchorPane;
    @FXML
    GridPane gridPane;
    @FXML
    VBox centerVBox;
    @FXML
    Text deviceIdText;
    @FXML
    Text deviceNameText;

    @FXML
    public void initialize() throws BluetoothStateException {

        LocalDevice localDevice = LocalDevice.getLocalDevice();

        mainVBox.setPrefSize(w, h);
        topAnchorPane.setMaxHeight(h*0.1);

        gridPane.setMaxSize(w,h);
        gridPane.setAlignment(Pos.CENTER);
 
        centerVBox.setPrefWidth(w/2);

        deviceNameText.setText("Device Name: " + localDevice.getFriendlyName());
        deviceIdText.setText("Device ID: " + localDevice.getBluetoothAddress());
 
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
    private void StartConnection() {
        BluetoothServer server = new BluetoothServer();
        server.run();
    }
}
