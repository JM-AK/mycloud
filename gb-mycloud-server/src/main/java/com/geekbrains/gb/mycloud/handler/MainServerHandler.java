package com.geekbrains.gb.mycloud.handler;

import com.geekbraind.gb.mycloud.dictionary.Command;
import com.geekbraind.gb.mycloud.dictionary.MsgType;
import com.geekbraind.gb.mycloud.dictionary.ProtocolCode;
import com.geekbraind.gb.mycloud.lib.MsgLib;
import com.geekbraind.gb.mycloud.message.*;
import com.geekbraind.gb.mycloud.util.CmdService;
import com.geekbraind.gb.mycloud.util.FileService;
import com.geekbrains.gb.mycloud.data.ServerSettings;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.DosFileAttributeView;
import java.util.Date;

public class MainServerHandler extends ChannelInboundHandlerAdapter {

    public enum State {
        IDLE, FILE, COMMAND
    }

    private State currentState = State.IDLE;

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("Client connected");
        ctx.write("Welcome to "+ InetAddress.getLocalHost().getHostAddress() + "!\r\n");
        ctx.write(new Date() + "\r\n");
        ctx.flush();
    }

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
                ctx.fireChannelRead(msg);
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
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.flush();
        ctx.close();
    }

    /*
     * /cmd##/delete_file##file name
     * /cmd##/download_file##file name
     * /cmd##/upload_file##file name
     * /cmd##/rename_file##old file name##new file name
     *
     * /file_list##...fileName##...
     **/

    public void parseMsg (ChannelHandlerContext ctx, String inMsg) throws IOException {
        Path serverPath = ServerSettings.getInstance().getServerPath();

        AbstractMsg msg = CmdService.getInstance().getMsg(inMsg);

        if (msg instanceof CommandMsg) {
            CommandMsg cmdMsg = (CommandMsg) msg;

            //
            if (cmdMsg.equalsCmd(Command.GETFILELIST)) {
                Path rootPath = Paths.get((String) cmdMsg.getAttachment()[0]);
                CmdService.getInstance().sendFileList(rootPath, null, ctx, future -> System.out.println("FileList sent"));
            }

            //
            if (cmdMsg.equalsCmd(Command.RENAME_FILE_DIR)) {
                Path path = Paths.get((String) cmdMsg.getAttachment()[0]);
                String newName = (String) cmdMsg.getAttachment()[1];
                Files.move(path, path.resolveSibling(newName), StandardCopyOption.REPLACE_EXISTING);
                Path subPath = path.subpath(1, path.getNameCount()-1);
                path = Paths.get(serverPath.toString(), subPath.toString());

                CmdService.getInstance().sendFileList(path, null, ctx, future -> System.out.println("FileList sent - rename file or dir"));
                CmdService.getInstance().sendCommand(new ReplyMsg(Command.RENAME_FILE_DIR,
                        true, path.toString() + "-" + newName ).toString(), null, ctx, null);
            }

            //
            if (cmdMsg.equalsCmd(Command.CREATE_DIR)) {
                Path newPath = Paths.get((String) cmdMsg.getAttachment()[0]);
                boolean isRequestFileList = false;
                //ToDO подумать над логикой еще раз, мб убрать избыточную логику отправки дерева
                if (cmdMsg.getAttachment().length == 1) isRequestFileList = true;
                if (cmdMsg.getAttachment().length == 2) isRequestFileList = (boolean) cmdMsg.getAttachment()[1];
                Files.createDirectory(newPath);

                if (isRequestFileList)
                    CmdService.getInstance().sendFileList(newPath, null, ctx, future -> System.out.println("FileList sent"));
                CmdService.getInstance().sendCommand(new ReplyMsg(Command.CREATE_DIR, true, newPath.toString()).toString(), null, ctx, null);
            }

            //
            if (cmdMsg.equalsCmd(Command.DELETE_FILE)) {
                Path filePath = Paths.get((String) cmdMsg.getAttachment()[0]);
                Files.deleteIfExists(filePath);
                Path subPath = filePath.subpath(1, filePath.getNameCount() - 1);
                filePath = Paths.get(serverPath.toString(), subPath.toString());

                CmdService.getInstance().sendFileList(filePath, null, ctx, future -> System.out.println("FileList sent - delete file"));
                CmdService.getInstance().sendCommand(new ReplyMsg(Command.DELETE_FILE, true, filePath.toString()).toString(), null, ctx, null);
            }

            //
            if (cmdMsg.equalsCmd(Command.DELETE_DIR)) {
                Path path = Paths.get((String) cmdMsg.getAttachment()[0]);
                Files.walkFileTree(path, new FileVisitor<Path>() {
                    @Override
                    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                        DosFileAttributeView attr = Files.getFileAttributeView(dir, DosFileAttributeView.class);
                        attr.setReadOnly(false);
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        DosFileAttributeView attr = Files.getFileAttributeView(file, DosFileAttributeView.class);
                        attr.setReadOnly(false);
                        Files.deleteIfExists(file);
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                        DosFileAttributeView attr = Files.getFileAttributeView(dir, DosFileAttributeView.class);
                        attr.setReadOnly(false);
                        Files.deleteIfExists(dir);
                        return FileVisitResult.CONTINUE;
                    }
                });

                Path subPath = path.subpath(1, path.getNameCount() - 1);
                path = Paths.get(serverPath.toString(), subPath.toString());
                CmdService.getInstance().sendFileList(path, null, ctx, future -> System.out.println("FileList sent - delete dir"));
                CmdService.getInstance().sendCommand(new ReplyMsg(Command.DELETE_DIR, true, path.toString()).toString(), null, ctx, null);
            }

            //
            if (cmdMsg.equalsCmd(Command.REFRESH_FILELIST)) {
                Path path = Paths.get((String) cmdMsg.getAttachment()[0]);
                CmdService.getInstance().sendFileList(path, null, ctx, future -> System.out.println("FileList sent - refresh"));
            }

            //
            if (cmdMsg.equalsCmd(Command.OPEN_DIR)) {
                Path path = Paths.get((String) cmdMsg.getAttachment()[0]);
                CmdService.getInstance().sendFileList(path, null, ctx, future -> System.out.println("FileList sent - open dir"));
            }

            //
            if (cmdMsg.equalsCmd(Command.LOGOUT)) {
                AuthHandler.setAuthorised(false);
                ctx.pipeline().removeLast();
                ctx.pipeline().removeLast();
                CmdService.getInstance().sendCommand(new ReplyMsg(Command.LOGOUT, true).toString(), null, ctx, future -> System.out.println("Logout"));
            }

            //
            if (cmdMsg.equalsCmd(Command.DOWNLOAD_FILEDIR)){
                Path src = Paths.get((String) cmdMsg.getAttachment()[0]);
                Path dst = Paths.get((String) cmdMsg.getAttachment()[1]);
                boolean isDirectory = Files.isDirectory(src);
                int bufferSize = ServerSettings.getInstance().getBuferSize();
                if(!isDirectory) {
                    FileMsg fileMsg = new FileMsg(src, dst, false);
                    FileService.getInstance().sendFile(fileMsg, bufferSize, null, ctx, future -> System.out.println("File received"));
                }
                if(isDirectory) {
                    Files.walkFileTree(src, new SimpleFileVisitor<Path>() {
                        @Override
                        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                            Path path = src.getParent();
                            Path relPath = path.relativize(dir);
                            Path clientPath = Paths.get(dst.toString(), relPath.toString());
                            CommandMsg repCmd = new CommandMsg(Command.CREATE_DIR, clientPath.toString(), false);
                            CmdService.getInstance().sendCommand(repCmd.toString(), null, ctx, null);
                            return FileVisitResult.CONTINUE;
                        }

                        @Override
                        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                            Path subFile = src.getParent().relativize(file);
                            Path clientPath = Paths.get(dst.toString(), subFile.toString()).getParent();
                            FileMsg fileMsg = new FileMsg(file, clientPath, false);
                            FileService.getInstance().sendFile(fileMsg, bufferSize, null, ctx, future -> System.out.println("File received"));
                            return FileVisitResult.CONTINUE;
                        }
                    });
                }
            }

        }

        //команды на загрузку данных
        if (msg instanceof FileMsg) {
            FileMsg fileMsg = (FileMsg) msg;
            String fileName = fileMsg.getFileName();
            Path dstFolder = Paths.get(fileMsg.getDestination());
            Path dstFile = Paths.get(dstFolder.toString(), fileName);
            boolean isFileList = fileMsg.isFileList();
            if (Files.exists(dstFile)) Files.deleteIfExists(dstFile);
            Files.createFile(dstFile);
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

}
