package com.geekbraind.gb.mycloud.util;

import com.geekbraind.gb.mycloud.dictionary.ProtocolCode;
import com.geekbraind.gb.mycloud.message.FileMsg;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.*;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;

public class FileService {

    private enum State {
        IDLE, FILE_NAME_LENGTH, FILE_NAME, FILE_LENGTH, FILE
    }

    private State currentState = State.IDLE;
    private int nextLength;
    private long receivedFileLength;
    private long totalFileLength;

    private static FileService instance = new FileService();

    private FileService (){}

    public static FileService getInstance() {
        return instance;
    }

    public void receiveFile(ByteBuf buf) throws IOException {
        String dstFileName = null;

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
                    dstFileName = new String(fileNameBytes, StandardCharsets.UTF_8);
                    Path dstFile = Paths.get(dstFileName);
                    System.out.println("STATE: Filename received - " + dstFileName);
                    if (Files.exists(dstFile)) {
                        Files.deleteIfExists(dstFile);
                    }
                    Files.createFile(dstFile);
                    currentState = State.FILE_LENGTH;
                }
            }
            if (currentState == State.FILE_LENGTH) {
                if (buf.readableBytes() >= 8) {
                    totalFileLength = buf.readLong();
                    System.out.println("STATE: File size received - " + totalFileLength);
                    currentState = State.FILE;
                }
            }
            if (currentState == State.FILE) {
                Path dstFile = Paths.get(dstFileName);
                byte[] tmpArr = new byte[1];
                while (buf.readableBytes() > 0) {
                    //знаю, что здесь лучше использовать BufferedOutputStream. Но очень хотелось попробовать nio
                    tmpArr[0] = buf.readByte();
                    Files.write(dstFile, tmpArr, StandardOpenOption.APPEND);
                    receivedFileLength++;
                    if (totalFileLength == receivedFileLength) {
                        currentState = State.IDLE;
                        System.out.println("STATE File received");
                        break;
                    }
                }
            }
        }
    }

    public void sendFile (FileMsg fileMsg, int bufferSize , ChannelHandlerContext ctx, ChannelFutureListener fileTransferListener) {
        // 1(=isFile) + 4(=fileNameLen) + dstFile + 8(=fileSize) + file
        String fileName = fileMsg.getFileName();
        String src = fileMsg.getSource();
        String dst = fileMsg.getDestination();
        String dstFileName = Paths.get(dst,fileName).toString();

        long fileSize = fileMsg.getFileSize();

        ByteBuf buf = null;
        buf = ByteBufAllocator.DEFAULT.directBuffer(1 + 4 + dstFileName.getBytes(StandardCharsets.UTF_8).length + 8);
        buf.writeByte(ProtocolCode.FILE_SIGNAL_BYTE);
        buf.writeInt(dstFileName.getBytes(StandardCharsets.UTF_8).length);
        buf.writeBytes(dstFileName.getBytes(StandardCharsets.UTF_8));
        buf.writeLong(fileSize);
        ctx.writeAndFlush(buf);

        Path filePath = Paths.get(src,fileName);

        //если операционная система поддреживает zero-copy отправку
//        FileRegion region = new DefaultFileRegion(filePath.toFile(), 0, fileSize);
//        ChannelFuture transferOperationFuture = ctx.writeAndFlush(region);
        //если операционная система не поддерживает zero-copy отправку как на Windows

        fileMsg.setBufferSize(bufferSize);
        byte[] byteArray = new byte[bufferSize];
        buf = ByteBufAllocator.DEFAULT.directBuffer(bufferSize);
        try (InputStream fis = Files.newInputStream(filePath)){
            while (fileSize > 0) {
                int readedBytes = fis.read(byteArray);
                if (readedBytes < bufferSize) {
                    buf = ByteBufAllocator.DEFAULT.directBuffer(readedBytes);
                    byte[] restByteArray = Arrays.copyOfRange(byteArray, 0, readedBytes);
                    buf.writeBytes(restByteArray);

                    ChannelFuture transferOperationFuture = ctx.writeAndFlush(buf);
                    if(fileTransferListener != null) {
                        transferOperationFuture.addListener(fileTransferListener);
                    }
                    try {
                        transferOperationFuture.await();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } else {
                    buf.writeBytes(byteArray);
                    ctx.writeAndFlush(buf);
                }
                ctx.writeAndFlush(buf);
                fileSize -= readedBytes;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
