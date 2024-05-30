package com.app.chatapp;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.IOException;
import java.util.Objects;

public class SignINController {

    private String login;
    private String password;

    @FXML
    private Button signInButton;
    @FXML
    private TextField loginField;
    @FXML
    private PasswordField passwordField;

    @FXML
    public void switchToSignUp(MouseEvent event) throws IOException {
        ControllerUtils.changeScene(((Node)event.getSource()), "sign-up-form.fxml");
    }

    @FXML
    public void loginButton(MouseEvent mouseEvent) throws IOException {
        if(!loginField.getText().isEmpty() && !passwordField.getText().isEmpty()) {
            login = loginField.getText();
            password = passwordField.getText();
            System.out.println(loginField.getText());
            String data = login + " " + password;
            App.sendToServer("LOGIN");
            App.sendToServer(data);
            String response = App.receiveFromServer();
            System.out.println(response);

            if (response.equals("OK: 200")) {
                ControllerUtils.changeScene(signInButton, "chatScene.fxml");
            } else {
                ControllerUtils.createErrorPopUp(((Node) mouseEvent.getSource()), "WRONG PASSWORD", "OK");
            }
        }
    }

}
