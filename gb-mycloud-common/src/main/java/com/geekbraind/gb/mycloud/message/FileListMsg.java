package com.geekbraind.gb.mycloud.message;

import com.geekbraind.gb.mycloud.dictionary.MsgType;
import com.geekbraind.gb.mycloud.lib.MsgLib;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class FileListMsg extends AbstractMsg {
    private ArrayList<Path> files;
    private String path;

    public List<Path> getFiles() {
        return files;
    }

    public String getPath() {
        return path;
    }

    public FileListMsg(ArrayList<Path> files, String path) {
        super.setMsgType(MsgType.FILE_LIST);
        this.files = files;
        this.path = path;
    }

    @Override
    public String toString() {
        String fileListStr = Arrays.stream(files.toArray())
                .map(Object::toString)
                .collect(Collectors.joining(MsgLib.DELIMITER));
        return MsgType.FILE_LIST + MsgLib.DELIMITER + fileListStr;
    }
}