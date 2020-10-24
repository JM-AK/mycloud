package com.geekbraind.gb.mycloud.message;

import com.geekbraind.gb.mycloud.dictionary.Command;

public class AuthRequestMsg extends CommandMsg {

    public AuthRequestMsg(String login, String password) {
        super(Command.AUTHORISE, login, password);
    }

    public String getLogin () {
        return (String) super.getAttachment()[0];
    }

    public String getPassword () {
        return (String) super.getAttachment()[1];
    }

}
