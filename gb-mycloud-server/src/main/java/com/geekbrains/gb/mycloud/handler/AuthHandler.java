package com.geekbrains.gb.mycloud.handler;


import com.geekbraind.gb.mycloud.dictionary.Command;
import com.geekbraind.gb.mycloud.dictionary.MsgType;
import com.geekbraind.gb.mycloud.message.AuthResponeMsg;
import com.geekbraind.gb.mycloud.lib.MsgLib;
import com.geekbraind.gb.mycloud.util.CmdService;
import com.geekbrains.gb.mycloud.service.AuthService;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import java.nio.file.Path;
import java.nio.file.Paths;

/*
 * /cmd##/authorise_client##login##password
 *
 * */


public class AuthHandler extends ChannelInboundHandlerAdapter {
    private static boolean isAuthorised = false;

    public static void setAuthorised(boolean authorised) {
        isAuthorised = authorised;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg == null) {
            return;
        }
        if (!isAuthorised) {
            ByteBuf buf = ((ByteBuf) msg);
            String cmdMsg =  CmdService.getInstance().receiveCommand(buf);
            if (cmdMsg.startsWith(MsgType.COMMAND.toString())) {
                String[] cmdArr = cmdMsg.split(MsgLib.DELIMITER);
                if (cmdArr[1].equals(Command.AUTHORISE)) {
                    AuthService authService = AuthService.getInstance();
                    String login = cmdArr[2];
                    String password = cmdArr[3];
                    String username = authService.getUsername(login, password);
                    if (username != null) {
                        isAuthorised = true;
                        //ToDo заменить на чтение из файла properties
                        Path path = Paths.get("storage_server", login);
                        CmdService.getInstance().sendFileList(path, ctx, future -> System.out.println("FileList sent"));
                        CmdService.getInstance().sendCommand(new AuthResponeMsg(isAuthorised).toString(),ctx, future -> System.out.println("User is authorised"));
                        ctx.pipeline().remove(this);
                        ctx.pipeline().addLast(new MainServerHandler());
                        ctx.pipeline().addLast(new FilesHandler());
                    } else {
                        CmdService.getInstance().sendCommand(new AuthResponeMsg(isAuthorised).toString(), ctx,future -> System.out.println("User is not authorised") );
                    }
                }
            } else {
                ReferenceCountUtil.release(msg);
            }
        } else {
            ctx.fireChannelRead(msg);
            ctx.flush();
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.flush();
        ctx.close();
    }
}

