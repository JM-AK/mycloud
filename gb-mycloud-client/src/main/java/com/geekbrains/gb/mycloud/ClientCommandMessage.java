package com.geekbrains.gb.mycloud;

import com.geekbraind.gb.mycloud.CommandLibrary;
import com.geekbraind.gb.mycloud.CommandMessage;
import com.geekbraind.gb.mycloud.FileMessage;
import com.geekbraind.gb.mycloud.MessageLibrary;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.stream.Collectors;

public class ClientCommandMessage extends CommandMessage {

    private String rootDir;

    public ClientCommandMessage() {

    }

    public ClientCommandMessage(String rootDir) {
        this.rootDir = rootDir;
    }

/*
 * /cmd##/delete_file##file name
 * /cmd##/download_file##file name
 * /cmd##/upload_file##file name
 * /cmd##/rename_file##old file name##new file name
 *
 * /file_list##...fileName##...
 **/
    @Override
    public void parseCommand(ChannelHandlerContext ctx, String command) throws IOException {
        if (!ClientNetwork.getInstance().isAuthorised() && command.equals(MessageLibrary.getAuthAcceptMessage())){
            ClientNetwork.getInstance().setIsAuthorised(true);
        }


    }
}
