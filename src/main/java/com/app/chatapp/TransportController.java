package com.app.chatapp;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TransportController implements Runnable {
    private static TransportController instance;

    private String login;
    private String password;
    private Boolean isRegister = false;

    private static Socket socket = null;
    private static BufferedReader in = null;
    private static BufferedWriter out = null;

    public ObservableList<String> users = FXCollections.observableArrayList();


    private TransportController(){};

    public static TransportController getInstance() {
        if (instance == null) {
            instance = new TransportController();
        }
        return instance;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setIsRegister(Boolean isRegister) {
        this.isRegister = isRegister;
    }

    public static void sendToServer(String data){
        try {
            System.out.println(data);
            out.write(data);
            out.newLine();
            out.flush();
        } catch (IOException e) {
            System.out.println("Lost connection to a server, couldn't send data.");
        }
    }

    private static String receiveFromServer(){
        try {
            String message = "";
            while((message = in.readLine()) != null){
                return message;
            }
        } catch (IOException e) {
            System.out.println("Lost connection to a server, couldn't receive data.");
        }
        return null;
    }

    public Boolean singIn() throws Exception {
        if (login == null || password == null) {
            return false;
        }

        socket = new Socket("127.0.0.1", 9999);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

        sendToServer("LOGIN");
        sendToServer(String.format("%s %s", login, password));
        String response = receiveFromServer();

        assert response != null;
        if (!response.equals("OK: 200")) {
            socket.close();
            return false;
        }

        return true;
    }

    @Override
    public void run() {
        try {
            String message;
            while ((message = in.readLine()) != null) {
                users.add("dupa");
            }
        } catch (IOException e) {
            System.out.println("Lost connection to the server.");
        }
    }
}
