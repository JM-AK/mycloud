package com.geekbrains.gb.mycloud.handler;

import com.geekbraind.gb.mycloud.dictionary.Command;
import com.geekbraind.gb.mycloud.dictionary.ProtocolCode;
import com.geekbraind.gb.mycloud.message.*;
import com.geekbraind.gb.mycloud.util.CmdService;
import com.geekbraind.gb.mycloud.util.FileService;
import com.geekbrains.gb.mycloud.controller.MainController;
import com.geekbrains.gb.mycloud.data.ClientSettings;
import com.geekbrains.gb.mycloud.util.ClientNetwork;
import com.geekbrains.gb.mycloud.util.FileListReceiverCallback;
import com.geekbrains.gb.mycloud.util.StoragePath;
import com.geekbrains.gb.mycloud.util.WindowManager;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.io.IOException;
import java.nio.file.*;
import java.util.stream.Collectors;

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

    private void parseMsg (ChannelHandlerContext ctx, String inMsg) throws IOException {

        AbstractMsg msg = CmdService.getInstance().getMsg(inMsg);

        if (msg instanceof FileMsg) {
            fileMsgHandler((FileMsg) msg);
        }
        if (msg instanceof CommandMsg) {
            cmdMsgHandler((CommandMsg) msg);
        }
        if (msg instanceof FileListMsg) {
            fileListHandler((FileListMsg) msg, MainController.getInstance());
        }
        if (msg instanceof ReplyMsg) {
            replyMsgHandler((ReplyMsg) msg);
        }
        if (msg instanceof InfoMsg) {
            infoMsgHandler((InfoMsg) msg);
        }
    }

    private void downloadFiles (ChannelHandlerContext ctx, Object msg) {
        try {
            FileService.getInstance().receiveFile((ByteBuf) msg);
            ReplyMsg replyMsg = new ReplyMsg(Command.DOWNLOAD_FILEDIR, true, "Success download");

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void fileMsgHandler (FileMsg msg) throws IOException {
        FileMsg fileMsg = (FileMsg) msg;
        String fileName = fileMsg.getFileName();
        Path dstFolder = Paths.get(fileMsg.getDestination());
        Path dstFile = Paths.get(dstFolder.toString(), fileName);
        boolean isFileList = fileMsg.isFileList();

        if (Files.exists(dstFile)) Files.deleteIfExists(dstFile);
        Files.createFile(dstFile);

        ReplyMsg replyMsg = new ReplyMsg(Command.DOWNLOAD_FILEDIR, true, "Client ready for download");
        ClientNetwork.getInstance().sendObject(replyMsg);
    }

    private void cmdMsgHandler (CommandMsg cmdMsg) {
        if (cmdMsg.equalsCmd(Command.CREATE_DIR)) {
            Path newDir = Paths.get((String) cmdMsg.getAttachment()[0]);
            try {
                Files.createFile(newDir);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void fileListHandler(FileListMsg msg, FileListReceiverCallback cb) {
        ClientSettings.getInstance().setServerFileList(
                msg.getFiles().stream()
                .map(path -> Paths.get(path))
                .collect(Collectors.toList())
        );
        Path path = Paths.get(msg.getPath());
        StoragePath sp = ClientSettings.getInstance().getServerPath();
        sp.setFullPath(path);
        if (sp.getRoot() == null) {
            sp.setRoot(path);
        }
        cb.receiveFileListCallback();

    }

    private void replyMsgHandler (ReplyMsg replyMsg){
        alertClient(replyMsg);
        if (replyMsg.getCommand().equals(Command.AUTHORISE) && replyMsg.isSuccess()) {
            WindowManager.showMain();
            MainController.getInstance().initialise();

        }
        if (replyMsg.getCommand().equals(Command.LOGOUT) && replyMsg.isSuccess()) {
            WindowManager.showLogin();
        }
    }

    private void alertClient(ReplyMsg replyMsg) {
        if (replyMsg.isSuccess()) {
            WindowManager.showInfoAlert(replyMsg.getComment());
        } else {
            WindowManager.showErrorAlert(replyMsg.getComment());
        }
    }

    private void infoMsgHandler (InfoMsg infoMsg){
        WindowManager.showInfoAlert(infoMsg.getMsg());
        System.out.println(infoMsg.toString());
    }


}
