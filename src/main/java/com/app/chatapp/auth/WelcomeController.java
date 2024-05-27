package com.app.chatapp.auth;

import com.app.chatapp.App;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ChoiceBox;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Objects;

public class WelcomeController {
    @FXML
    private Circle userImageCircle;

    @FXML
    private HBox switchToSignUp, switchToSignIn;
    public void switchScene(MouseEvent event) throws IOException {
        Parent root;
        String id = ((Node) event.getSource()).getId();
        System.out.println(id);
        if(Objects.equals(id, "switchToSignUp"))
            root = FXMLLoader.load(Objects.requireNonNull(App.class.getResource("sign-up-form.fxml")));
        else if (Objects.equals(id, "switchToSignIn")) {
            root = FXMLLoader.load(Objects.requireNonNull(App.class.getResource("sign-in-form.fxml")));
        } else {
            root = FXMLLoader.load(Objects.requireNonNull(App.class.getResource("chatScene.fxml")));
        }
        Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }
}