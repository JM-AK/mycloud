package com.geekbraind.gb.mycloud;

/*
* for command transfer
* types of command
*
* auth##login##password
* deletefile##file name
* renamefile##file name
* down/uploadfile##file name
* info##message
*
* */

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import java.nio.charset.StandardCharsets;

public class CommandMessage extends AbstractMessage{
    public enum State {
        IDLE, COMMAND_LENGTH, COMMAND
    }

    private State currentState = State.IDLE;
    private int commandLength;
    private String command;

    public CommandMessage (String command){
        this.command = command;
        this.commandLength = command.getBytes().length;
    }

    public void receiveCommand(ChannelHandlerContext ctx, ByteBuf buf) {
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
                    command = new String(commandByteArr, StandardCharsets.UTF_8);
                    System.out.println("STATE: Command received - " + command);
                    currentState = State.IDLE;
                    break;
                }
            }
        }
        if (buf.readableBytes() == 0) {
            buf.release();
        }
    }

    public String getCommand(){
        return command;
    }

    public void sendCommand (Channel channel, ChannelFutureListener commandListener){
        // 1 + 4 + commandLength
        ByteBuf buf = null;
        buf = ByteBufAllocator.DEFAULT.directBuffer(1 + 4 + commandLength);
        buf.writeByte(MessageLibrary.CMD_SYGNAL_BYTE);
        buf.writeInt(commandLength);
        buf.writeBytes(command.getBytes(StandardCharsets.UTF_8));

        ChannelFuture transferOperationFuture = channel.writeAndFlush(buf);
        if(commandListener != null) {
            transferOperationFuture.addListener(commandListener);
        }
    }

    public String [] parseCommand() {
        return command.split(MessageLibrary.DELIMITER);
    }
}
