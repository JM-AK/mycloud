package com.geekbrains.gb.mycloud.controller;

import com.geekbraind.gb.mycloud.message.AuthRequestMsg;
import com.geekbrains.gb.mycloud.data.ClientMsgLib;
import com.geekbrains.gb.mycloud.util.ClientNetwork;
import com.geekbrains.gb.mycloud.util.WindowManager;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;


public class LoginController {

    @FXML
    private TextField loginField;

    @FXML
    private PasswordField passField;

    @FXML
    private void initialize() {
        loginField.setText("alex@example.com");
        passField.setText("123");
    }

    public void btnSignUp() {
        WindowManager.showRegister();
    }

    public void btnChangePassword() {
        WindowManager.showChangePassword();
    }

    public void sendAuthMsg() {
        if (loginField.getText().isEmpty() || passField.getText().isEmpty()) {
            WindowManager.showWarningAlert(ClientMsgLib.WRNG_NOT_ALL_DATA);
            return;
        }
        AuthRequestMsg msg = new AuthRequestMsg(loginField.getText(), passField.getText());
        ClientNetwork.getInstance().sendMsg(msg);
    }
}

