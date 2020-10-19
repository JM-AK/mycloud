package com.geekbraind.gb.mycloud;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.io.File;

public class InMessageHandler extends ChannelInboundHandlerAdapter {
    public enum Status {
        IDLE, FILE, COMMAND
    }

    private CommandMessage commandMessage;
    private FileMessage fileMessage;
    public Status currentStatus;

    public InMessageHandler(String rootDir, CommandMessage commandMessage){
        this.currentStatus = Status.IDLE;
        this.commandMessage = commandMessage;
        this.fileMessage = new FileMessage(rootDir);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf buf = ((ByteBuf) msg);
        while (buf.readableBytes() > 0) {
            if (currentStatus == Status.IDLE) {
                byte selectorByte = buf.readByte();
                if (selectorByte == CommandLibrary.FILE_SIGNAL_BYTE) {
                    currentStatus = Status.FILE;
                } else if (selectorByte == CommandLibrary.CMD_SIGNAL_BYTE) {
                    currentStatus = Status.COMMAND;
                }
            }
            if (currentStatus == Status.FILE) {
                fileMessage.receiveFile(ctx, buf);
            }
            if (currentStatus == Status.COMMAND) {
                commandMessage.receiveCommand(ctx, buf);
            }
            if (buf.readableBytes() == 0) {
                buf.release();
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }

    public CommandMessage getCommandMessage() {
        return commandMessage;
    }
}
