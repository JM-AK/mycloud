package com.geekbraind.gb.mycloud.dictionary;

/*
* Type of data/object to be transferred
* */

public enum MsgType {
    UNKNOWN("/unknown type of message"),
    INFO ("/info"),
    REPLY("/reply"),
    COMMAND ("/cmd"),
    FILE ( "/file"),
    FILE_LIST ( "/file_list");

    private String s;

    MsgType(String s) {
        this.s = s;
    }


    @Override
    public String toString() {
        return s;
    }
}
