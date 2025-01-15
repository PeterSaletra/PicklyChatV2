package com.app.chatapp;
import com.app.chatapp.filter.ProfanityFilter;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableMap;
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
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import lombok.extern.slf4j.Slf4j;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.HashMap;

@Slf4j
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

    @FXML
    private Button fileButton;

    private String chosenUser;

    public HashMap<String, ArrayList<ChatMessage>> userMessagesMap = new HashMap<>();


    private ContextMenu statusesMenu;

    private String currentStatus = "Online";


    private TransportController transportController;


    @FXML
    public void displayFilePicker(MouseEvent event){
        FileChooser fileChooser = new FileChooser();
        Stage stage = (Stage) fileButton.getScene().getWindow();
        fileChooser.showOpenDialog(stage);
        TransportController.sendToServer(fileChooser.showOpenDialog(stage));
    }


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        initializeStatusMenu();
        updateUserStatusIndicator(currentStatus);

        //enter message sending
        screen.addEventFilter(KeyEvent.KEY_PRESSED, keyEvent -> {
            if(keyEvent.getCode() == KeyCode.ENTER) {
                sendMessage();
            }
        });

        transportController = TransportController.getInstance();
        Image image = transportController.getImage();



        //temporary solution
        while(image == null){
            image = transportController.getImage();
            System.out.print("");
        }


        ImagePattern imagePattern = new ImagePattern(image);

        userImageCircle.setFill(imagePattern);

        usersListView.setItems(transportController.users);
        usersListView.setCellFactory(list -> new UserListCell());

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

        System.out.println("Adding status listener for user: " + usernameLabel.getText());

        TransportController.userStatuses.addListener((MapChangeListener<String, String>) change -> {
            System.out.println("Status change detected!");
            System.out.println("Key: " + change.getKey());
            System.out.println("Old value: " + change.getValueRemoved());
            System.out.println("New value: " + change.getValueAdded());
            System.out.println("Was added: " + change.wasAdded());
            System.out.println("Was removed: " + change.wasRemoved());
            System.out.println("Current thread: " + Thread.currentThread().getName());

            Platform.runLater(() -> {
                System.out.println("Updating UI for status change");
                updateUserListViewStatus(change.getKey(), change.getValueAdded());
            });
        });



    }

    private void initializeStatusMenu() {
        String[] statuses = new String[]{"Online", "Away", "Offline"};
        Color[] colors = new Color[]{Color.GREEN, Color.YELLOW, Color.RED};
        MenuItem[] menuItems = new MenuItem[3];

        for (int i = 0; i < 3; i++) {
            Circle circle = new Circle(4); // Set specific size for the circle
            circle.setFill(colors[i]);

            // Create HBox to hold circle and text
            HBox content = new HBox(5); // 5 pixels spacing
            content.setAlignment(Pos.CENTER_LEFT);
            content.getChildren().addAll(circle, new Label(statuses[i]));

            menuItems[i] = new MenuItem();
            menuItems[i].setGraphic(content);
            menuItems[i].setUserData(statuses[i]);

            // Add status change handler
            final int index = i;
//            menuItems[i].setOnAction(event -> {
//                String newStatus = statuses[index];
//                // Send status change to server through TransportController
//                TransportController.userStatuses.put(usernameLabel.getText(), newStatus);
//
//                // Update local UI
//                updateUserStatusIndicator(statuses[index]);
//            });

            menuItems[i].setOnAction(event -> {
                String newStatus = statuses[index];
                StatusStore.saveStatus(usernameLabel.getText(), newStatus);
                updateUserStatusIndicator(statuses[index]);

                // Local update will happen through polling
            });

        }

        statusesMenu = new ContextMenu(menuItems);

        // Show menu on username label click
        usernameLabel.setOnMouseClicked(event -> {
            statusesMenu.show(usernameLabel, event.getScreenX(), event.getScreenY());
        });
    }



    private void updateUserStatusIndicator(String status) {
        Circle statusIndicator = new Circle(4);
        statusIndicator.setFill(getStatusColor(status));

        HBox container = new HBox(5);
        container.setAlignment(Pos.CENTER_LEFT);
        container.getChildren().add(statusIndicator);

        usernameLabel.setGraphic(container);
    }
    
    private Paint getStatusColor(String status) {
        return switch(status) {
            case "Online" -> Color.GREEN;
            case "Away" -> Color.YELLOW;
            case "Offline" -> Color.RED;
            default -> throw new IllegalStateException("Unexpected value: " + status);
        };
    }

    private void updateUserListViewStatus(String username, String status) {
        System.out.println("I am " + usernameLabel.getText());
        System.out.println("updating list view");
        Platform.runLater(() -> {
            // This will trigger the cell factory to update all visible cells
            usersListView.refresh();
        });
    }

    @FXML
    public void closeApplication(MouseEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();
        Platform.exit();
        System.exit(1);
    }

    private void updateChatWindow() {

        messageVBox.getChildren().clear();

        ArrayList<ChatMessage> userMessages = userMessagesMap.getOrDefault(chosenUser, new ArrayList<>());

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
        popupStage.initModality(Modality.WINDOW_MODAL);
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


    private class UserListCell extends ListCell<String> {
        @Override
        protected void updateItem(String username, boolean empty) {
            super.updateItem(username, empty);

            if (empty || username == null) {
                setText(null);
                setGraphic(null);
                return;
            }

            // Get status for the user
            String status = TransportController.userStatuses.getOrDefault(username, "Online");

            // Create the status indicator
            Circle statusIndicator = new Circle(4);
            statusIndicator.setFill(getStatusColor(status));

            // Set up the container with status and username
            HBox container = new HBox(5);
            container.setAlignment(Pos.CENTER_LEFT);
            container.getChildren().addAll(statusIndicator, new Label(username));

            setText(null);
            setGraphic(container);
        }
    }
}
