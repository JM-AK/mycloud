package com.geekbraind.gb.mycloud;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class MessageLibrary {

    public static final byte CMD_SYGNAL_BYTE = 15;
    public static final byte FILE_SYGNAL_BYTE = 16;

    public enum MESSAGE_TYPE {
        UNKNOWN,
        INFO,
        COMMAND,
        FILE_LIST,
    }

    //command from user to server
    public enum CommandList {
        AUTHORISE("/authorise_client"),
        DELETE_FILE("/delete_file"),
        RENAME_FILE("/rename_file"),
        DOWNLOAD_FILE("/download_file"),
        UPLOAD_FILE("/upload_file");

        private String cmd;

        CommandList(String s) {
            this.cmd = s;
        }

        public String getCommand() {
            return cmd;
        }
    }

    //message type dictionary
    public static final String MSG_COMMAND = "/cmd";
    public static final String MSG_INFO = "/info";
    public static final String MSG_FILE_LIST = "/file_list";

    //special words
    public static final String DELIMITER = "##";
    public static final String AUTH_ACCEPT = "auth_accept";
    public static final String AUTH_DENIED = "auth_denied";
    public static final String COMMAND_COMPLETED = "completed";

    //info messages
    public static String getAuthAcceptMessage() {
        return MSG_INFO + DELIMITER + AUTH_ACCEPT;
    }
    public static String getAuthDeniedMessage() {
        return MSG_INFO + DELIMITER + AUTH_DENIED;
    }
    public static String getCommandCompletedMessage(CommandList cmd, String fileName) {
        return MSG_INFO + DELIMITER + cmd.getCommand() + DELIMITER + fileName + DELIMITER + COMMAND_COMPLETED;
    }

    //system messages
    public static String getAuthRequestMessage(String login, String password) {
        return MSG_COMMAND + DELIMITER + CommandList.AUTHORISE.getCommand() + DELIMITER + login + DELIMITER + password;
    }
    public static String getFileRequestMessage(CommandList cmd, String fileName) {
        return MSG_COMMAND + DELIMITER + cmd.getCommand() + DELIMITER + fileName;
    }
    public static String getFileListRequestMessages(List<String> fileList) {
        String fileListStr = Arrays.stream(fileList.toArray())
                .map(Object::toString)
                .collect(Collectors.joining(DELIMITER));
        return MSG_FILE_LIST + DELIMITER + fileListStr;
    }

    //methods
    public static MESSAGE_TYPE getMessageType(String msg) {
        String[] arr = msg.split(DELIMITER);
        if (arr.length < 2) {
            return MESSAGE_TYPE.UNKNOWN;
        }
        String msgType = arr[0];
        switch (msgType) {
            case MSG_INFO:
                return MESSAGE_TYPE.INFO;
            case MSG_COMMAND:
                return MESSAGE_TYPE.COMMAND;
            case MSG_FILE_LIST:
                return MESSAGE_TYPE.FILE_LIST;
            default:
                return MESSAGE_TYPE.UNKNOWN;
        }
    }

}