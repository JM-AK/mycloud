package com.geekbraind.gb.mycloud.message;


import com.geekbraind.gb.mycloud.dictionary.Command;
import com.geekbraind.gb.mycloud.dictionary.MsgType;
import com.geekbraind.gb.mycloud.lib.MsgLib;

/*
 * /cmd##/authorise_client##login##password
 * /cmd##/delete_file##file name
 *
 * */

public class CommandMsg extends AbstractMsg {
    private Command command;
    private Object[] attachment;

    public CommandMsg(Command command, Object...attachment) {
        super.setMsgType(MsgType.COMMAND);
        this.command = command;
        this.attachment = attachment;
    }

    public Command getCommand () {
        return command;
    }

    public Object[] getAttachment() {
        return attachment;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Object item : attachment) {
            sb.append(MsgLib.DELIMITER).append(item);
        }
        return MsgType.COMMAND + MsgLib.DELIMITER + command + sb.toString();
    }

    public boolean equalsCmd(Command cmd) {
        return this.command.equals(cmd);
    }
}
