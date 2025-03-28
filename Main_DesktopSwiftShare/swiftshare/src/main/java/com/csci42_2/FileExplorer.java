package com.csci42_2;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import java.io.File;

public class FileExplorer extends Application {
    private TreeView<File> fileTree;
    private ListView<File> fileList;
    private Label selectedFileLabel;

    @Override
    public void start(Stage primaryStage) {
        BorderPane root = new BorderPane();

        // TreeView for directory navigation
        fileTree = new TreeView<>();
        TreeItem<File> rootNode = createNode(new File(System.getProperty("user.home")));
        rootNode.setExpanded(true);
        fileTree.setRoot(rootNode);
        fileTree.setShowRoot(true);

        // ListView for file display
        fileList = new ListView<>();
        fileList.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        
        // Label to show selected file
        selectedFileLabel = new Label("Selected file: None");

        // Handle directory selection
        fileTree.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                updateFileList(newVal.getValue());
            }
        });

        // Handle file selection
        fileList.setOnMouseClicked(event -> {
            File selectedFile = fileList.getSelectionModel().getSelectedItem();
            if (selectedFile != null) {
                selectedFileLabel.setText("Selected file: " + selectedFile.getAbsolutePath());
            }
        });

        // Layout
        root.setLeft(new ScrollPane(fileTree));
        root.setCenter(new ScrollPane(fileList));
        root.setBottom(selectedFileLabel);

        Scene scene = new Scene(root, 600, 400);
        primaryStage.setTitle("Custom File Explorer");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    // Recursively create a TreeItem node for directories
    private TreeItem<File> createNode(File file) {
        TreeItem<File> node = new TreeItem<>(file);
        node.setExpanded(false); // Start collapsed
    
        // If it's a directory, load subdirectories lazily
        if (file.isDirectory()) {
            node.getChildren().add(new TreeItem<>(new File("Loading..."))); // Placeholder
            node.addEventHandler(TreeItem.branchExpandedEvent(), event -> {
                if (node.getChildren().size() == 1 && node.getChildren().get(0).getValue().getName().equals("Loading...")) {
                    node.getChildren().clear();
                    for (File subFile : file.listFiles(File::isDirectory)) {
                        node.getChildren().add(createNode(subFile));
                    }
                }
            });
        }
        return node;
    }
    

    // Update the ListView with files in the selected directory
    private void updateFileList(File directory) {
        fileList.getItems().clear();
        File[] files = directory.listFiles(File::isFile);
        if (files != null) {
            fileList.getItems().addAll(files);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
