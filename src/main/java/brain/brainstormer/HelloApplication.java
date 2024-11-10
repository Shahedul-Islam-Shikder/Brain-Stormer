package brain.brainstormer;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class HelloApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        // Load login scene by default
        loadScene(stage, "hello-view.fxml", "Brain-Stormer - Login", 400, 600, true);
    }

    // Utility method to load scenes with specific dimensions and window settings
    private void loadScene(Stage stage, String fxmlFile, String title, int width, int height, boolean maximize) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource(fxmlFile));
        Scene scene = new Scene(fxmlLoader.load(), width, height);
        stage.setTitle(title);
        stage.setScene(scene);

        // Set stage to maximized if specified, otherwise set preferred size
        if (maximize) {
            stage.setMaximized(true);
        } else {
            stage.setWidth(width);
            stage.setHeight(height);
        }

        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
