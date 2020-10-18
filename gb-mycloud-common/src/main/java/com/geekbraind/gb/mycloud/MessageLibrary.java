package com.geekbraind.gb.mycloud;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


/*
 * for command transfer
 * types of command
 *
 * /cmd##/authorise_client##login##password
 * /cmd##/delete_file##file name
 * /info##auth_acept
 * /info##auth_denied
 * /info##cmd name##file name##completed
 * /file_list##...fileName##...
 *
 * */

public class MessageLibrary {

    public enum MESSAGE_TYPE {
        UNKNOWN,
        INFO,
        COMMAND,
        FILE_LIST,
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

    //system messages
    public static String getAuthRequestMessage(String login, String password) {
        return MSG_COMMAND + DELIMITER + CommandLibrary.CMD_AUTHORISE + DELIMITER + login + DELIMITER + password;
    }
    public static String getFileRequestMessage(CommandLibrary.CommandList cmd, String fileName) {
        return MSG_COMMAND + DELIMITER + CommandLibrary.getCommandType(cmd) + DELIMITER + fileName;
    }
    public static String getFileListRequestMessages(List<Path> fileList) {
        String fileListStr = Arrays.stream(fileList.toArray())
                .map(Object::toString)
                .collect(Collectors.joining(DELIMITER));
        return MSG_FILE_LIST + DELIMITER + fileListStr;
    }
    //info messages
    public static String getAuthAcceptMessage() {
        return MSG_INFO + DELIMITER + AUTH_ACCEPT;
    }
    public static String getAuthDeniedMessage() {
        return MSG_INFO + DELIMITER + AUTH_DENIED;
    }
    public static String getCommandCompletedMessage(CommandLibrary.CommandList cmd, String fileName) {
        return MSG_INFO + DELIMITER + CommandLibrary.getCommandType(cmd) + DELIMITER + fileName + DELIMITER + COMMAND_COMPLETED;
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