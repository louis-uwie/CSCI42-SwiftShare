package com.csci42_2;

import java.io.IOException;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

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
    public void initialize() {

        mainVBox.setPrefSize(w, h);
        topAnchorPane.setMaxHeight(h*0.1);

        gridPane.setMaxSize(w,h);
        gridPane.setAlignment(Pos.CENTER);
 
        centerVBox.setPrefWidth(w/2);
 
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

    
}
