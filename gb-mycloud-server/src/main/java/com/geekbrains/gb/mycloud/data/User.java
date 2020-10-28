package com.geekbrains.gb.mycloud.data;

public class User {

    private String login;
    private String password;
    private String username;

    public User(String login, String password, String username) {
        this.login = login;
        this.password = password;
        this.username = username;
    }

    public String getLogin() {
        return login;
    }

    public String getUsername() {
        return username;
    }

    public boolean isPasswordCorrect(String password) {
        return this.password.equals(password);
    }

}
