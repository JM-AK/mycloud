package com.geekbraind.gb.mycloud.util;

import com.geekbraind.gb.mycloud.dictionary.ProtocolCode;
import com.geekbraind.gb.mycloud.message.CommandMsg;
import com.geekbraind.gb.mycloud.message.FileMsg;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class MsgHandler extends ChannelInboundHandlerAdapter {
    public enum Status {
        IDLE, FILE, COMMAND
    }

    private CommandMsg commandMessage;
    private FileMsg fileMsg;
    public Status currentStatus;

    public MsgHandler(String rootDir, CommandMsg commandMessage){
        this.currentStatus = Status.IDLE;
        this.commandMessage = commandMessage;
        this.fileMsg = new FileMsg(rootDir);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf buf = ((ByteBuf) msg);
        while (buf.readableBytes() > 0) {
            if (currentStatus == Status.IDLE) {
                byte selectorByte = buf.readByte();
                if (selectorByte == ProtocolCode.FILE_SIGNAL_BYTE) {
                    currentStatus = Status.FILE;
                } else if (selectorByte == ProtocolCode.TEXT_SIGNAL_BYTE) {
                    currentStatus = Status.COMMAND;
                }
            }
            if (currentStatus == Status.FILE) {
//                fileMsg.receiveFile(ctx, buf);
            }
            if (currentStatus == Status.COMMAND) {
//                commandMessage.receiveCommand(ctx, buf);
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

    public CommandMsg getCommandMessage() {
        return commandMessage;
    }
}
