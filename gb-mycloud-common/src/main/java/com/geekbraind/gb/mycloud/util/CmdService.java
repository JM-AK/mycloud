package com.geekbraind.gb.mycloud.util;

import com.geekbraind.gb.mycloud.dictionary.MsgType;
import com.geekbraind.gb.mycloud.dictionary.ProtocolCode;
import com.geekbraind.gb.mycloud.lib.MsgLib;
import com.geekbraind.gb.mycloud.message.*;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.geekbraind.gb.mycloud.dictionary.Command.*;

/*
*
* Service to receive and transfer text messages (command msg to server or client) by network
*
* */


public class CmdService {
    private enum State {
        IDLE, COMMAND_LENGTH, COMMAND
    }

    private State currentState = State.IDLE;
    private static CmdService instance = new CmdService();

    private CmdService (){}

    public static CmdService getInstance() {
        return instance;
    }

    public String receiveCommand(ByteBuf buf) {
        int commandLength = 0;
        while (buf.readableBytes() > 0) {
            if (currentState == State.IDLE){
                currentState = State.COMMAND_LENGTH;
                System.out.println("STATE: Start command receiving");
            }
            if (currentState == State.COMMAND_LENGTH) {
                if (buf.readableBytes() >= 4) {
                    System.out.println("STATE: Get command length");
                    commandLength = buf.readInt();
                    currentState = State.COMMAND;
                }
            }
            if (currentState == State.COMMAND) {
                if (buf.readableBytes() >= commandLength) {
                    byte[] commandByteArr = new byte[commandLength];
                    buf.readBytes(commandByteArr);
                    String command = new String(commandByteArr, StandardCharsets.UTF_8);
                    System.out.println("STATE: Command received - " + command);
                    currentState = State.IDLE;
                    return command;
                }
            }
        }
        return null;
    }

    public void sendCommand (String command, ChannelHandlerContext ctx, ChannelFutureListener commandListener){
        // 1 + 4 + commandLength
        int commandLength = command.length();
        ByteBuf buf = null;
        buf = ByteBufAllocator.DEFAULT.directBuffer(1 + 4 + commandLength);
        buf.writeByte(ProtocolCode.TEXT_SIGNAL_BYTE);
        buf.writeInt(commandLength);
        buf.writeBytes(command.getBytes(StandardCharsets.UTF_8));

        ChannelFuture transferOperationFuture = ctx.writeAndFlush(buf);
        if(commandListener != null) {
            transferOperationFuture.addListener(commandListener);
        }
        try {
            transferOperationFuture.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void sendFileList(Path path, ChannelHandlerContext ctx, ChannelFutureListener commandListener) {
        List<String> files = null;
        try {
            files = Files.list(path).map(Path::toString).collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
        }
        FileListMsg fileListMsg = new FileListMsg(path.toString(), files);
        instance.sendCommand(fileListMsg.toString(), ctx, commandListener);
    }

    /*
     * /cmd##/delete_file##file name
     * /cmd##/download_file##file name
     * /cmd##/upload_file##file name
     * /cmd##/rename_file##old file name##new file name
     *
     * /file_list##...fileName##...
     **/

    public AbstractMsg getMsg (String msg) {
        String [] msgArr = msg.split(MsgLib.DELIMITER);

        if (msgArr[0].equals(MsgType.COMMAND.toString())){
            return new CommandMsg(getCmd(msgArr[1]), Arrays.asList(msgArr).subList(2,msgArr.length).toArray());
        }

        if (msgArr[0].equals(MsgType.FILE_LIST.toString())){
            return new FileListMsg(msgArr[1], Arrays.asList(msgArr).subList(2,msgArr.length));
        }

        if (msgArr[0].equals(MsgType.REPLY.toString())){
            if (msgArr.length > 2) {
                return new ReplyMsg(getCmd(msgArr[1]), Boolean.parseBoolean(msgArr[2]), msgArr[3]);
            } else {
                return new ReplyMsg(getCmd(msgArr[1]), Boolean.parseBoolean(msgArr[2]));
            }
        }

        if (msgArr[0].equals(MsgType.FILE.toString())){
            return new FileMsg(Paths.get(msgArr[1]),Paths.get(msgArr[2]), Boolean.parseBoolean(msgArr[3]));
        }

        if (msgArr[0].equals(MsgType.INFO.toString())){
            return new InfoMsg(msgArr[1]);
        }
        return null;
    }

}
