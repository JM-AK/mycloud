package com.geekbraind.gb.mycloud.util;

public class CheckEmail {

    public static boolean isEmail (String s) {
        String emailPattern = "^([a-z0-9]+(?:[._-][a-z0-9]+)*)@([a-z0-9]+(?:[.-][a-z0-9]+)*\\.[a-z]{2,})$";
        if (!s.matches(emailPattern)) return false;
        return true;
    }

}
