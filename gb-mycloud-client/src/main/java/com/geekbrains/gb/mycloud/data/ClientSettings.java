package com.geekbrains.gb.mycloud.data;

import com.geekbrains.gb.mycloud.util.StoragePath;
import javafx.stage.Stage;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class ClientSettings {

    private static final ClientSettings instance = new ClientSettings();

    public static ClientSettings getInstance() {
        return instance;
    }

    private Stage stage;
    private StoragePath localPath;
    private StoragePath serverPath;
    private Path home;
    private int bufferSize;
    private int serverPort;
    private String serverIp;
    private List<Path> clientFileList = new ArrayList<>();
    private List<Path> serverFileList = new ArrayList<>();

//    private ProgressController progressController;

    public Stage getStage() {
        return stage;
    }

    public StoragePath getLocalPath() {
        return localPath;
    }

    public StoragePath getServerPath() {
        return serverPath;
    }

    public Path getHome() {
        return home;
    }

    public int getBufferSize() {
        return bufferSize;
    }

    public int getServerPort() {
        return serverPort;
    }

    public String getServerIp() {
        return serverIp;
    }

    public List<Path> getClientFileList() {
        return clientFileList;
    }

    public List<Path> getServerFileList() {
        return serverFileList;
    }

//    public ProgressController getProgressController() {
//        return progressController;
//    }

//    public void setProgressController(ProgressController progressController) {
//        this.progressController = progressController;
//    }

    public void setBufferSize(int bufferSize) {
        this.bufferSize = bufferSize;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void setServerPort(int serverPort) {
        this.serverPort = serverPort;
    }

    public void setHome(String home) {
        this.home = Paths.get(home);
        localPath.setRoot(this.home);
    }

    public void setServerIp(String serverIp) {
        this.serverIp = serverIp;
    }

    public void setClientFileList (List<Path> clientFileList) {
        this.clientFileList = clientFileList;
    }
    public void setServerFileList (List<Path> serverFileList) {
        this.serverFileList = serverFileList;
    }

    private ClientSettings() {
        this.localPath = new StoragePath();
        this.serverPath = new StoragePath();
    }
}
