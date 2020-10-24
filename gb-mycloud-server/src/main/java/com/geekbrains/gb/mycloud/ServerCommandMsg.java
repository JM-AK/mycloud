package com.geekbrains.gb.mycloud;

public class ServerCommandMsg {

    private String rootDir;

//    public ServerCommandMsg() {
//
//    }

//    public ServerCommandMsg(String rootDir) {
//        this.rootDir = rootDir;
//    }

/*
 * /cmd##/delete_file##file name
 * /cmd##/download_file##file name
 * /cmd##/upload_file##file name
 * /cmd##/rename_file##old file name##new file name
 *
 * /file_list##...fileName##...
 **/
//    @Override
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
