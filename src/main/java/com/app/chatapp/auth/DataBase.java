package com.app.chatapp.auth;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class DataBase {
    private final String url = "jdbc:mysql://localhost:3306/picklychat";
    private String login;
    private String password;
    private final String driver = "com.mysql.cj.jdbc.Driver";
    private Connection conn;

    public DataBase() throws Exception {
        loadENV();
        Class.forName(driver);
        conn = DriverManager.getConnection(url, login, password);
    }

    private void loadENV(){
        var props = new Properties();
        var envFile = Paths.get(".env");
        try (var inputStream = Files.newInputStream(envFile)) {
            props.load(inputStream);
        }catch (IOException e){
            e.printStackTrace();
        }
        this.login = props.get("LOGIN").toString();
        this.password = props.get("PASSWORD").toString();
    }

    public boolean insertNewUser(String name, String password){
        try(PreparedStatement stm = conn.prepareStatement("insert into users (userName, password, avatar_path) VALUES (?, ?," +  getClass().getResource("pictures/avatar.jpg") +" )")){
            stm.setString(1, name);
            stm.setString(2, password);
            stm.executeUpdate();
            return true;
        }catch (SQLException e){
            System.err.println(e.toString());
            return false;
        }
    }

    public boolean insertNewUser(String name, String password, String filePath){
        try(PreparedStatement stm = conn.prepareStatement("insert into users (userName, password, avatar_path) VALUES (?, ?, ?)")){
            stm.setString(1, name);
            stm.setString(2, password);
            stm.setString(3, filePath);
            stm.executeUpdate();
            return true;
        }catch (SQLException e){
            System.err.println();
            return false;
        }
    }

    public boolean updateUserName(String oldName, String newName){
        try(PreparedStatement stm = conn.prepareStatement("UPDATE user set username=? where username=?");){
            stm.setString(1, newName);
            stm.setString(2, oldName);
            return true;
        }catch (SQLException e){
            System.err.println("Nie udało sie zaktualizować wyniku użytkownika: " + e.toString());
            return false;
        }
    }

    public boolean doesUsernameExist(String newName){
        try(PreparedStatement stm = conn.prepareStatement("SELECT * FROM Users WHERE username=?")){
            stm.setString(1, newName);
            ResultSet res = stm.executeQuery();
            if(res.next()){
                return true;
            }
        } catch (SQLException e){
            System.err.print("Nie udalo się sprawdzić czy użytkownik istnieje: " + e.toString());
        }
        return false;
    }

    public String getUserPassword(String username){
        try(PreparedStatement stm = conn.prepareStatement("SELECT password FROM Users WHERE username=?")){
            stm.setString(1, username);
            ResultSet res = stm.executeQuery();
            if(res.next()){
                return res.getString(1);
            }
        } catch (SQLException e){
            System.err.print("Nie udało się pobrac hasla: " + e.toString());
        }
        return null;
    }

    public void shutdown(){
        try{
            conn.close();
        }catch (SQLException e){
            e.printStackTrace();
        }
    }
}