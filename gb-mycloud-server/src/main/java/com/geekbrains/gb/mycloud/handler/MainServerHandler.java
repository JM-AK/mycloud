package com.geekbrains.gb.mycloud.handler;

import com.geekbraind.gb.mycloud.dictionary.ProtocolCode;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class MainServerHandler extends ChannelInboundHandlerAdapter {

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
//                fileMsg.receiveFile(ctx, buf);
            }
            if (currentState == State.COMMAND) {
//                commandMessage.receiveCommand(ctx, buf);
            }
            if (buf.readableBytes() == 0) {
                buf.release();
            }
        }
    }

    /*
     * /cmd##/delete_file##file name
     * /cmd##/download_file##file name
     * /cmd##/upload_file##file name
     * /cmd##/rename_file##old file name##new file name
     *
     * /file_list##...fileName##...
     **/


    //    public void parseCommand(ChannelHandlerContext ctx, String command) throws IOException {
//
//        MsgLib.MESSAGE_TYPE msgType = MsgLib.getMessageType(command);
//
//        if (msgType.equals(MsgLib.MESSAGE_TYPE.COMMAND)) {
//            String[] cmdArr = command.split(MsgLib.DELIMITER);
//            String cmdReceived = cmdArr[1];
//            String fileName = cmdArr[2];
//            switch (cmdReceived) {
//                case CmdLib.CMD_DELETE_FILE:
//                    Files.deleteIfExists(Paths.get(rootDir, "\\", fileName));
//                    System.out.println(MsgLib.getCommandCompletedMessage(CmdLib.CommandList.DELETE_FILE, fileName));
//                    return;
//                case CmdLib.CMD_RENAME_FILE:
//                    Files.move(Paths.get(rootDir, "\\", fileName), Paths.get(rootDir, "\\", cmdArr[3]), StandardCopyOption.REPLACE_EXISTING);
//                    System.out.println(MsgLib.getCommandCompletedMessage(CmdLib.CommandList.RENAME_FILE, fileName + "->" + cmdArr[3]));
//                    return;
//                case CmdLib.CMD_DOWNLOAD_FILE:
//                    FileMsg fileRequested = new FileMsg(rootDir, fileName);
//                    fileRequested.sendFile(ctx.channel(), new ChannelFutureListener() {
//                        @Override
//                        public void operationComplete(ChannelFuture channelFuture) throws Exception {
//                            System.out.println(MsgLib.getCommandCompletedMessage(CmdLib.CommandList.DOWNLOAD_FILE, fileName));
//                        }
//                    });
//                    //проверить не дублирется ли сообщение
//                    System.out.println(MsgLib.getCommandCompletedMessage(CmdLib.CommandList.DOWNLOAD_FILE, fileName));
//                    return;
//                case CmdLib.CMD_UPLOAD_FILE:
//                    Files.deleteIfExists(Paths.get(rootDir, "\\", fileName));
//                    Files.createFile(Paths.get(rootDir, "\\", fileName));
//                    System.out.println(MsgLib.getCommandCompletedMessage(CmdLib.CommandList.UPLOAD_FILE, fileName));
//                    return;
//            }
//        } else if (msgType.equals(MsgLib.MESSAGE_TYPE.FILE_LIST)) {
//            List<Path> fileList = Files.walk(Paths.get(rootDir)).filter(Files::isRegularFile).collect(Collectors.toList());
//            CommandMsg fileListMsg = new CommandMsg(MsgLib.getFileListRequestMessages(fileList)) {
//                @Override
//                public void parseCommand(ChannelHandlerContext ctx, String command) throws IOException {
//                }
//            };
//            fileListMsg.sendCommand(ctx.channel(), new ChannelFutureListener() {
//                @Override
//                public void operationComplete(ChannelFuture channelFuture) throws Exception {
//                    System.out.println("FileList send");
//                }
//            });
//        }
//    }

}
