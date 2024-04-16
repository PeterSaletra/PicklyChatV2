package com.app.chatapp.auth;

import com.app.chatapp.App;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class WelcomeController {
    @FXML
    private HBox switchToSignUp, switchToSignIn;
    public void switchScene(MouseEvent event) throws IOException {
        Parent root;
        String id = ((Node) event.getSource()).getId();
        if(Objects.equals(id, "switchToSignUp"))
            root = FXMLLoader.load(Objects.requireNonNull(App.class.getResource("sign-up-form.fxml")));
        else
            root = FXMLLoader.load(Objects.requireNonNull(App.class.getResource("sign-in-form.fxml")));
        Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }
}