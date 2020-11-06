package com.geekbraind.gb.mycloud.dictionary;

/*
* List of commands from user to server
*
* */

public enum Command {
    AUTHORISE("/authorise_user"),
    REGISTER("/register_user"),
    CHANGEPASS("/change_password"),
    LOGOUT("/logout"),

    RENAME_FILE_DIR("/rename_file_or_dir"),
    DELETE_FILE("/delete_file"),

    CREATE_DIR("/create_dir"),
    CREATE_FILE("/create_file"),
    OPEN_DIR("/open_dir"),
    DELETE_DIR("/delete_dir"),

    UPLOAD_FILEDIR("/upload_file_or_dir"),
    DOWNLOAD_FILEDIR("/download_file_or_dir"),

    GETFILELIST("/get_file_list"),
    REFRESH_FILELIST("/refresh_file_list"),

    UNKNOWNCMD(null),
    ;

    private String s;

    Command(String s) {
        this.s = s;
    }

    public static Command getCmd (String s) {
        for (int i = 0; i < Command.values().length; i++) {
            if (s.equals(Command.values()[i].s))
                return Command.values()[i];
        }
        return null;
    }

    @Override
    public String toString() {
        return s;
    }

}
