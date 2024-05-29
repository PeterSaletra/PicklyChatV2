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
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        Parent root = FXMLLoader.load(Objects.requireNonNull(App.class.getResource("sign-up-form.fxml")));
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
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
                App.changeScene(signInButton, "chatScene.fxml");
            } else {
                Stage dialogStage = new Stage();
                Window mainWindow = ((Node) mouseEvent.getSource()).getScene().getWindow();

                dialogStage.initModality(Modality.APPLICATION_MODAL);
                dialogStage.setWidth(300);
                dialogStage.setHeight(100);

                Button okButton = new Button("Ok");
                okButton.setOnAction(event -> dialogStage.close());

                VBox vbox = new VBox(new Text("WRONG PASSWORD"), okButton);
                vbox.setAlignment(Pos.BOTTOM_CENTER);
                vbox.setPadding(new Insets(15));

                dialogStage.setScene(new Scene(vbox));
                dialogStage.setX(mainWindow.getX() + (mainWindow.getWidth() - dialogStage.getWidth()) / 2);
                dialogStage.setY(mainWindow.getY() + (mainWindow.getHeight() - dialogStage.getHeight()) / 2);
                dialogStage.show();
            }
        }
    }

}
