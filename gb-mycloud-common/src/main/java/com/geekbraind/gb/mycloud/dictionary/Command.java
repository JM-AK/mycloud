package com.geekbraind.gb.mycloud.dictionary;

/*
* List of commands from user to server
*
* */

public enum Command {
    AUTHORISE("/authorise_user"),
    REGISTER("/register_user"),
    DELETE_FILE("/delete_file"),
    RENAME_FILE("/rename_file"),
    DOWNLOAD_FILE("/download_file"),
    UPLOAD_FILE("/upload_file"),
    ;

    private String s;

    Command(String s) {
        this.s = s;
    }

    @Override
    public String toString() {
        return s;
    }
}
