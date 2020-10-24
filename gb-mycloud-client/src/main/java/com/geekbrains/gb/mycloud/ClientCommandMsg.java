package com.geekbrains.gb.mycloud;

import com.geekbraind.gb.mycloud.message.CommandMsg;
import com.geekbraind.gb.mycloud.lib.MsgLib;
import io.netty.channel.ChannelHandlerContext;

import java.io.IOException;

public class ClientCommandMsg {

    private String rootDir;

//    public ClientCommandMsg() {
//
//    }

//    public ClientCommandMsg(String rootDir) {
//        this.rootDir = rootDir;
//    }

/*
 * /cmd##/delete_file##file name
 * /cmd##/download_file##file name
 * /cmd##/upload_file##file name
 * /cmd##/rename_file##old file name##new file name
 *
 * /file_list##...fileName##...
 **/
//    @Override
//    public void parseCommand(ChannelHandlerContext ctx, String command) throws IOException {
//        if (!ClientNetwork.getInstance().isAuthorised() && command.equals(MsgLib.getAuthAcceptMessage())){
//            ClientNetwork.getInstance().setIsAuthorised(true);
//        }


//    }
}
