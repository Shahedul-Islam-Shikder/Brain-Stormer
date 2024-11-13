module brain.brainstormer {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires com.almasb.fxgl.all;
    requires org.mongodb.driver.sync.client;
    requires org.mongodb.driver.core;
    requires org.mongodb.bson;
    requires jBCrypt;
    requires chesslib;

    opens brain.brainstormer to javafx.fxml;
    exports brain.brainstormer;

    exports brain.brainstormer.controller;           // Export the controller package
    opens brain.brainstormer.controller to javafx.fxml; // Open the controller package for JavaFX reflection
}
