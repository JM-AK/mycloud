package com.geekbraind.gb.mycloud.message;

/*
*
* /info##authorise_user##true##comment
*
* */

import com.geekbraind.gb.mycloud.dictionary.Command;
import com.geekbraind.gb.mycloud.lib.MsgLib;

public class AuthResponeMsg extends ReplyMsg {

    public AuthResponeMsg(boolean isAuthorised, String comment) {
        super(Command.AUTHORISE,
                isAuthorised,
                (isAuthorised)? MsgLib.AUTH_ACCEPT : MsgLib.AUTH_DENIED);
    }
}
