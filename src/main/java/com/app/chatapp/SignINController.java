package com.app.chatapp;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import org.w3c.dom.events.MouseEvent;

import java.io.IOException;
import java.util.Objects;

public class SignINController {

    @FXML
    private HBox switchToSignUp;

    private void switchScene(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("sign-up-form.fxml")));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root, 800, 600));
    }
    public void handleSignUpLabel(javafx.scene.input.MouseEvent mouseEvent) throws IOException {

        Stage stage = (Stage) switchToSignUp.getScene().getWindow();
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("FXML2.fxml")));
//
        Scene scene = new Scene(root);
        assert stage != null;
        stage.setScene(scene);
        stage.show();
        System.out.println("click");
    }
}
