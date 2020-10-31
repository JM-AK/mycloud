package com.geekbrains.gb.mycloud.handler;


import com.geekbraind.gb.mycloud.dictionary.Command;
import com.geekbraind.gb.mycloud.dictionary.ProtocolCode;
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
            byte selectorByte = buf.readByte();
            if (selectorByte == ProtocolCode.TEXT_SIGNAL_BYTE) {
                String inMsg = CmdService.getInstance().receiveCommand(buf);
                AbstractMsg msgReceived = CmdService.getInstance().getMsg(inMsg);

                if (msgReceived instanceof CommandMsg) {
                    CommandMsg cmdMsg = (CommandMsg) msgReceived;
                    AuthService authService = AuthService.getInstance();

                    if (cmdMsg.equalsCmd(Command.AUTHORISE)) {
                        String login = cmdMsg.getAttachment()[0].toString();
                        String password = cmdMsg.getAttachment()[1].toString();
                        String username = authService.getUsername(login, password);
                        if (username != null) {
                            isAuthorised = true;
                            //ToDo заменить на чтение из файла properties
                            Path path = Paths.get(ServerSettings.getInstance().getServerPath().toString(), login);
//                            CmdService.getInstance().sendFileList(path, null, ctx, future -> System.out.println("FileList sent"));
                            CmdService.getInstance().sendCommand(new AuthResponeMsg(isAuthorised).toString(), null, ctx, future -> {
                                if (future.isSuccess()) {
                                    System.out.println("Success sent - user is authorised");
                                } else {
                                    System.out.println("Failed sent - user is authorised");
                                }
                            });
                            ctx.pipeline().remove(this);
                            ctx.pipeline().addLast(new MainServerHandler());
                            ctx.pipeline().addLast(new FilesHandler());
                        } else {
                            CmdService.getInstance().sendCommand(new AuthResponeMsg(isAuthorised).toString(), null, ctx, future -> {
                                if (future.isSuccess()) {
                                    System.out.println("Success sent - user is not authorised");
                                } else {
                                    System.out.println("Failed sent - user is not authorised");
                                }
                            });
                        }
                    }

                    if (cmdMsg.equalsCmd(Command.REGISTER)) {
                        String login = cmdMsg.getAttachment()[0].toString();
                        String password = cmdMsg.getAttachment()[1].toString();
                        String username = cmdMsg.getAttachment()[2].toString();

                        if (!authService.isLoginBusy(login)) {
                            if (authService.createAccount(login, password, username)) {
                                Path ssp = ServerSettings.getInstance().getServerPath();
                                Files.createDirectory(Paths.get(ssp.toString(), login));
                                CmdService.getInstance().sendCommand(new ReplyMsg(Command.REGISTER, true).toString(), null, ctx, future -> {
                                    if (future.isSuccess()) {
                                        System.out.println("Success sent - user registered");
                                    } else {
                                        System.out.println("Failed sent - user is authorised");
                                    }
                                });
                            } else {
                                CmdService.getInstance().sendCommand(new ReplyMsg(Command.REGISTER, false).toString(), null, ctx, future -> {
                                    if (future.isSuccess()) {
                                        System.out.println("Success sent - user is not registered");
                                    } else {
                                        System.out.println("Failed sent - user is not registered");
                                    }
                                });
                            }
                        } else {
                            CmdService.getInstance().sendCommand(new ReplyMsg(Command.REGISTER, false, "Login incorrect").toString(), null, ctx, future -> {
                                if (future.isSuccess()) {
                                    System.out.println("Success sent - incorrect login");
                                } else {
                                    System.out.println("Failed sent - incorrect login");
                                }
                            });
                        }
                    }
                    if (cmdMsg.equalsCmd(Command.CHANGEPASS)) {
                        String login = cmdMsg.getAttachment()[0].toString();
                        String oldPass = cmdMsg.getAttachment()[1].toString();
                        String newPass = cmdMsg.getAttachment()[2].toString();
                        if (authService.changePassword(login, oldPass, newPass)) {
                            CmdService.getInstance().sendCommand(new ReplyMsg(Command.CHANGEPASS, true).toString(), null, ctx, future -> {
                                if (future.isSuccess()) {
                                    System.out.println("Success sent - Change pass OK");
                                } else {
                                    System.out.println("Failed sent - Change pass OK");
                                }
                            });
                        } else {
                            CmdService.getInstance().sendCommand(new ReplyMsg(Command.CHANGEPASS, false).toString(), null, ctx, future -> {
                                if (future.isSuccess()) {
                                    System.out.println("Success sent - Change pass WRONG");
                                } else {
                                    System.out.println("Failed sent - Change pass WRONG");
                                }
                            });
                        }
                    }

                } else {
                    ReferenceCountUtil.release(msg);
                }
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

