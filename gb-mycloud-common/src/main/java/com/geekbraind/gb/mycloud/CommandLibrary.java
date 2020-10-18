package com.geekbraind.gb.mycloud;

public class CommandLibrary {
    public static final byte CMD_SIGNAL_BYTE = 15;
    public static final byte FILE_SIGNAL_BYTE = 16;

    //command from user to server
    public enum CommandList {
        AUTHORISE,
        DELETE_FILE,
        RENAME_FILE,
        DOWNLOAD_FILE,
        UPLOAD_FILE;
    }

    public static final String CMD_AUTHORISE = "/authorise_client";
    public static final String CMD_DELETE_FILE = "/delete_file";
    public static final String CMD_RENAME_FILE = "/rename_client";
    public static final String CMD_DOWNLOAD_FILE = "/download_client";
    public static final String CMD_UPLOAD_FILE = "/upload_client";
    public static final String CMD_UNKNOWN = "unknown command";

    public static String getCommandType (CommandList cmd){
        if (cmd == CommandList.AUTHORISE) {
            return CMD_AUTHORISE;
        } else if (cmd == CommandList.DELETE_FILE) {
            return CMD_DELETE_FILE;
        } else if (cmd == CommandList.RENAME_FILE) {
            return CMD_RENAME_FILE;
        } else if (cmd == CommandList.DOWNLOAD_FILE) {
            return CMD_DOWNLOAD_FILE;
        } else if (cmd == CommandList.UPLOAD_FILE) {
            return CMD_UPLOAD_FILE;
        } else {
            return CMD_UNKNOWN;
        }
    }
}
