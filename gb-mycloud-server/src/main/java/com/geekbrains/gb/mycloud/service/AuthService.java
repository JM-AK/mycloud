package com.geekbrains.gb.mycloud.service;

import com.geekbrains.gb.mycloud.data.SQLiteQuery;
import com.lambdaworks.crypto.SCryptUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;

public class AuthService {
    private static final Logger logger = LogManager.getLogger(AuthService.class);
    private static final String dbURL = "jdbc:sqlite:mycloud.db";
    private static final String dbDriver = "org.sqlite.JDBC";

    private static AuthService instance = new AuthService();
    private Connection connection;
    private Statement statement;
    private PreparedStatement ps;

    public static AuthService getInstance() {
        instance.connect();
        return instance;
    }

    /*
    * Methods to manage connection
    * */

    private void connect() {
        try {
            Class.forName(dbDriver);
            connection = DriverManager.getConnection(dbURL);
            statement = connection.createStatement();
            createTable();
            System.out.println("DB connected");
        } catch (SQLException | ClassNotFoundException throwables) {
            throwables.printStackTrace();
        }
    }

    public boolean isConnected() {
        try {
            if (!connection.isClosed()) {
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void checkConnection() {
        if (!this.isConnected()) {
            this.connect();
        }
    }

    public void disconnect() {
        try {
            if(isConnected()) {
                statement.close();
                if (ps != null) ps.close();
                connection.close();
                System.out.println("DB disconnected");
            }
        } catch (SQLException throwables) {
            logger.error("Ошибка: {}", throwables.getMessage(), throwables);
            throwables.printStackTrace();
        }
    }

    /*
     *
     * Methods to manage account
     *
     * */

    public boolean createAccount (String login, String password, String username) {
        if (!isLoginValid(login)) return false;
        if (login.isEmpty() || password.isEmpty() || username.isEmpty()) return false;
        login = login.toLowerCase().trim();
        password = password.trim();
        username = username.trim();
        String hash = SCryptUtil.scrypt(password, 4096, 8, 1);
        try {
            ps = connection.prepareStatement(SQLiteQuery.USER_REGISTER);
            ps.setString(1, login);
            ps.setString(2, hash);
            ps.setString(3, username);
            ps.execute();
            System.out.println("DB new user " + username);
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public String getUsername (String login, String password) {
        if (login.isEmpty() || password.isEmpty()) return null;
        login = login.trim();
        password = password.trim();
        try {
            ps = connection.prepareStatement(SQLiteQuery.SELECT_USER_LOGIN);
            ps.setString(1, login);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String hash = rs.getString("password");
                boolean activate = rs.getBoolean("active");
                if (activate && SCryptUtil.check(password, hash)) {
                    return rs.getString("username");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean deleteAccount (String login) {
        if (login.isEmpty() ) return false;
        login = login.trim();

        try {
            ps = connection.prepareStatement(SQLiteQuery.SELECT_USER_LOGIN);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                    ps = connection.prepareStatement(SQLiteQuery.DELETE_USER);
                    ps.setString(1, login);
                    ps.execute();
                    return true;
                }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean changePassword (String login, String oldPassword, String newPassword) {
        if (login.isEmpty() || oldPassword.isEmpty() || newPassword.isEmpty()) return false;
        login = login.trim();
        oldPassword = oldPassword.trim();
        newPassword = newPassword.trim();
        if (getUsername(login, oldPassword) == null) return false;

        String hash = SCryptUtil.scrypt(newPassword, 4096, 8, 1);
        try {
            ps = connection.prepareStatement(SQLiteQuery.UPDATE_PASSWORD);
            ps.setString(1, hash);
            ps.setString(2, login);
            ps.execute();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean activateAccount (String login) {
        if (login.isEmpty()) return false;
        login = login.trim();
        if (isAccountActive(login)) return false;
        try {
            ps = connection.prepareStatement(SQLiteQuery.UPDATE_ACTIVE);
            ps.setBoolean(1, true);
            ps.setString(2, login);
            ps.execute();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean deactivateAccount (String login) {
        if (login.isEmpty()) return false;
        login = login.trim();
        if (!isAccountActive(login)) return false;
        try {
            ps = connection.prepareStatement(SQLiteQuery.UPDATE_ACTIVE);
            ps.setBoolean(1, false);
            ps.setString(2, login);
            ps.execute();
            System.out.println("DB account deactivated " + login);
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /*
    *
    * Methods to validate login and check parameters
    *
    * */

    public boolean isLoginBusy (String login) {
        if (login.isEmpty()) return false;
        login = login.trim();
        try {
            ps = connection.prepareStatement(SQLiteQuery.SELECT_USER_LOGIN);
            ps.setString(1, login);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                return true;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean isAccountActive (String login) {
        if (login.isEmpty()) return false;
        login = login.trim();
        try {
            ps = connection.prepareStatement(SQLiteQuery.SELECT_USER_LOGIN);
            ps.setString(1, login);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                return rs.getBoolean("active");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean isLoginValid (String login) {
        if (login == null) return false;
        if (login.isEmpty()) return false;
        if (!login.contains("@")) return false;
        String[] line = login.split("@");
        if (line.length > 2 || line.length <= 1) return false;

        String sub = login.substring(login.indexOf("@") + 1, login.length());
        if (sub.isEmpty()) return false;
        if (!sub.contains(".")) return false;
        String[] line2 = sub.split("\\.");
        if (line2.length > 2 || line2.length <= 1) return false;
        if (line2[0].equals("")) return false;
        return true;
    }

    /*
    * Support methods
    * */

    private void createTable() {
        try {
            statement.execute(SQLiteQuery.CREATE_TABLE);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    //ForTesting
//    public static void main(String[] args) {
//        instance.connect();
//////        instance.createTable();
//////        System.out.println(instance.isLoginValid("alex@example.com"));
//////        System.out.println(instance.isLoginBusy("alex@example.com"));
////
//////        System.out.println(instance.deleteAccount("alex@example.com", "123"));
////        System.out.println(instance.createAccount("alex@example.com", "123", "alex-root"));
//
//        System.out.println(instance.getUsername("alex@example.com", "123"));
//        System.out.println();
//        //        System.out.println(instance.deactivateAccount("alex@example.com"));
////        System.out.println(instance.isAccountActive("alex@example.com"));
////        System.out.println(instance.activateAccount("alex@example.com"));
////
//        instance.disconnect();
////
////
//    }

}
