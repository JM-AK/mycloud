package com.geekbrains.gb.mycloud.controller;

import com.geekbraind.gb.mycloud.dictionary.Command;
import com.geekbraind.gb.mycloud.message.CommandMsg;
import com.geekbraind.gb.mycloud.util.CheckEmail;
import com.geekbrains.gb.mycloud.data.ClientMsgLib;
import com.geekbrains.gb.mycloud.service.AuthService;
import com.geekbrains.gb.mycloud.util.ClientNetwork;
import com.geekbrains.gb.mycloud.util.WindowManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class RegisterController {

    @FXML
    public TextField nameField;
    @FXML
    public TextField loginField;
    @FXML
    public PasswordField passFieldPrimary;
    @FXML
    public PasswordField passFieldSecondary;

    @FXML
    public void btnBack(ActionEvent actionEvent) {
        WindowManager.showLogin();
    }

    @FXML
    public void btnRegister(ActionEvent actionEvent) {
        if (nameField.getText().isEmpty() || loginField.getText().isEmpty() || passFieldPrimary.getText().isEmpty()) {
            WindowManager.showWarningAlert(ClientMsgLib.WRNG_NOT_ALL_DATA);
            return;
        }
        if (!passFieldPrimary.getText().equals(passFieldSecondary.getText())) {
            WindowManager.showWarningAlert(ClientMsgLib.WRNG_DONT_MATCH);
            return;
        }
        if (!CheckEmail.isEmail(loginField.getText())) {
            WindowManager.showWarningAlert(ClientMsgLib.WRNG_LOGIN_TYPING);
            return;
        }

        CommandMsg cmdMsg = new CommandMsg(Command.REGISTER, loginField.getText(), passFieldPrimary.getText(), nameField.getText());
        ClientNetwork.getInstance().sendObject(cmdMsg);
    }
}
