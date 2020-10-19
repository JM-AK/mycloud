package com.geekbrains.gb.mycloud;

import com.geekbraind.gb.mycloud.CommandMessage;
import com.geekbraind.gb.mycloud.InMessageHandler;

public class InClientHandler extends InMessageHandler {
    public InClientHandler(String rootDir, CommandMessage commandMessage) {
        super(rootDir, commandMessage);
    }
}
