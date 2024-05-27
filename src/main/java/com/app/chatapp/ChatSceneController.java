package com.app.chatapp;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.stage.FileChooser;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;

public class ChatSceneController implements Initializable {

    @FXML
    private ChoiceBox<String> choiceBox;
    @FXML
    private Circle userImageCircle;
    @FXML
    private Button sendMessageButton;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        FileInputStream inputstream = null;
        try {
            inputstream = new FileInputStream("./src/main/resources/pictures/avatar.jpg");
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        Image image = new Image(inputstream);

        ImagePattern imagePattern = new ImagePattern(image);

        userImageCircle.setFill(imagePattern);
    }

    @FXML
    public void closeApplication(MouseEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();
    }

    @FXML
    public void enterSettings(MouseEvent event) throws IOException {
        Parent root = FXMLLoader.load(Objects.requireNonNull(App.class.getResource("settingsScene.fxml")));
        Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }


}
