package com.app.chatapp;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ProgressIndicator;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.util.Objects;

public class App extends Application {

    private static Socket socket = null;
    private static BufferedReader in = null;
    private static BufferedWriter out = null;

    @Override
    public void start(Stage stage) throws IOException {


        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("sign-in-form.fxml"));
        Scene primaryStage = new Scene(fxmlLoader.load(), 800, 600);
        stage.setTitle("ChatApp");
        stage.setScene(primaryStage);
        stage.setResizable(false);
        stage.setX(Screen.getPrimary().getBounds().getWidth()/4);
        stage.setY(Screen.getPrimary().getBounds().getHeight()/8);
        stage.show();

        socket = new Socket("127.0.0.1", 9999);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

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
    }


    public static String receiveFromServer(){
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

    public static void changeScene(Node currentNode, String resourceName) throws IOException {
        Stage stage = (Stage) currentNode.getScene().getWindow();
        Parent root = FXMLLoader.load(Objects.requireNonNull(App.class.getResource(resourceName)));
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}