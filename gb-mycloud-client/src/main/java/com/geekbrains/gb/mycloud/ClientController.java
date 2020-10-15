package com.geekbrains.gb.mycloud;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

public class ClientController {
    @FXML
    public GridPane loginArea;
    @FXML
    public Button login;

    @FXML
    public void showLoginArea(ActionEvent actionEvent) {
        loginArea.setVisible(true);
    }

    @FXML
    public void exit(ActionEvent actionEvent) {
        System.out.println("Exit");
        Stage stage = (Stage) login.getScene().getWindow();
        stage.close();
    }

    @FXML
    public void downloadFile(ActionEvent actionEvent) {
        System.out.println("load file");
    }

    @FXML
    public void uploadFile(ActionEvent actionEvent) {
        System.out.println("upload file");
    }

    @FXML
    public void renameFile(ActionEvent actionEvent) {
        System.out.println("rename file");
    }

    @FXML
    public void deleteFile(ActionEvent actionEvent) {
        System.out.println("delete file");
    }

    @FXML
    public void loginTry(ActionEvent actionEvent) {
        System.out.println("Try login");
        loginArea.setVisible(false);
    }
}
