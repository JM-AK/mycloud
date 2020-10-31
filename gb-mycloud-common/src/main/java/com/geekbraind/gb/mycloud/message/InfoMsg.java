package com.geekbraind.gb.mycloud.message;

import com.geekbraind.gb.mycloud.dictionary.MsgType;
import com.geekbraind.gb.mycloud.lib.MsgLib;

/*
 * /info##msg
 *
 * */

public class InfoMsg extends AbstractMsg{
    private String msg;

    public InfoMsg(String msg) {
        super.setMsgType(MsgType.INFO);
        this.msg = msg;
    }

    public String getMsg () {
        return this.msg;
    }

    @Override
    public String toString() {
        return MsgType.INFO + MsgLib.DELIMITER + msg;
    }
}
