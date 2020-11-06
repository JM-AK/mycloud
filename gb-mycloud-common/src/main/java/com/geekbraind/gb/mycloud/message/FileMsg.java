package com.geekbraind.gb.mycloud.message;

/*
 * for file transfer
 * */

import com.geekbraind.gb.mycloud.dictionary.MsgType;
import com.geekbraind.gb.mycloud.lib.MsgLib;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileMsg extends AbstractMsg {
    private String source;
    private String destination;
    private String fileName;
    private long fileSize;
    private int bufferSize;
    private boolean isRequestFileList;

    public FileMsg(Path src, Path dst, boolean isFileList) {
        this.source = src.getParent().toString();
        this.destination = dst.toString();
        this.fileName = src.subpath(src.getNameCount() - 1, src.getNameCount()).toString();
        this.isRequestFileList = isRequestFileList;
        try {
            this.fileSize = Files.size(src);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getSource() {
        return source;
    }

    public String getDestination() {
        return destination;
    }

    public String getFileName() {
        return fileName;
    }

    public long getFileSize() {
        return fileSize;
    }

    public int getBufferSize() {
        return bufferSize;
    }

    public boolean isFileList() {
        return isRequestFileList;
    }

    public void setBufferSize(int bufferSize) {
        this.bufferSize = bufferSize;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(MsgLib.DELIMITER).append(source);
        sb.append(MsgLib.DELIMITER).append(destination);
        sb.append(MsgLib.DELIMITER).append(fileName);
        sb.append(MsgLib.DELIMITER).append(isRequestFileList);
        return MsgType.FILE +  sb.toString();
    }

}
