package brain.brainstormer.controller;

import brain.brainstormer.service.UserService;
import brain.brainstormer.utils.SceneSwitcher;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class LoginController {

    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Button loginButton;
    @FXML
    private Hyperlink registerButton;  // Changed from Button to Hyperlink

    private final UserService userService;

    public LoginController() {
        userService = new UserService(); // Initialize UserService
    }

    @FXML
    private void initialize() {
        loginButton.setOnAction(event -> loginUser());
        registerButton.setOnAction(event -> goToRegister());
    }

    private void loginUser() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (userService.loginUser(username, password)) {
            showAlert("Login Successful", "Welcome back, " + username + "!");
            goToHome();  // Transition to home screen
        } else {
            showAlert("Login Failed", "Invalid username or password.");
        }
    }

    private void goToRegister() {
        // Get the current stage from the loginButton and switch to register.fxml
        Stage stage = (Stage) loginButton.getScene().getWindow();
        SceneSwitcher.switchScene(stage, "/brain/brainstormer/register.fxml", true);
    }

    private void goToHome() {
        // Get the current stage from the loginButton and switch to home.fxml
        Stage stage = (Stage) loginButton.getScene().getWindow();
        SceneSwitcher.switchScene(stage, "/brain/brainstormer/home.fxml", true);
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
