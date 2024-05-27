module com.app.chatapp {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
   //requires javax.websocket.api;
    requires java.sql;

    opens com.app.chatapp to javafx.fxml;
    exports com.app.chatapp;
    exports com.app.chatapp.auth;
    opens com.app.chatapp.auth to javafx.fxml;
}