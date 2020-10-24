package com.geekbrains.gb.mycloud.handler;

import com.geekbraind.gb.mycloud.message.CommandMsg;
import com.geekbraind.gb.mycloud.util.MsgHandler;

public class ServerHandler extends MsgHandler {

    public ServerHandler(String rootDir, CommandMsg commandMessage) {
        super(rootDir, commandMessage);
    }

}
