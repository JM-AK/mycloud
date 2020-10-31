package com.geekbrains.gb.mycloud.handler;

import com.geekbraind.gb.mycloud.dictionary.Command;
import com.geekbraind.gb.mycloud.dictionary.ProtocolCode;
import com.geekbraind.gb.mycloud.message.*;
import com.geekbraind.gb.mycloud.util.CmdService;
import com.geekbraind.gb.mycloud.util.FileService;
import com.geekbrains.gb.mycloud.data.ClientSettings;
import com.geekbrains.gb.mycloud.util.ClientNetwork;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.io.IOException;
import java.nio.file.*;

public class MainClientHandler extends ChannelInboundHandlerAdapter {

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
                downloadFiles(ctx, msg);
                currentState = State.IDLE;
            }
            if (currentState == State.COMMAND) {
                String cmd = CmdService.getInstance().receiveCommand(buf);
                currentState = State.IDLE;
                parseMsg(ctx, cmd);
            }
            if (buf.readableBytes() == 0) {
                buf.release();
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.flush();
        ctx.close();
    }

    public void parseMsg (ChannelHandlerContext ctx, String inMsg) throws IOException {
        Path clientPath = ClientSettings.getInstance().getHome();

        AbstractMsg msg = CmdService.getInstance().getMsg(inMsg);

        if (msg instanceof FileMsg) {
            FileMsg fileMsg = (FileMsg) msg;
            String fileName = fileMsg.getFileName();
            Path dstFolder = Paths.get(fileMsg.getDestination());
            Path dstFile = Paths.get(dstFolder.toString(), fileName);
            boolean isFileList = fileMsg.isFileList();
            if (Files.exists(dstFile)) Files.deleteIfExists(dstFile);
            Files.createFile(dstFile);

            ReplyMsg replyMsg = new ReplyMsg(Command.DOWNLOAD_FILEDIR, true, "Client ready for download");
            ClientNetwork.getInstance().sendMsg(replyMsg);
        }

        if (msg instanceof ReplyMsg) {
            ReplyMsg replyMsg = (ReplyMsg) msg;
            System.out.println(replyMsg.toString());
        }

        if (msg instanceof InfoMsg) {
            InfoMsg infoMsg = (InfoMsg) msg;
            System.out.println(infoMsg.toString());
        }
    }

    public void downloadFiles (ChannelHandlerContext ctx, Object msg) {
        try {
            FileService.getInstance().receiveFile((ByteBuf) msg);
            ReplyMsg replyMsg = new ReplyMsg(Command.DOWNLOAD_FILEDIR, true, "Success download");

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}