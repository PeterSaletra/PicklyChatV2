package com.app.chatapp;
import javafx.application.Platform;
import javafx.collections.MapChangeListener;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
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

    private String currentUser;

    public HashMap<String, ArrayList<ChatSceneController.ChatMessage>> userMessagesMap = new HashMap<>();

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

        transportController.receivedMessages.addListener((MapChangeListener<String, ChatMessage>) change -> {
            if (change.wasAdded()) {
                String sender = change.getKey();
                ChatMessage message = change.getValueAdded();
                message.getMessage().getStyleClass().add("messageBoxSender");

                ArrayList<ChatMessage> userMessages = userMessagesMap.getOrDefault(sender, new ArrayList<>());
                userMessages.add(message);
                userMessagesMap.put(sender, userMessages);

                if (Objects.equals(sender, currentUser)) {
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

        ArrayList<ChatMessage> userMessages = userMessagesMap.getOrDefault(currentUser, new ArrayList<>());
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
        if (message.isEmpty() || currentUser == null) {
            return;
        }

        Label label = new Label(message);
        label.getStyleClass().add("messageBox");
        label.getStyle();

        ChatMessage chatMessage = new ChatMessage(label, false);
        ArrayList<ChatMessage> userMessages = userMessagesMap.getOrDefault(currentUser, new ArrayList<>());

        userMessages.add(chatMessage);
        userMessagesMap.put(currentUser, userMessages);

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
        TransportController.sendToServer(transportController.getLogin() + " " + currentUser + " " + message);


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
            addClickListener(emojiLabel);

            int col = i % 10;
            int row = i / 10;

            gridPane.add(emojiLabel, col, row);
        }

        Scene scene = new Scene(gridPane, 500, 300);
        popupStage.setScene(scene);
        popupStage.showAndWait();
    }

    private void addClickListener(Text label) {
        label.setOnMouseClicked(event -> {
            messageBox.setText(messageBox.getText() + " " + label.getText());
        });
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
