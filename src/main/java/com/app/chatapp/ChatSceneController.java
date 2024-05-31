package com.app.chatapp;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
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
    @FXML
    private TextField messageBox;
    @FXML
    private ScrollPane messageContainer;
    @FXML
    private VBox messageVBox;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

/*        Scene scene = userImageCircle.getScene();
        scene.addEventFilter(KeyEvent.KEY_PRESSED, keyEvent -> {
            if(keyEvent.getCode() == KeyCode.ENTER) {
                sendMessage();
            }
        });*/
        FileInputStream inputstream = null;
        try {
            inputstream = new FileInputStream("./src/main/resources/com/app/chatapp/pictures/avatar.jpg");
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


    public void sendMessage() {

        String message = messageBox.getText();
        Label label = new Label(message);
        messageVBox.getChildren().add(label);
        messageContainer.setContent(messageVBox);

        messageContainer.setFitToHeight(true);
        messageContainer.setFitToWidth(true);
        messageContainer.vvalueProperty().bind(messageVBox.heightProperty());
    }
}
