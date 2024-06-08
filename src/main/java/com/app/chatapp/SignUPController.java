package com.app.chatapp;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
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
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;

public class SignUPController {

    private String login;
    private String password;
    private File profilePicture;
    @FXML
    private Button signUpButton;
    @FXML
    private TextField loginField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private PasswordField confirmPasswordField;

    @FXML
    public void switchToSignIn(MouseEvent event) throws IOException {
        ControllerUtils.changeScene(((Node)event.getSource()), "sign-in-form.fxml");
    }

    @FXML
    public void signupButton(MouseEvent mouseEvent) throws Exception {
        boolean isAvatarSet = false;
        if(!loginField.getText().isEmpty() && !passwordField.getText().isEmpty()) {
            login = loginField.getText();
            password = passwordField.getText();

            if(!password.equals(confirmPasswordField.getText())) {
                ControllerUtils.createErrorPopUp(((Node) mouseEvent.getSource()), "PASSWORDS MISMATCH", "OK");
            } else {
                TransportController transportController = TransportController.getInstance();
                transportController.setLogin(login);
                transportController.setPassword(password);
                if (profilePicture != null) isAvatarSet = true;
                if (transportController.signUp(profilePicture, isAvatarSet)) {
                    App.executorService.submit(transportController);
                    ControllerUtils.changeScene(signUpButton, "chatScene.fxml");
                } else {
                    ControllerUtils.createErrorPopUp(((Node) mouseEvent.getSource()), "USER ALREADY EXISTS", "OK");
                }
            }
        }

     /*   if (!usernameField.getText().isEmpty() && !passwordField.getText().isEmpty()) {
            username = usernameField.getText();
            password = passwordField.getText();
            if(!password.equals(confirmPasswordField.getText())) {
                ControllerUtils.createErrorPopUp(((Node) mouseEvent.getSource()), "PASSWORDS MISMATCH", "OK");
            } else {
                String data = username + " " + password;
                if (profilePicture != null) {
                    App.sendToServer("REG/F");
                    App.sendToServer(data);
                    App.sendToServer(profilePicture);
                } else {
                    App.sendToServer("REG/N");
                    App.sendToServer(data);
                }
                String response = App.receiveFromServer();
                System.out.println(response);

                if (response.equals("OK: 201")) {
                    ControllerUtils.changeScene(signUpButton, "chatScene.fxml");
                } else if (response.equals("Error: 404")) {
                    ControllerUtils.createErrorPopUp(((Node) mouseEvent.getSource()), "USER ALREADY EXISTS", "OK");
                }
            }
        }*/
    }

    public void fileSelection(MouseEvent mouseEvent) {

        Window mainWindow = ((Node) mouseEvent.getSource()).getScene().getWindow();
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Resource File");

        profilePicture = fileChooser.showOpenDialog(mainWindow);

    }
}
