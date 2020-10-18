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

public class ServerCommandMessage extends CommandMessage {

    private String rootDir;

    public ServerCommandMessage() {

    }

    public ServerCommandMessage(String rootDir) {
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

        MessageLibrary.MESSAGE_TYPE msgType = MessageLibrary.getMessageType(command);

        if (msgType.equals(MessageLibrary.MESSAGE_TYPE.COMMAND)) {
            String[] cmdArr = command.split(MessageLibrary.DELIMITER);
            String cmdReceived = cmdArr[1];
            String fileName = cmdArr[2];
            Paths.get(fileName);
            switch (cmdReceived) {
                case CommandLibrary.CMD_DELETE_FILE:
                    Files.deleteIfExists(Paths.get(rootDir, "\\", fileName));
                    System.out.println(MessageLibrary.getCommandCompletedMessage(CommandLibrary.CommandList.DELETE_FILE, fileName));
                    return;
                case CommandLibrary.CMD_RENAME_FILE:
                    Files.move(Paths.get(rootDir, "\\", fileName), Paths.get(rootDir, "\\", cmdArr[3]), StandardCopyOption.REPLACE_EXISTING);
                    System.out.println(MessageLibrary.getCommandCompletedMessage(CommandLibrary.CommandList.RENAME_FILE, fileName + "->" + cmdArr[3]));
                    return;
                case CommandLibrary.CMD_DOWNLOAD_FILE:
                    FileMessage fileRequested = new FileMessage(rootDir, fileName);
                    fileRequested.sendFile(ctx.channel(), new ChannelFutureListener() {
                        @Override
                        public void operationComplete(ChannelFuture channelFuture) throws Exception {
                            System.out.println(MessageLibrary.getCommandCompletedMessage(CommandLibrary.CommandList.DOWNLOAD_FILE, fileName));
                        }
                    });
                    //проверить не дублирется ли сообщение
                    System.out.println(MessageLibrary.getCommandCompletedMessage(CommandLibrary.CommandList.DOWNLOAD_FILE, fileName));
                    return;
                case CommandLibrary.CMD_UPLOAD_FILE:
                    Files.deleteIfExists(Paths.get(rootDir, "\\", fileName));
                    Files.createFile(Paths.get(rootDir, "\\", fileName));
                    System.out.println(MessageLibrary.getCommandCompletedMessage(CommandLibrary.CommandList.UPLOAD_FILE, fileName));
                    return;
            }
        } else if (msgType.equals(MessageLibrary.MESSAGE_TYPE.FILE_LIST)) {
            List<Path> fileList = Files.walk(Paths.get(rootDir)).filter(Files::isRegularFile).collect(Collectors.toList());
            CommandMessage fileListMsg = new CommandMessage(MessageLibrary.getFileListRequestMessages(fileList)) {
                @Override
                public void parseCommand(ChannelHandlerContext ctx, String command) throws IOException {
                }
            };
            fileListMsg.sendCommand(ctx.channel(), new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture channelFuture) throws Exception {
                    System.out.println("FileList send");
                }
            });
        }
    }
}
