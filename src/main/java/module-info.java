module com.example.smartmusicplayer {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;
    requires java.sql;

    requires org.controlsfx.controls;
    requires org.kordamp.bootstrapfx.core;

    opens com.example.smartmusicplayer to javafx.fxml;
    opens com.example.smartmusicplayer.controllers to javafx.fxml;
    exports com.example.smartmusicplayer;
    exports com.example.smartmusicplayer.controllers;
}
