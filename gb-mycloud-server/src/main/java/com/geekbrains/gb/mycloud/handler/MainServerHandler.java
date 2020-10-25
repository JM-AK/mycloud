package com.geekbrains.gb.mycloud.handler;

import com.geekbraind.gb.mycloud.dictionary.ProtocolCode;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class MainServerHandler extends ChannelInboundHandlerAdapter {

    public enum State {
        IDLE, FILE, COMMAND
    }

    private State currentState = State.IDLE;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf buf = ((ByteBuf) msg);
        while (buf.readableBytes() > 0) {
            if (currentState == State.IDLE) {
                byte selectorByte = buf.readByte();
                if (selectorByte == ProtocolCode.FILE_SIGNAL_BYTE) {
                    currentState = State.FILE;
                } else if (selectorByte == ProtocolCode.TEXT_SIGNAL_BYTE) {
                    currentState = State.COMMAND;
                }
            }
            if (currentState == State.FILE) {
//                fileMsg.receiveFile(ctx, buf);
            }
            if (currentState == State.COMMAND) {
//                commandMessage.receiveCommand(ctx, buf);
            }
            if (buf.readableBytes() == 0) {
                buf.release();
            }
        }
    }
}
