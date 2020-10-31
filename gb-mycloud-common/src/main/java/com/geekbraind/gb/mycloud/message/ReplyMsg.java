package com.geekbraind.gb.mycloud.message;

/*
*
* /reply##cmd##result of command ##msg
*
* */

import com.geekbraind.gb.mycloud.dictionary.Command;
import com.geekbraind.gb.mycloud.dictionary.MsgType;
import com.geekbraind.gb.mycloud.lib.MsgLib;

public class ReplyMsg extends AbstractMsg {
    private Command command;
    private Boolean isSuccess;
    private String comment;

    public ReplyMsg(Command command, boolean isSuccess) {
        super.setMsgType(MsgType.REPLY);
        this.command = command;
        this.isSuccess = isSuccess;
    }

    public ReplyMsg(Command command, boolean isSuccess, String comment) {
        super.setMsgType(MsgType.REPLY);
        this.command = command;
        this.isSuccess = isSuccess;
        this.comment = comment;
    }

    public String getComment () {
        return this.comment;
    }

    public boolean isSuccess () {
        return this.isSuccess;
    }

    @Override
    public String toString() {
        if (comment != null) {
            return  String.join(MsgLib.DELIMITER, MsgType.REPLY.toString(), command.toString(), isSuccess.toString(), comment);
        } else {
            return String.join(MsgLib.DELIMITER,MsgType.REPLY.toString(), command.toString(), isSuccess.toString());
        }
    }








}
