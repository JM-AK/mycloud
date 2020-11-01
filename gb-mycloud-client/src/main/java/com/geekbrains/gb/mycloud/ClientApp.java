package com.geekbrains.gb.mycloud;

import com.geekbrains.gb.mycloud.data.ClientSettings;
import com.geekbrains.gb.mycloud.util.ClientNetwork;
import com.geekbrains.gb.mycloud.util.WindowManager;
import javafx.application.Application;
import javafx.stage.Stage;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class ClientApp extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        ClientSettings.getInstance().setStage(primaryStage);
        applyClientSetting();
        //for test !!!
        List<Path> pathList = new ArrayList<>();
        Path p1 = Paths.get("storage_server\\b@ex.com\\test");
        Path p2 = Paths.get("storage_server\\b@ex.com\\test1");
        pathList.add(p1);
        pathList.add(p2);
        ClientSettings.getInstance().setServerFileList(pathList);
        System.out.println(ClientSettings.getInstance().getServerFileList());
        System.out.println(ClientSettings.getInstance());
        WindowManager.showMain();
//        !!!!Вернуть после теста!!!
//        CountDownLatch networkStarter = new CountDownLatch(1);
//        new Thread(() -> {
//            try {
//                ClientNetwork.getInstance().start(networkStarter);
//            } catch (InterruptedException e) {
//                networkStarter.countDown();
//            }
//        }).start();
//        networkStarter.await();
//        WindowManager.showLogin();
    }

    @Override
    public void stop() throws Exception {
        super.stop();
    }

    public void applyClientSetting () {
        int bufferSize = 8;
        int serverPort = 8189;
        String serverIp = "localhost";
        String homeRoot = "storage_client";

        ClientSettings.getInstance().setBufferSize(bufferSize);
        ClientSettings.getInstance().setHome(homeRoot);
        ClientSettings.getInstance().setServerIp(serverIp);
        ClientSettings.getInstance().setServerPort(serverPort);
    }
}
