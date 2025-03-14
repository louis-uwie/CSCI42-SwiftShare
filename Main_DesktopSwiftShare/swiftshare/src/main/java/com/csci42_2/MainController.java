package com.csci42_2;

import java.io.IOException;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

public class MainController {

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
    Text appDescription;

    @FXML
    public void initialize() {

        mainVBox.setPrefSize(w, h);
        topAnchorPane.setMaxHeight(h*0.1);

        gridPane.setMaxSize(w,h);
        gridPane.setAlignment(Pos.CENTER);
 
        centerVBox.setPrefWidth(w/2);
        appDescription.setWrappingWidth(w/2);
        appDescription.setText("SwiftShare is a cross-platform offline file transfer application designed to enable seamless file sharing between Windows, macOS, Linux, Android, and iOS devices.");
 
    }
    
    @FXML
    private void SendButton() throws IOException {
        App.setRoot("sendScreen");
    }

    @FXML
    private void ReceiveButton() throws IOException {
        App.setRoot("recieveScreen");
    }

    @FXML
    private void SettingsButton() throws IOException {
        System.out.println("Settings Button Pressed");
    }

    
}
