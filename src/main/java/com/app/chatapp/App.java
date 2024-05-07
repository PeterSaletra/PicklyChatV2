package com.app.chatapp;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ProgressIndicator;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.IOException;
import java.util.Objects;

public class App extends Application {

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
    }
    public void changeScene(String fxmlFile) throws IOException {
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource(fxmlFile)));

    }



    public static void main(String[] args) {
        launch();
    }
}