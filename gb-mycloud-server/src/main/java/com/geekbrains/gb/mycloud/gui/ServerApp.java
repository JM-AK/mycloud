package com.geekbrains.gb.mycloud.gui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class ServerApp extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root= FXMLLoader.load(getClass().getClassLoader().getResource("server.fxml"));
        primaryStage.setTitle("Cloud server Admin Console");
        primaryStage.setScene(new Scene(root, 600,300));
        primaryStage.setAlwaysOnTop(true);
        primaryStage.show();
    }
}
