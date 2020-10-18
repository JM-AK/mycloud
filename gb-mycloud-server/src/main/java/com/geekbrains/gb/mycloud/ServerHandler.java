package com.geekbrains.gb.mycloud;

import com.geekbraind.gb.mycloud.CommandMessage;
import com.geekbraind.gb.mycloud.InMessageHandler;

public class ServerHandler extends InMessageHandler {

    public ServerHandler(String rootDir, CommandMessage commandMessage) {
        super(rootDir, commandMessage);
    }

}
