package com.geekbraind.gb.mycloud.util;

import com.geekbraind.gb.mycloud.dictionary.ProtocolCode;
import com.geekbraind.gb.mycloud.message.CommandMsg;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class CmdService {

    public enum State {
        IDLE, COMMAND_LENGTH, COMMAND
    }

    private State currentState = State.IDLE;
    private int commandLength;

//    public void receiveCommand(ChannelHandlerContext ctx, ByteBuf buf) throws IOException {
//        while (buf.readableBytes() > 0) {
//            if (currentState == CommandMsg.State.IDLE){
//                currentState = CommandMsg.State.COMMAND_LENGTH;
//                System.out.println("STATE: Start command receiving");
//            }
//            if (currentState == CommandMsg.State.COMMAND_LENGTH) {
//                if (buf.readableBytes() >= 4) {
//                    System.out.println("STATE: Get command length");
//                    commandLength = buf.readInt();
//                    currentState = CommandMsg.State.COMMAND;
//                }
//            }
//            if (currentState == CommandMsg.State.COMMAND) {
//                if (buf.readableBytes() >= commandLength) {
//                    byte[] commandByteArr = new byte[commandLength];
//                    buf.readBytes(commandByteArr);
//                    command = new String(commandByteArr, StandardCharsets.UTF_8);
//                    System.out.println("STATE: Command received - " + command);
//                    currentState = CommandMsg.State.IDLE;
//                    //ссылка на метод, который должен быть переопределен
//                    parseCommand(ctx, command);
//                    break;
//                }
//            }
//        }
//    }
//
//    public void sendCommand (Channel channel, ChannelFutureListener commandListener){
//        // 1 + 4 + commandLength
//        ByteBuf buf = null;
//        buf = ByteBufAllocator.DEFAULT.directBuffer(1 + 4 + commandLength);
//        buf.writeByte(ProtocolCode.TEXT_SIGNAL_BYTE);
//        buf.writeInt(commandLength);
//        buf.writeBytes(command.getBytes(StandardCharsets.UTF_8));
//
//        ChannelFuture transferOperationFuture = channel.writeAndFlush(buf);
//        if(commandListener != null) {
//            transferOperationFuture.addListener(commandListener);
//        }
//    }

    public void parseCommand(ChannelHandlerContext ctx, String command) throws IOException {

    }


}
