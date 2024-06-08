package com.app.chatapp;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.scene.control.Label;
import javafx.scene.image.Image;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TransportController implements Runnable {
    private static TransportController instance;

    private String login;
    private String password;
    private Boolean isConnected = false;

    private static Socket socket = null;
    private static BufferedReader in = null;
    private static BufferedWriter out = null;

    public ObservableList<String> users = FXCollections.observableArrayList();

    public ObservableMap<String, ChatSceneController.ChatMessage> receivedMessages = FXCollections.observableHashMap();

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

    public Boolean getIsConnected() {
        return isConnected;
    }

    public String getLogin(){
        return login;
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

    public static void sendToServer(File data){
        try{
            sendToServer(String.valueOf(data.length()));

            FileInputStream fis = new FileInputStream(data);

            byte[] imageData = new byte[4096];
            int bytesRead;

            if(!in.readLine().equals("BEGIN TRANSFER")) return;

            while ((bytesRead = fis.read(imageData)) != -1){
                socket.getOutputStream().write(imageData, 0, bytesRead);
            }
            fis.close();

        } catch (IOException e) {
            System.out.println("Lost connection to a server, couldn't send File.");
        }
        return;
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

    public Boolean signUp(File file, Boolean withFile) throws Exception {
        if (login == null || password == null) {
            return false;
        }

        socket = new Socket("127.0.0.1", 9999);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        isConnected = true;
        if(withFile) {
            sendToServer("REG/F");
            sendToServer(String.format("%s %s", login, password));
            sendToServer(file);
        }
        else {
            sendToServer("REG/N");
            sendToServer(String.format("%s %s", login, password));
        }
        String response = receiveFromServer();

        assert response != null;
        if (!response.equals("OK: 201")) {
            socket.close();
            isConnected = false;
            return false;
        }

        return true;
    }

    public int singIn() throws Exception {
        if (login == null || password == null) {
            return -1;
        }

        socket = new Socket("127.0.0.1", 9999);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        isConnected = true;

        sendToServer("LOGIN");
        sendToServer(String.format("%s %s", login, password));
        String response = receiveFromServer();


        assert response != null;
        if (!response.equals("OK: 200")) {
            socket.close();
            isConnected = false;
            if(response.equals("Error: 405")){
                return 0;
            } else{
                return -1;
            }
        }

        return 1;
    }

    @Override
    public void run() {
        try {
            String message;
            while ((message = in.readLine()) != null) {
                if(message.startsWith("USR")){
                    String[] newUser = message.split(" ");
                    Platform.runLater(() -> {
                        users.add(newUser[1]);
                    });
                } else if (message.startsWith("QUIT")){
                    String[] leavingUser = message.split(" ");
                    Platform.runLater(() -> {
                    users.remove(leavingUser[1]);
                    });
                } else if (message.startsWith("ACTIVE: ")){
                    String[] newUser = message.split(" ");
                    for (String user : newUser){
                        if(!user.equals(login) && !user.equals("ACTIVE:")) {
                            Platform.runLater(() -> {
                            users.add(user);
                            });
                        }
                    }
                } else {
                    List<String> splitedMessage = new ArrayList(Arrays.asList(message.split(" ")));
                    String sender = splitedMessage.getFirst();
                    splitedMessage.remove(0);

                    Label label = new Label(String.join(" ",splitedMessage));
                    ChatSceneController.ChatMessage chatMessage = new ChatSceneController.ChatMessage(label, true);
                    Platform.runLater(() -> {
                        receivedMessages.put(sender, chatMessage);
                    });
                }
            }
        } catch (IOException e) {
            System.out.println("Lost connection to the server.");
        }
    }
}
