package com.geekbrains.gb.mycloud;

import com.geekbraind.gb.mycloud.message.CommandMsg;
import com.geekbraind.gb.mycloud.lib.MsgLib;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.util.concurrent.CountDownLatch;

public class ClientController {

    private ClientNetwork clientNetwork;

    @FXML
    public TextArea logArea;

    @FXML
    public GridPane loginArea;
    @FXML
    public Button login;

    public ClientNetwork getClientNetwork() {
        return clientNetwork;
    }

    @FXML
    public void showLoginArea(ActionEvent actionEvent) {
        loginArea.setVisible(true);
    }

    @FXML
    public void exit(ActionEvent actionEvent) {
        System.out.println("Exit");
        Stage stage = (Stage) login.getScene().getWindow();
        stage.close();
        clientNetwork.stop();
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
        try {
            start();
            new CommandMsg(MsgLib.getAuthRequestMessage("alex", "123")).sendCommand(clientNetwork.getCurrentChannel(), new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture channelFuture) throws Exception {
                    System.out.println(MsgLib.getCommandCompletedMessage(CmdLib.CommandList.AUTHORISE,null));
                }
            });
            Thread.sleep(1000);
            if(clientNetwork.isAuthorised()){
                loginArea.setVisible(false);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void start() throws InterruptedException {
        CountDownLatch networkStarter = new CountDownLatch(1);
        new Thread (()-> {
                clientNetwork = ClientNetwork.getInstance();
                clientNetwork.setRootDir("storage_client");
            try {
                clientNetwork.start(networkStarter);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
        networkStarter.await();
        logArea.appendText("Соединение с сервером открыто " + System.lineSeparator());

    }
}
