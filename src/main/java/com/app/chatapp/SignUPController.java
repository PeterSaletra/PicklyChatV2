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

    private String username;
    private String password;
    private File profilePicture;
    @FXML
    private Button signUpButton;
    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private PasswordField confirmPasswordField;

    @FXML
    public void switchToSignIn(MouseEvent event) throws IOException {
        Parent root;
        root = FXMLLoader.load(Objects.requireNonNull(App.class.getResource("sign-in-form.fxml")));

        Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    @FXML
    public void signupButton(MouseEvent mouseEvent) throws IOException {
        if (!usernameField.getText().isEmpty() && !passwordField.getText().isEmpty()) {
            username = usernameField.getText();
            password = passwordField.getText();
            if(!password.equals(confirmPasswordField.getText())) {
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

            } else {
                String data = username + " " + password;
                if (profilePicture != null) {
                    App.sendToServer("REG/F");
                    App.sendToServer(data);
                    App.sendToServer(profilePicture);
                } else {
                    App.sendToServer("REG/N");
                    System.out.println("Dupa");
                    App.sendToServer(data);
                }
                String response = App.receiveFromServer();
                System.out.println(response);

                if (response.equals("OK: 201")) {
                    App.changeScene(signUpButton, "chatScene.fxml");
                } else if (response.equals("ERROR: 400")) {
                    //username exists handler
                }
            }
        }
    }

    public void fileSelection(MouseEvent mouseEvent) {

        Window mainWindow = ((Node) mouseEvent.getSource()).getScene().getWindow();
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Resource File");

        profilePicture = fileChooser.showOpenDialog(mainWindow);

    }
}
