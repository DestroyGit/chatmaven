package server;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SQLiteAuthService implements AuthService{
    private static Connection connection;
    private static Statement stmt;
    private static PreparedStatement psInsert;

    public SQLiteAuthService() {

    }

    @Override
    public String getNicknameByLoginAndPassword(String login, String password) {
        String nickname;
        try {
            connect();
            System.out.println("DB connect");
            ResultSet rs = stmt.executeQuery("SELECT login, password, nickname FROM chatusers");
            while (rs.next()) {
                if (rs.getString(1).equals(login) && rs.getString(2).equals(password)) {
                    nickname = rs.getString(3);
                    rs.close();
                    return nickname;
                }
            }
            rs.close();
        } catch (SQLException | ClassNotFoundException throwables) {
            throwables.printStackTrace();
        }
        finally {
            disconnect();
        }
        return null;
    }

     //РЕГИСТРАЦИЯ ПОЛЬЗОВАТЕЛЯ, КОГДА ОН ВВОДИЛ ЛОГИН, ПАРОЛЬ И НИКНЕЙМ И НАЖАЛ КНОПКУ РЕГИСТРАЦИИ
    @Override
    public boolean registration(String login, String password, String nickname) {
        try {
            connect();
            ResultSet rs = stmt.executeQuery("SELECT login, nickname FROM chatusers");
            while (rs.next()) {
                if (rs.getString(1).equals(login) || rs.getString(2).equals(nickname)) {
                    rs.close();
                    return false;
                }
            }
            psInsert = connection.prepareStatement("INSERT INTO chatusers (login, password, nickname) VALUES (?,?,?)");
            psInsert.setString(1, login);
            psInsert.setString(2, password);
            psInsert.setString(3, nickname);
            psInsert.executeUpdate();
            rs.close();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        } finally {
            disconnect();
        }
        return true;
    }

    public String changeNickname(String login, String newNickname){
        try {
            connect();
            ResultSet rs = stmt.executeQuery("SELECT login, password, nickname FROM chatusers");
            while (rs.next()) {
                if (rs.getString(3).equals(newNickname)) {
                    return null;
                }
            }
            String nick = "'" + newNickname +"'";
            String log = "'" + login +"'";
            stmt.executeUpdate("UPDATE chatusers SET nickname = " + nick + " WHERE login = " + log);
            rs.close();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        } finally {
            disconnect();
        }
        return newNickname;
    }

    public static void connect() throws ClassNotFoundException, SQLException {
        Class.forName("org.sqlite.JDBC");
        connection = DriverManager.getConnection("jdbc:sqlite:main.db");
        stmt = connection.createStatement();
    }

    public static void disconnect() {
        try {
            stmt.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        try {
            connection.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

    }
}
