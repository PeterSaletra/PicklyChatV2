package com.app.chatapp;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;
import java.io.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javafx.scene.image.Image;
import javafx.stage.WindowEvent;

public class App extends Application {

    public static ExecutorService executorService = Executors.newCachedThreadPool();

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("sign-in-form.fxml"));
        Scene primaryStage = new Scene(fxmlLoader.load(), 800, 600);
        stage.setTitle("Pickly Chat App");
        stage.setScene(primaryStage);
        stage.setResizable(false);
        stage.getIcons().add(new Image(App.class.getResourceAsStream("pictures/pickly_logo.jpg")));
        stage.setX(Screen.getPrimary().getBounds().getWidth()/4);
        stage.setY(Screen.getPrimary().getBounds().getHeight()/8);
        stage.show();

       stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                if(TransportController.getInstance().getIsConnected()) {
                    TransportController.sendToServer("QUIT");
                }
                stage.close();
                System.exit(1);
            }
        });

    }

    public static void main(String[] args) {
        launch();
    }

}