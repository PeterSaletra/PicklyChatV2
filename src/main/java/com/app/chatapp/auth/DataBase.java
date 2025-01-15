package com.app.chatapp.auth;

import java.sql.*;

public class DataBase {
    private Connection connection;
    private final Logger logger;

    public DataBase(Connection connection) {
        this.connection = connection;
        this.logger = new Logger("Database");
    }

    public boolean insertNewUser(String name, String password){
        try(PreparedStatement stm = connection.prepareStatement("insert into users (userName, password, avatar_path) VALUES (?, ?, 'src/main/resources/com/app/chatapp/pictures/avatar.jpg' )")){
            stm.setString(1, name);
            stm.setString(2, password);
            stm.executeUpdate();
            logger.echo("User " + name + " successfully added to database");
            return true;
        }catch (SQLException e){
            logger.err("Error occurred while adding user: " + name + " to database", e.getMessage());
            return false;
        }
    }

    public String getUserFilePath(String username){
        try(PreparedStatement stm = connection.prepareStatement("SELECT avatar_path FROM users WHERE username=?")){
            stm.setString(1, username);
            ResultSet res = stm.executeQuery();
            if(res.next()){
                return res.getString(1);
            }
        } catch (SQLException e){
            logger.err("Error occurred while checking user filePath: " + username + " in database", e.getMessage());
        }
        return null;
    }

    public boolean insertNewUser(String name, String password, String filePath){
        try(PreparedStatement stm = connection.prepareStatement("insert into users (userName, password, avatar_path) VALUES (?, ?, ?)")){
            stm.setString(1, name);
            stm.setString(2, password);
            stm.setString(3, filePath);
            stm.executeUpdate();
            logger.echo("User " + name + " successfully added to database");
            return true;
        }catch (SQLException e){
            logger.err("Error occurred while adding user: " + name + " to database", e.getMessage());
            return false;
        }
    }

    public boolean updateUserName(String oldName, String newName){
        try(PreparedStatement stm = connection.prepareStatement("UPDATE users set username=? where username=?")){
            stm.setString(1, newName);
            stm.setString(2, oldName);
            logger.echo("User " + newName + " successfully updated in database");
            return true;
        }catch (SQLException e){
            logger.err("Error occurred while updating user: " + oldName + " to database", e.getMessage());
            return false;
        }
    }

    public boolean doesUsernameExist(String newName){
        try(PreparedStatement stm = connection.prepareStatement("SELECT * FROM users WHERE username=?")){
            stm.setString(1, newName);
            ResultSet res = stm.executeQuery();
            if(res.next()){
                logger.echo("User: " + newName + " exists in database");
                return true;
            }
        } catch (SQLException e){
            logger.err("Error occurred while checking user: " + newName + " in database", e.getMessage());
        }
        return false;
    }

    public String getUserPassword(String username){
        try(PreparedStatement stm = connection.prepareStatement("SELECT password FROM users WHERE username=?")){
            stm.setString(1, username);
            ResultSet res = stm.executeQuery();
            if(res.next()){
                return res.getString(1);
            }
        } catch (SQLException e){
            logger.err("Error occurred while checking user password: " + username + " in database", e.getMessage());
        }
        return null;
    }

    public void shutdown(){
        try{
            connection.close();
        }catch (SQLException e){
            logger.err("Error occurred while closing database connection", e.getMessage());
        }
    }
}