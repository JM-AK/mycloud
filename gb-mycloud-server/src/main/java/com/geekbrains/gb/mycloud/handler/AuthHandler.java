package com.geekbrains.gb.mycloud.handler;


import com.geekbraind.gb.mycloud.message.CommandMsg;
import com.geekbraind.gb.mycloud.lib.MsgLib;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/*
* /cmd##/authorise_client##login##password
*
* */


public class AuthHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
//        ByteBuf buf = ((ByteBuf) msg);
//        while (buf.readableBytes() > 0) {
//            if (currentStatus == Status.IDLE) {
//                byte selectorByte = buf.readByte();
//                if (selectorByte == CmdLib.CMD_SIGNAL_BYTE) {
//                    currentStatus = Status.COMMAND;
//                    super.getCommandMessage().receiveCommand(ctx, buf);
//                    String commandReceived = super.getCommandMessage().getCommand();
//                    if (commandReceived.startsWith(MsgLib.MSG_COMMAND)){
//                        String[] cmdArr = commandReceived.split(MsgLib.DELIMITER);
//                        if(cmdArr[1].equals(CmdLib.CMD_AUTHORISE)){
//                            if (isAuthorised(cmdArr[2],cmdArr[3])) {
//                                System.out.println(MsgLib.getAuthAcceptMessage());
//                                new CommandMsg(MsgLib.getAuthAcceptMessage()).sendCommand(ctx.channel(), null );
//                                ctx.pipeline().remove(this);
//                            } else {
//                                System.out.println(MsgLib.getAuthDeniedMessage());
//                                new CommandMsg(MsgLib.getAuthDeniedMessage()).sendCommand(ctx.channel(), null);
//                            }
//                        }
//                    }
//                }
//            }
//        }
//        if (buf.readableBytes() == 0) {
//            buf.release();
//        }
    }
    //to be done
    public boolean isAuthorised (String login, String password){
        return true;
    }
}

