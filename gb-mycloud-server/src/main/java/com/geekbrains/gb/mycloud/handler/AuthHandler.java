package com.geekbrains.gb.mycloud.handler;


import com.geekbraind.gb.mycloud.dictionary.Command;
import com.geekbraind.gb.mycloud.message.AbstractMsg;
import com.geekbraind.gb.mycloud.message.AuthResponeMsg;
import com.geekbraind.gb.mycloud.message.CommandMsg;
import com.geekbraind.gb.mycloud.message.ReplyMsg;
import com.geekbraind.gb.mycloud.util.CmdService;
import com.geekbrains.gb.mycloud.data.ServerSettings;
import com.geekbrains.gb.mycloud.service.AuthService;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import java.nio.file.Files;
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
            String inMsg =  CmdService.getInstance().receiveCommand(buf);
            AbstractMsg msgReceived = CmdService.getInstance().getMsg(inMsg);

            if (msgReceived instanceof CommandMsg) {
                CommandMsg cmdMsg = (CommandMsg) msg;
                AuthService authService = AuthService.getInstance();

                if (cmdMsg.equalsCmd(Command.AUTHORISE)) {
                    String login = cmdMsg.getAttachment()[0].toString();
                    String password = cmdMsg.getAttachment()[1].toString();
                    String username = authService.getUsername(login, password);
                    if (username != null) {
                        isAuthorised = true;
                        //ToDo заменить на чтение из файла properties
                        Path path = Paths.get(ServerSettings.getInstance().getServerPath().toString(), login);
                        CmdService.getInstance().sendFileList(path, null, ctx, future -> System.out.println("FileList sent"));
                        CmdService.getInstance().sendCommand(new AuthResponeMsg(isAuthorised).toString(), null, ctx, future -> System.out.println("User is authorised"));
                        ctx.pipeline().remove(this);
                        ctx.pipeline().addLast(new MainServerHandler());
                        ctx.pipeline().addLast(new FilesHandler());
                    } else {
                        CmdService.getInstance().sendCommand(new AuthResponeMsg(isAuthorised).toString(), null, ctx,future -> System.out.println("User is not authorised") );
                    }
                }

                if (cmdMsg.equalsCmd(Command.REGISTER)) {
                    String login = cmdMsg.getAttachment()[0].toString();
                    String password = cmdMsg.getAttachment()[1].toString();
                    String username = cmdMsg.getAttachment()[2].toString();

                    if(!authService.isLoginBusy(login)) {
                        if (authService.createAccount(login, password, username)) {
                            Path ssp = ServerSettings.getInstance().getServerPath();
                            Files.createDirectory(Paths.get(ssp.toString(),login));
                            CmdService.getInstance().sendCommand(new ReplyMsg(Command.REGISTER, true).toString(), null, ctx, future -> System.out.println("New user registered"));
                        } else {
                            CmdService.getInstance().sendCommand(new ReplyMsg(Command.REGISTER, false).toString(), null, ctx, future -> System.out.println("New user NOT registered - Login BUSY"));
                        }
                    } else {
                        CmdService.getInstance().sendCommand(new ReplyMsg(Command.REGISTER, false,"Login incorrect").toString(), null, ctx, future -> System.out.println("New user NOT registered"));
                    }
                }
                if (cmdMsg.equalsCmd(Command.CHANGEPASS)) {
                    String login = cmdMsg.getAttachment()[0].toString();
                    String oldPass = cmdMsg.getAttachment()[1].toString();
                    String newPass = cmdMsg.getAttachment()[2].toString();
                    if (authService.changePassword(login, oldPass, newPass)) {
                        CmdService.getInstance().sendCommand(new ReplyMsg(Command.CHANGEPASS, true).toString(), null, ctx, future -> System.out.println("Change pass OK"));
                    } else {
                        CmdService.getInstance().sendCommand(new ReplyMsg(Command.CHANGEPASS, false).toString(), null, ctx, future -> System.out.println("Change pass WRONG"));
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

