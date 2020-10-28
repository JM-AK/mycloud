package com.geekbrains.gb.mycloud.data;

public class SQLiteQuery {

    public static final String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS users (" +
            "id auto_increment NOT NULL, " +
            "login character varchar(50) NOT NULL, " +
            "password text NOT NULL, " +
            "username character varchar(50) NOT NULL, " +
            "active boolean NOT NULL, " +
            "CONSTRAINT users_pkey PRIMARY KEY (id))";

    public static final String USER_REGISTER = "INSERT INTO users (login, password, username, active) " +
            "VALUES (?, ?, ?, true)";

    public static final String UPDATE_ACTIVE = "UPDATE users SET active = ? WHERE login = ?";

    public static final String SELECT_USER_LOGIN = "SELECT * FROM users WHERE login = ?";

    public static final String SELECT_USER_LOGIN_PASS = "SELECT * FROM users WHERE " +
            "login = ? AND password = ?";

    public static final String UPDATE_PASSWORD = "UPDATE users SET password = ? WHERE login = ?";

    public static final String DELETE_USER = "DELETE FROM users WHERE login = ?";
}


