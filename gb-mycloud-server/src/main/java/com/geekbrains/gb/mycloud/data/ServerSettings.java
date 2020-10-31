package com.geekbrains.gb.mycloud.data;

import java.nio.file.Path;
import java.nio.file.Paths;

public class ServerSettings {
    private static final ServerSettings instance = new ServerSettings();

    public static ServerSettings getInstance() {
        return instance;
    }

    private Path serverPath = Paths.get("storage_server");
    private int port = 8189;
    private int bufferSize = 8;

    public Path getServerPath() {
        return serverPath;
    }

    public int getPort() {
        return port;
    }

    public void setServerPath(Path serverPath) {
        this.serverPath = serverPath;
    }

    public int getBuferSize() {
        return bufferSize;
    }
}
