package com.geekbraind.gb.mycloud.message;

import com.geekbraind.gb.mycloud.dictionary.MsgType;
import com.geekbraind.gb.mycloud.lib.MsgLib;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class FileListMsg extends AbstractMsg {
    private List<String> files;
    private String path;

    public List<String> getFiles() {
        return files;
    }

    public String getPath() {
        return path;
    }

    public FileListMsg(List<String> files, String path) {
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