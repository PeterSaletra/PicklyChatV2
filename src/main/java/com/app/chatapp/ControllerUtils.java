package com.app.chatapp;

import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import java.io.IOException;
import java.util.Objects;

public final class ControllerUtils {

    public static void changeScene(Node currentNode, String resourceName) throws IOException {
        Stage stage = (Stage) currentNode.getScene().getWindow();
        Parent root = FXMLLoader.load(Objects.requireNonNull(App.class.getResource(resourceName)));
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    public static void createErrorPopUp(Node currentNode, String text, String buttonText){
        Stage dialogStage = new Stage();
        Window mainWindow = currentNode.getScene().getWindow();

        dialogStage.initModality(Modality.APPLICATION_MODAL);
        dialogStage.setWidth(300);
        dialogStage.setHeight(100);
        dialogStage.setTitle("ERROR");

        Button okButton = new Button(buttonText);
        okButton.setOnAction(event -> dialogStage.close());

        VBox vbox = new VBox(new Text(text), okButton);
        vbox.setAlignment(Pos.BOTTOM_CENTER);
        vbox.setPadding(new Insets(15));

        dialogStage.setScene(new Scene(vbox));
        dialogStage.setX(mainWindow.getX() + (mainWindow.getWidth() - dialogStage.getWidth()) / 2);
        dialogStage.setY(mainWindow.getY() + (mainWindow.getHeight() - dialogStage.getHeight()) / 2);
        dialogStage.show();
    }
}
