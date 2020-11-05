package com.geekbrains.gb.mycloud.controller;

import com.geekbraind.gb.mycloud.message.AuthRequestMsg;
import com.geekbrains.gb.mycloud.data.ClientMsgLib;
import com.geekbrains.gb.mycloud.service.AuthService;
import com.geekbrains.gb.mycloud.util.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.net.URL;
import java.util.ResourceBundle;


public class LoginController implements Initializable, AuthCallback {

    @FXML
    private TextField loginField;

    @FXML
    private PasswordField passField;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        ClientNetwork.getInstance().getMainClientHandler().setAuthCallback(this);
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

    @Override
    public void authCallback(boolean result) {
        if (result) {
            Platform.runLater(()->{
                WindowManager.showMain();
            });
        }
    }
}

