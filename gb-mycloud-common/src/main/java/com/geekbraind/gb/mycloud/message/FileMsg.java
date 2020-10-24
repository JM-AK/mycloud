package com.geekbraind.gb.mycloud.message;

/*
 * for file transfer
 * */

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileMsg extends AbstractMsg {
    private String source;
    private String destination;
    private String fileName;
    private long fileSize;
    private boolean isRequestFileList;

    public FileMsg(Path src, Path dst, boolean isFileList) {
        this.source = src.toString();
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

    public boolean isFileList() {
        return isRequestFileList;
    }
}
