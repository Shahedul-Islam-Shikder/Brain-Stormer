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

public class RegisterController {

    @FXML
    private TextField usernameField;

    @FXML
    private TextField emailField;

    @FXML
    private TextField nameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Button registerButton;

    @FXML
    private Hyperlink loginButton;

    private final UserService userService;

    public RegisterController() {
        userService = new UserService();
    }

    @FXML
    private void initialize() {
        registerButton.setOnAction(event -> registerUser());
        loginButton.setOnAction(event -> goToLogin());
    }

    private void registerUser() {
        String username = usernameField.getText();
        String email = emailField.getText();
        String name = nameField.getText();
        String password = passwordField.getText();

        if (userService.registerUser(name, username, email, password)) {
            showAlert("Registration Successful", "Welcome, " + username + "! You can now log in.");
            goToLogin();
        } else {
            showAlert("Registration Failed", "Username or email already taken.");
        }
    }

    private void goToLogin() {
        Stage stage = (Stage) registerButton.getScene().getWindow();
        SceneSwitcher.switchScene(stage, "/brain/brainstormer/hello-view.fxml", true);

    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
