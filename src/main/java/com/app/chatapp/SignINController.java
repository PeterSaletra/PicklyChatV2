package com.app.chatapp;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import java.io.IOException;

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
    public void loginButton(MouseEvent mouseEvent) throws Exception {
        if(!loginField.getText().isEmpty() && !passwordField.getText().isEmpty()) {
            login = loginField.getText();
            password = passwordField.getText();

            TransportController transportController = TransportController.getInstance();
            transportController.setLogin(login);
            transportController.setPassword(password);

            int bool = transportController.signIn();

            if(bool == 1) {
                TransportController.sendToServer("SEND_FILE");

                App.executorService.submit(transportController);
                FXMLLoader loader = new FXMLLoader(getClass().getResource("chatScene.fxml"));
                Parent root = loader.load();

                ChatSceneController controller = loader.getController();
                controller.displayUsername(login);

                Stage stage = (Stage) ((Node)mouseEvent.getSource()).getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.show();

            } else if (bool == 0) {
                ControllerUtils.createErrorPopUp(((Node) mouseEvent.getSource()), "USER ALREADY LOGGED IN", "OK");
            } else {
                ControllerUtils.createErrorPopUp(((Node) mouseEvent.getSource()), "WRONG PASSWORD/WRONG USERNAME", "OK");
            }

        }
    }

}
