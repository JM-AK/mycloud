package com.geekbraind.gb.mycloud.message;

import com.geekbraind.gb.mycloud.dictionary.MsgType;

public abstract class AbstractMsg {
    private MsgType msgType;

    public MsgType getMsgType() {
        return msgType;
    }

    protected void setMsgType(MsgType msgType) {
        this.msgType = msgType;
    };

    public boolean equalsMsgType (AbstractMsg msg) {
        return this.msgType.equals(msg.getMsgType());
    }

}
