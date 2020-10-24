package com.geekbraind.gb.mycloud.util;

import com.geekbraind.gb.mycloud.dictionary.ProtocolCode;
import com.geekbraind.gb.mycloud.message.FileMsg;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class FileService {

    public enum State {
        IDLE, FILE_NAME_LENGTH, FILE_NAME, FILE_LENGTH, FILE
    }

    private State currentState = State.IDLE;
    private int nextLength;
    private long receivedFileLength;

//    public void receiveFile(ChannelHandlerContext ctx, ByteBuf buf) throws IOException {
//        while (buf.readableBytes() > 0) {
//            if (currentState == FileMsg.State.IDLE) {
//                currentState = FileMsg.State.FILE_NAME_LENGTH;
//                receivedFileLength = 0L;
//                System.out.println("STATE: Start file receiving");
//            }
//            if (currentState == FileMsg.State.FILE_NAME_LENGTH) {
//                if (buf.readableBytes() >= 4) {
//                    System.out.println("STATE: Get filename length");
//                    nextLength = buf.readInt();
//                    currentState = FileMsg.State.FILE_NAME;
//                }
//            }
//            if (currentState == FileMsg.State.FILE_NAME) {
//                if (buf.readableBytes() >= nextLength) {
//                    byte[] fileNameBytes = new byte[nextLength];
//                    buf.readBytes(fileNameBytes);
//                    fileName = new String(fileNameBytes, StandardCharsets.UTF_8);
//                    System.out.println("STATE: Filename received - " + fileName);
//                    currentState = FileMsg.State.FILE_LENGTH;
//                }
//            }
//            if (currentState == FileMsg.State.FILE_LENGTH) {
//                if (buf.readableBytes() >= 8) {
//                    fileSize = buf.readLong();
//                    System.out.println("STATE: File size received - " + fileSize);
//                    currentState = FileMsg.State.FILE;
//                }
//            }
//            if (currentState == FileMsg.State.FILE) {
//                Path filePath = Paths.get(rootDir,"\\",fileName);
//                byte[] tmpArr = new byte[1];
//                while (buf.readableBytes() > 0) {
//                    //знаю, что здесь лучше использовать BufferedOutputStream. Но очень хотелось попробовать nio
//                    tmpArr[0] = buf.readByte();
//                    Files.write(filePath, tmpArr, StandardOpenOption.APPEND);
//                    receivedFileLength++;
//                    if (fileSize == receivedFileLength) {
//                        currentState = FileMsg.State.IDLE;
//                        System.out.println("STATE File received");
//                        break;
//                    }
//                }
//            }
//        }
//    }
//
//
//    public void sendFile (Channel channel, ChannelFutureListener fileTransferListener) throws IOException {
//        // 1(=isFile) + 4(=fileNameLen) + fileName + 8(=fileSize) + file
//        ByteBuf buf = null;
//        buf = ByteBufAllocator.DEFAULT.directBuffer(1 + 4 + fileName.getBytes(StandardCharsets.UTF_8).length + 8);
//        buf.writeByte(ProtocolCode.FILE_SIGNAL_BYTE);
//        buf.writeInt(fileName.getBytes(StandardCharsets.UTF_8).length);
//        buf.writeBytes(fileName.getBytes(StandardCharsets.UTF_8));
//        buf.writeLong(fileSize);
//        channel.writeAndFlush(buf);
//
//        Path filePath = Paths.get(rootDir,"\\", fileName);
//        FileRegion region = new DefaultFileRegion(filePath.toFile(), 0, fileSize);
//
//        ChannelFuture transferOperationFuture = channel.writeAndFlush(region);
//        if(fileTransferListener != null) {
//            transferOperationFuture.addListener(fileTransferListener);
//        }
//    }

}
