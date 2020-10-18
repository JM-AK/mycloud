package com.geekbraind.gb.mycloud;

/*
 * for file transfer
 * */

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;

public class FileMessage extends AbstractMessage {
    public enum State {
        IDLE, FILE_NAME_LENGTH, FILE_NAME, FILE_LENGTH, FILE
    }

    private String rootDir;
    private State currentState = State.IDLE;
    private int nextLength;
    private String fileName;
    private long fileSize;
    private long receivedFileLength;

    public FileMessage(String rootDir) {
        this.rootDir = rootDir;
    }

    public FileMessage(String rootDir, String fileName) throws IOException {
        this.rootDir = rootDir;
        this.fileName = fileName;
        this.fileSize = Files.size(Paths.get(rootDir, "\\", fileName));
    }

    public void receiveFile(ChannelHandlerContext ctx, ByteBuf buf) throws IOException {
        while (buf.readableBytes() > 0) {
            if (currentState == State.IDLE) {
                currentState = State.FILE_NAME_LENGTH;
                receivedFileLength = 0L;
                System.out.println("STATE: Start file receiving");
            }
            if (currentState == State.FILE_NAME_LENGTH) {
                if (buf.readableBytes() >= 4) {
                    System.out.println("STATE: Get filename length");
                    nextLength = buf.readInt();
                    currentState = State.FILE_NAME;
                }
            }
            if (currentState == State.FILE_NAME) {
                if (buf.readableBytes() >= nextLength) {
                    byte[] fileNameBytes = new byte[nextLength];
                    buf.readBytes(fileNameBytes);
                    fileName = new String(fileNameBytes, StandardCharsets.UTF_8);
                    System.out.println("STATE: Filename received - " + fileName);
                    currentState = State.FILE_LENGTH;
                }
            }
            if (currentState == State.FILE_LENGTH) {
                if (buf.readableBytes() >= 8) {
                    fileSize = buf.readLong();
                    System.out.println("STATE: File size received - " + fileSize);
                    currentState = State.FILE;
                }
            }
            if (currentState == State.FILE) {
                Path filePath = Paths.get(rootDir,"\\",fileName);
                byte[] tmpArr = new byte[1];
                while (buf.readableBytes() > 0) {
                    //знаю, что здесь лучше использовать BufferedOutputStream. Но очень хотелось попробовать nio
                    tmpArr[0] = buf.readByte();
                    Files.write(filePath, tmpArr, StandardOpenOption.APPEND);
                    receivedFileLength++;
                    if (fileSize == receivedFileLength) {
                        currentState = State.IDLE;
                        System.out.println("STATE File received");
                        break;
                    }
                }
            }
        }
        if (buf.readableBytes() == 0) {
            buf.release();
        }
    }

    public void sendFile (Channel channel, ChannelFutureListener fileTransferListener) throws IOException {
        // 1(=isFile) + 4(=fileNameLen) + fileName + 8(=fileSize) + file
        ByteBuf buf = null;
        buf = ByteBufAllocator.DEFAULT.directBuffer(1 + 4 + fileName.getBytes(StandardCharsets.UTF_8).length + 8);
        buf.writeByte(MessageLibrary.FILE_SYGNAL_BYTE);
        buf.writeInt(fileName.getBytes(StandardCharsets.UTF_8).length);
        buf.writeBytes(fileName.getBytes(StandardCharsets.UTF_8));
        buf.writeLong(fileSize);
        channel.writeAndFlush(buf);

        Path filePath = Paths.get(rootDir,"\\", fileName);
        FileRegion region = new DefaultFileRegion(filePath.toFile(), 0, fileSize);

        ChannelFuture transferOperationFuture = channel.writeAndFlush(region);
        if(fileTransferListener != null) {
            transferOperationFuture.addListener(fileTransferListener);
        }
    }

}
