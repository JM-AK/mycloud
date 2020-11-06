package com.geekbrains.gb.mycloud.controller;

import com.geekbraind.gb.mycloud.dictionary.Command;
import com.geekbraind.gb.mycloud.message.CommandMsg;
import com.geekbrains.gb.mycloud.data.ClientMsgLib;
import com.geekbrains.gb.mycloud.util.ChangePassCallback;
import com.geekbrains.gb.mycloud.util.ClientNetwork;
import com.geekbrains.gb.mycloud.util.WindowManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.net.URL;
import java.util.ResourceBundle;

public class ChangePassController implements Initializable, ChangePassCallback {
    @FXML
    public TextField loginField;
    @FXML
    public PasswordField passOldField;
    @FXML
    public PasswordField passNewFieldPrimary;
    @FXML
    public PasswordField passNewFieldSecondary;

    @FXML
    public void btnBack(ActionEvent actionEvent) {
        WindowManager.showLogin();
    }

    @FXML
    public void btnChangePassword(ActionEvent actionEvent) {
        if (loginField.getText().isEmpty() || passOldField.getText().isEmpty() ||
                passNewFieldPrimary.getText().isEmpty() || passNewFieldSecondary.getText().isEmpty()) {
            WindowManager.showWarningAlert(ClientMsgLib.WRNG_NOT_ALL_DATA);
            return;
        }
        if (!passNewFieldPrimary.getText().equals(passNewFieldSecondary.getText())) {
            WindowManager.showWarningAlert(ClientMsgLib.WRNG_DONT_MATCH);
            return;
        }
        if (passOldField.getText().equals(passNewFieldPrimary.getText())) {
            WindowManager.showWarningAlert(ClientMsgLib.WRNG_OLD_NEW_PASS);
            return;
        }
        CommandMsg cmdMsg = new CommandMsg(Command.CHANGEPASS, loginField.getText(), passOldField.getText(), passNewFieldPrimary.getText());
        ClientNetwork.getInstance().sendObject(cmdMsg);
    }

    @Override
    public void changePassCallback(boolean result) {
        if (result) {
            WindowManager.showLogin();
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        ClientNetwork.getInstance().getMainClientHandler().setChangePassCallback(this);
    }
}
