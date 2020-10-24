package com.geekbrains.gb.mycloud;

import com.geekbraind.gb.mycloud.message.CommandMsg;
import com.geekbraind.gb.mycloud.util.MsgHandler;

public class InClientHandler extends MsgHandler {
    public InClientHandler(String rootDir, CommandMsg commandMessage) {
        super(rootDir, commandMessage);
    }
}
