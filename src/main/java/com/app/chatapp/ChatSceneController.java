package com.app.chatapp;
import com.app.chatapp.filter.ProfanityFilter;
import javafx.application.Platform;
import javafx.collections.MapChangeListener;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.HashMap;

public class ChatSceneController implements Initializable {
    @FXML
    private StackPane screen;
    @FXML
    private ListView<String> usersListView;
    @FXML
    private ListView<String> messagesListView;
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
    @FXML
    private Label usernameLabel;

    private String chosenUser;

    public HashMap<String, ArrayList<ChatSceneController.ChatMessage>> userMessagesMap = new HashMap<>();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        //enter message sending
        screen.addEventFilter(KeyEvent.KEY_PRESSED, keyEvent -> {
            if(keyEvent.getCode() == KeyCode.ENTER) {
                sendMessage();
            }
        });

        // loading in user avatar
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

        // filling in old user messages
        usersListView.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                chosenUser = usersListView.getSelectionModel().getSelectedItem();
                if (chosenUser != null) {
                    updateChatWindow();
                }
            }
        });

        // adding messages from other users
        transportController.receivedMessages.addListener((MapChangeListener<String, ChatMessage>) change -> {
            if (change.wasAdded()) {
                String sender = change.getKey();
                ChatMessage message = change.getValueAdded();
                message.getMessage().getStyleClass().add("messageBoxSender");

                ArrayList<ChatMessage> userMessages = userMessagesMap.getOrDefault(sender, new ArrayList<>());
                userMessages.add(message);
                userMessagesMap.put(sender, userMessages);

                if (Objects.equals(sender, chosenUser)) {
                    Platform.runLater(this::updateChatWindow);
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

        ArrayList<ChatMessage> userMessages = userMessagesMap.getOrDefault(chosenUser, new ArrayList<>());
        System.out.println(userMessages.isEmpty());

        for (ChatMessage msgLabel : userMessages) {
            HBox costamHBox = new HBox(msgLabel.getMessage());
            costamHBox.setPrefWidth(570);
            if(msgLabel.isReceived) {
                costamHBox.setAlignment(Pos.CENTER_RIGHT);
            } else {
                costamHBox.setAlignment(Pos.CENTER_LEFT);
            }

            messageVBox.setFillWidth(true);
            messageVBox.getChildren().add(costamHBox);
        }

        messageContainer.setContent(messageVBox);
    }

    @FXML
    public void enterSettings(MouseEvent event) throws IOException {
        ControllerUtils.changeScene(((Node)event.getSource()),"settingsScene.fxml");
    }



    public void sendMessage() {
        String message = messageBox.getText();
        if (message.isEmpty() || chosenUser == null) {
            return;
        }

        message = ProfanityFilter.filterMessage(message);
        Label label = new Label(message);
        label.getStyleClass().add("messageBox");
        label.getStyle();

        ChatMessage chatMessage = new ChatMessage(label, false);
        ArrayList<ChatMessage> userMessages = userMessagesMap.getOrDefault(chosenUser, new ArrayList<>());

        userMessages.add(chatMessage);
        userMessagesMap.put(chosenUser, userMessages);

        messageVBox.getChildren().clear();

        // Add all labels to messageVBox
        for (ChatMessage msgLabel : userMessages) {
            HBox costamHBox = new HBox(msgLabel.getMessage());
            costamHBox.setPrefWidth(570);
            if(msgLabel.isReceived) {
                costamHBox.setAlignment(Pos.CENTER_RIGHT);
            } else {
                costamHBox.setAlignment(Pos.CENTER_LEFT);
            }

            messageVBox.setFillWidth(true);
            messageVBox.getChildren().add(costamHBox);
        }

        TransportController transportController = TransportController.getInstance();
        TransportController.sendToServer(transportController.getLogin() + " " + chosenUser + " " + message);


        messageContainer.setContent(messageVBox);
        messageContainer.vvalueProperty().bind(messageVBox.heightProperty());

        messageBox.clear();
    }

    public void displayEmoji(MouseEvent mouseEvent) {
        Stage popupStage = new Stage();
        popupStage.initModality(Modality.APPLICATION_MODAL);
        popupStage.setTitle("Emoji Popup");

        GridPane gridPane = new GridPane();
        gridPane.setHgap(10);
        gridPane.setVgap(10);
        gridPane.setAlignment(Pos.CENTER);

        int[] emojiCodes = {
                0x1F600, 0x1F601, 0x1F602, 0x1F603, 0x1F604, 0x1F605, 0x1F606, 0x1F607,
                0x1F608, 0x1F609, 0x1F60A, 0x1F60B, 0x1F60C, 0x1F60D, 0x1F60E, 0x1F60F,
                0x1F610, 0x1F611, 0x1F612, 0x1F613, 0x1F614, 0x1F615, 0x1F616, 0x1F617,
                0x1F618, 0x1F619, 0x1F61A, 0x1F61B, 0x1F61C, 0x1F61D
        };

        for (int i = 0; i < emojiCodes.length; i++) {
            String emoji = new String(Character.toChars(emojiCodes[i]));
            Text emojiLabel = new Text(emoji);
            emojiLabel.setStyle("-fx-font-size: 40px;");
            emojiLabel.setOnMouseClicked(event -> {
                messageBox.setText(messageBox.getText() + " " + emojiLabel.getText());
            });

            int col = i % 10;
            int row = i / 10;

            gridPane.add(emojiLabel, col, row);
        }

        Scene scene = new Scene(gridPane, 500, 300);
        popupStage.setScene(scene);
        popupStage.showAndWait();
    }

    public void displayUsername(String username){
        usernameLabel.setText(username);
    }

    public static class ChatMessage {
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
