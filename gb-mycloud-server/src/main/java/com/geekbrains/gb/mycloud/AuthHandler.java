package com.geekbrains.gb.mycloud;


import com.geekbraind.gb.mycloud.CommandLibrary;
import com.geekbraind.gb.mycloud.CommandMessage;
import com.geekbraind.gb.mycloud.InMessageHandler;
import com.geekbraind.gb.mycloud.MessageLibrary;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

/*
* /cmd##/authorise_client##login##password
*
* */


public class AuthHandler extends InMessageHandler {

    public AuthHandler (String rootDir, CommandMessage commandMessage){
        super(rootDir, commandMessage);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf buf = ((ByteBuf) msg);
        while (buf.readableBytes() > 0) {
            if (currentStatus == Status.IDLE) {
                byte selectorByte = buf.readByte();
                if (selectorByte == CommandLibrary.CMD_SIGNAL_BYTE) {
                    currentStatus = Status.COMMAND;
                    super.getCommandMessage().receiveCommand(ctx, buf);
                    String commandReceived = super.getCommandMessage().getCommand();
                    if (commandReceived.startsWith(MessageLibrary.MSG_COMMAND)){
                        String[] cmdArr = commandReceived.split(MessageLibrary.DELIMITER);
                        if(cmdArr[1].equals(CommandLibrary.CMD_AUTHORISE)){
                            if (isAuthorised(cmdArr[2],cmdArr[3])) {
                                System.out.println(MessageLibrary.getAuthAcceptMessage());
                                new CommandMessage(MessageLibrary.getAuthAcceptMessage()).sendCommand(ctx.channel(), null );
                                ctx.pipeline().remove(this);
                            } else {
                                System.out.println(MessageLibrary.getAuthDeniedMessage());
                                new CommandMessage(MessageLibrary.getAuthDeniedMessage()).sendCommand(ctx.channel(), null);
                            }
                        }
                    }
                }
            }
        }
        if (buf.readableBytes() == 0) {
            buf.release();
        }
    }
    //to be done
    public boolean isAuthorised (String login, String password){
        return true;
    }
}

