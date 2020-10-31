package com.geekbrains.gb.mycloud.controller;

import com.geekbraind.gb.mycloud.message.AuthRequestMsg;
import com.geekbrains.gb.mycloud.data.ClientMsgLib;
import com.geekbrains.gb.mycloud.service.AuthService;
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

    private void initialize() {
        loginField.setText("alex@example.com");
        passField.setText("123");
    }

    @FXML
    public void btnRegister() {
        WindowManager.showRegister();
    }

    @FXML
    public void btnChangePassword() {
        WindowManager.showChangePassword();
    }

    @FXML
    public void sendAuthMsg() {
        if (loginField.getText().isEmpty() || passField.getText().isEmpty()) {
            WindowManager.showWarningAlert(ClientMsgLib.WRNG_NOT_ALL_DATA);
            return;
        }
        if (!AuthService.getInstance().isLoginValid(loginField.getText())) {
            WindowManager.showWarningAlert(ClientMsgLib.WRNG_LOGIN_TYPING);
            return;
        }

        AuthRequestMsg msg = new AuthRequestMsg(loginField.getText(), passField.getText());
        ClientNetwork.getInstance().sendObject(msg);
    }
}

