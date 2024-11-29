package brain.brainstormer.utils;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.IOException;

public class SceneSwitcher {

    // Method to switch scenes with an option to maximize
    public static void switchScene(Stage stage, String fxmlPath, boolean maximize) {
        try {
            // Load the FXML file
            FXMLLoader loader = new FXMLLoader(SceneSwitcher.class.getResource(fxmlPath));
            Parent root = loader.load();

            // Create a new scene and add stylesheets
            Scene newScene = new Scene(root);
            newScene.getStylesheets().add(SceneSwitcher.class.getResource("/styles/checkbox.css").toExternalForm());
            newScene.getStylesheets().add(SceneSwitcher.class.getResource("/styles/textarea.css").toExternalForm());
            newScene.getStylesheets().add(SceneSwitcher.class.getResource("/styles/datepicker.css").toExternalForm());
            newScene.getStylesheets().add(SceneSwitcher.class.getResource("/styles/code.css").toExternalForm());
            // newScene.getStylesheets().add(SceneSwitcher.class.getResource("/styles/style.css").toExternalForm());

            // Associate the FXMLLoader with the scene
            newScene.setUserData(loader);

            // Set the new scene on the provided stage
            stage.setScene(newScene);
            stage.setResizable(true);

            // Set the stage to maximized if specified
            stage.setMaximized(maximize);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Failed to load FXML file: " + fxmlPath);
        }
    }

    // Method to get the current controller
    public static <T> T getCurrentController(Class<T> controllerClass) {
        // Get the currently active stage
        Stage currentStage = (Stage) Stage.getWindows().stream().filter(Window::isShowing).findFirst().orElse(null);

        if (currentStage != null && currentStage.getScene() != null) {
            // Retrieve the FXMLLoader associated with the scene
            FXMLLoader loader = (FXMLLoader) currentStage.getScene().getUserData();
            if (loader != null) {
                return loader.getController();
            }
        }
        return null; // Return null if no controller is found
    }
}
