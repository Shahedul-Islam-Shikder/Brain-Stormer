package brain.brainstormer.utils;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class SceneSwitcher {

    // Method to switch scenes with an option to maximize
    public static void switchScene(Stage stage, String fxmlPath, boolean maximize) {
        try {
            // Load the FXML file
            FXMLLoader loader = new FXMLLoader(SceneSwitcher.class.getResource(fxmlPath));
            Parent root = loader.load();

            // Set the new scene on the provided stage
            stage.setScene(new Scene(root));

            // Set the stage to maximized if specified
            stage.setMaximized(maximize);

            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Failed to load FXML file: " + fxmlPath);
        }
    }
}
