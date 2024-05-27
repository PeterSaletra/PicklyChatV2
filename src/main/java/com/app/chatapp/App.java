package com.app.chatapp;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ProgressIndicator;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.*;
import java.net.Socket;
import java.util.Objects;

public class App extends Application {

    private Socket socket = null;
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
            out.write(data);
            out.newLine();
            out.flush();
        } catch (IOException e) {
            System.out.println("Lost connection to a server, couldn't send data.");
        }
    }

    public static String receiveFromServer(){
        try {
            return in.readLine();
        } catch (IOException e) {
            System.out.println("Lost connection to a server, couldn't send data.");
        }
        return null;
    }

    public void changeScene(String fxmlFile) throws IOException {
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource(fxmlFile)));
    }

    public static void main(String[] args) {
        launch();
    }
}