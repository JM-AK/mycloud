package com.geekbrains.gb.mycloud.handler;


import com.geekbraind.gb.mycloud.dictionary.Command;
import com.geekbraind.gb.mycloud.dictionary.ProtocolCode;
import com.geekbraind.gb.mycloud.message.*;
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
import java.util.logging.Logger;

/*
 * /cmd##/authorise_client##login##password
 *
 * */


public class AuthHandler extends ChannelInboundHandlerAdapter {
    private static final Logger logger = Logger.getLogger(AuthHandler.class.getSimpleName());

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
                            FileListMsg fileListMsg = new FileListMsg(path);
                            CmdService.getInstance().sendCommand(fileListMsg.toString(), null, ctx, future -> {
                                if (future.isSuccess()) {
                                    logger.info("Success sent - filelist -" + fileListMsg);
                                } else {
                                    logger.warning("Failed sent - filelist -" + fileListMsg);
                                }
                            });
                            CmdService.getInstance().sendCommand(new AuthResponeMsg(isAuthorised).toString(), null, ctx, future -> {
                                if (future.isSuccess()) {
                                    logger.info("Success sent - auth result");
                                } else {
                                    logger.warning("Failed sent - auth result");
                                }
                            });
                            ctx.pipeline().remove(this);
                            ctx.pipeline().addLast(new MainServerHandler());
                            ctx.pipeline().addLast(new FilesHandler());
                        } else {
                            CmdService.getInstance().sendCommand(new AuthResponeMsg(isAuthorised).toString(), null, ctx, future -> {
                                if (future.isSuccess()) {
                                    logger.info("Success sent - user not authorised");
                                } else {
                                    logger.info("Success sent - user not authorised");
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
                                        logger.info("Success sent - user registered");
                                    } else {
                                        logger.warning("Failed sent - user registered");
                                    }
                                });
                            } else {
                                CmdService.getInstance().sendCommand(new ReplyMsg(Command.REGISTER, false).toString(), null, ctx, future -> {
                                    if (future.isSuccess()) {
                                        logger.info("Success sent - user is notregistered");
                                    } else {
                                        logger.warning("Failed sent - user is not registered");
                                    }
                                });
                            }
                        } else {
                            CmdService.getInstance().sendCommand(new ReplyMsg(Command.REGISTER, false, "Login incorrect").toString(), null, ctx, future -> {
                                if (future.isSuccess()) {
                                    logger.info("Success sent - incorrect login");
                                } else {
                                    logger.warning("Failed sent - incorrect login");
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
                                    logger.info("Success sent - Change pass OK");
                                } else {
                                    logger.warning("Failed sent - Change pass OK");
                                }
                            });
                        } else {
                            CmdService.getInstance().sendCommand(new ReplyMsg(Command.CHANGEPASS, false).toString(), null, ctx, future -> {
                                if (future.isSuccess()) {
                                    logger.info("Success sent - Change pass WRONG");
                                } else {
                                    logger.warning("Failed sent - Change pass WRONG");
                                }
                            });
                        }
                    }

                } else {
                    ReferenceCountUtil.release(msg);
                    buf.release();
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

