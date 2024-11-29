module brain.brainstormer {
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

    requires java.desktop;
    requires org.fxmisc.richtext;
    requires de.jensd.fx.glyphs.fontawesome;
    requires com.google.gson;

    // Open the specific packages to Gson for reflection
    opens brain.brainstormer.chess to com.google.gson;
    opens brain.brainstormer.controller to javafx.fxml;

    exports brain.brainstormer;
    exports brain.brainstormer.controller;
}
