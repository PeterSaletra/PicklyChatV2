package com.app.chatapp;
import javafx.event.EventHandler;
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
import java.util.ArrayList;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.HashMap;
import javafx.scene.layout.StackPane;

public class ChatSceneController implements Initializable {
    @FXML
    private StackPane screen;
    @FXML
    private ListView<String> usersListView;
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


    public HashMap<String, ArrayList<ChatMessage>> userMessagesMap = new HashMap<>();
    private String currentUser;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        //  https://stackoverflow.com/questions/13246211/how-to-get-stage-from-controller-during-initialization
        // https://stackoverflow.com/questions/20107039/how-to-create-custom-dialog-with-fxml-in-javafx
    /*     Stage stage = (Stage) screen.getScene().getWindow();
        stage.getScene();
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

        TransportController transportController = TransportController.getInstance();
        usersListView.setItems(transportController.users);

        usersListView.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                currentUser = usersListView.getSelectionModel().getSelectedItem();
                if (currentUser != null) {
                    updateChatWindow();
                }
            }
        });
    }

    @FXML
    public void closeApplication(MouseEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();
        javafx.application.Platform.exit();
        System.exit(1);
    }

    private void updateChatWindow() {

        messageVBox.getChildren().clear();
        ArrayList<ChatMessage> userMessages = userMessagesMap.getOrDefault(currentUser, new ArrayList<>());
        System.out.println(userMessages.isEmpty());


        for (ChatMessage msgLabel : userMessages) {
            messageVBox.getChildren().add(msgLabel.getMessage());
        }

        messageContainer.setContent(messageVBox);
    }

    @FXML
    public void enterSettings(MouseEvent event) throws IOException {
        ControllerUtils.changeScene(((Node)event.getSource()),"settingsScene.fxml");
    }


    public void sendMessage() {
        String message = messageBox.getText();
        if (message.isEmpty() || currentUser == null) {
            return;
        }

        Label label = new Label(message);
        label.getStyleClass().add("messageBox");


        ChatMessage chatMessage = new ChatMessage(label, false);
        ArrayList<ChatMessage> userMessages = userMessagesMap.getOrDefault(currentUser, new ArrayList<>());
        userMessages.add(chatMessage);
        userMessagesMap.put(currentUser, userMessages);

        messageVBox.getChildren().clear();

        // Add all labels to messageVBox
        for (ChatMessage msgLabel : userMessages) {
            messageVBox.getChildren().add(msgLabel.getMessage());
        }


        TransportController transportController = TransportController.getInstance();
        TransportController.sendToServer(message);
        TransportController.sendToServer(transportController.getLogin() + " " + currentUser);


        messageContainer.setContent(messageVBox);
        messageContainer.setFitToHeight(true);
        messageContainer.setFitToWidth(true);
        messageContainer.vvalueProperty().bind(messageVBox.heightProperty());

        messageBox.clear();
    }


    public class ChatMessage {
        private Label message;
        private boolean isReceived;

        public ChatMessage(Label message, boolean isReceived) {
            this.message = message;
            this.isReceived = isReceived;
        }

        public Label getMessage() {
            return message;
        }

        public boolean isReceived() {
            return isReceived;
        }
    }
}
