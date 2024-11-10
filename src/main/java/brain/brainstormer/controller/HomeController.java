package brain.brainstormer.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

public class HomeController {

    @FXML
    private Label welcomeLabel;
    @FXML
    private Button logoutButton;

    @FXML
    private void initialize() {
        // Assuming username is stored after login, this is just a placeholder
        String username = "User";  // You could get the actual username from a session or context
        welcomeLabel.setText("Hello, " + username + "!");

        logoutButton.setOnAction(event -> logout());
    }

    private void logout() {
        // Logic to log out and return to the login screen
    }
}
