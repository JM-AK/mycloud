package com.geekbraind.gb.mycloud.util;

import com.geekbraind.gb.mycloud.dictionary.ProtocolCode;
import com.geekbraind.gb.mycloud.message.FileMsg;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.*;

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

    private static FileService instance = new FileService();

    private FileService (){}

    public static FileService getInstance() {
        return instance;
    }

    public void receiveFile(ByteBuf buf) throws IOException {
        String dstFileName = null;
        Long totalPartLength = 0L;
        Long receivedPartLength = 0L;

        while (buf.readableBytes() > 0) {
            if (currentState == State.IDLE) {
                currentState = State.FILE_NAME_LENGTH;
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
                    currentState = State.FILE_LENGTH;
                }
            }
            if (currentState == State.FILE_LENGTH) {
                if (buf.readableBytes() >= 8) {
                    totalPartLength = buf.readLong();
                    System.out.println("STATE: File part size received - " + totalPartLength);
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
                    receivedPartLength++;
                    if (totalPartLength == receivedPartLength) {
                        currentState = State.IDLE;
                        System.out.println("STATE part of file received");
                        return;
                    }
                }
            }
        }
    }

    public void sendFile (FileMsg fileMsg, int bufferSize , Channel channel,ChannelHandlerContext ctx, ChannelFutureListener fileTransferListener) {
        // for FileRegion 1(=isFile) + 4(=fileNameLen) + dstFile + 8(=part of fileSize) + partFile
        // for simple protocol
        String fileName = fileMsg.getFileName();
        String src = fileMsg.getSource();
        String dst = fileMsg.getDestination();
        String dstFileName = Paths.get(dst,fileName).toString();
        Path filePath = Paths.get(src,fileName);
        long fileSize = fileMsg.getFileSize();
        fileMsg.setBufferSize(bufferSize);

        //если операционная система поддреживает zero-copy отправку
//        FileRegion region = new DefaultFileRegion(filePath.toFile(), 0, fileSize);
//        ChannelFuture transferOperationFuture = ctx.writeAndFlush(region);
        //если операционная система не поддерживает zero-copy отправку как на Windows
        try(InputStream fis = Files.newInputStream(filePath)){
            while (fileSize > 0) {
                ByteBuf buf = null;
                if (fileSize < bufferSize) bufferSize = (int) fileSize;

                buf = ByteBufAllocator.DEFAULT.directBuffer(1 + 4 + dstFileName.getBytes(StandardCharsets.UTF_8).length + 8 + bufferSize);
                buf.writeByte(ProtocolCode.FILE_SIGNAL_BYTE);
                buf.writeInt(dstFileName.getBytes(StandardCharsets.UTF_8).length);
                buf.writeBytes(dstFileName.getBytes(StandardCharsets.UTF_8));
                buf.writeLong(bufferSize);
                byte[] byteArray = new byte[bufferSize];


                    int readedBytes = fis.read(byteArray);
                    if (readedBytes < bufferSize) {
                        byte[] restByteArray = Arrays.copyOfRange(byteArray, 0, readedBytes);
                        buf.writeBytes(restByteArray);
                    } else {
                        buf.writeBytes(byteArray);
                    }
                    ChannelFuture transferOperationFuture = ctx.writeAndFlush(buf);
                    if(fileTransferListener != null) {
                        transferOperationFuture.addListener(fileTransferListener);
                    }
                    fileSize -= readedBytes;
                    System.out.println(fileSize);

            }
            if (fileSize < 0) {
                //ToDo - hashsum check
                return;
            }
        } catch (IOException e) {
        e.printStackTrace();
    }
    }

}
