package com.geekbraind.gb.mycloud.message;

import com.geekbraind.gb.mycloud.dictionary.MsgType;
import com.geekbraind.gb.mycloud.lib.MsgLib;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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

    public FileListMsg(String path, List<String> files) {
        super.setMsgType(MsgType.FILE_LIST);
        this.files = files;
        this.path = path;
    }

    public FileListMsg(Path path) throws IOException {
        super.setMsgType(MsgType.FILE_LIST);
        this.files = Files.list(path).map(Path::toString).collect(Collectors.toList());
        this.path = path.toString();
    }

    @Override
    public String toString() {
        String fileListStr = Arrays.stream(files.toArray())
                .map(Object::toString)
                .collect(Collectors.joining(MsgLib.DELIMITER));
        if (fileListStr == null) return MsgType.FILE_LIST + MsgLib.DELIMITER + path;
        else return MsgType.FILE_LIST + MsgLib.DELIMITER + path + MsgLib.DELIMITER+ fileListStr;
    }
}