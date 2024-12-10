package brain.brainstormer.controller;

import brain.brainstormer.utilGui.AlertUtil;
import brain.brainstormer.utils.SceneSwitcher;
import brain.brainstormer.utils.StyleUtil;
import brain.brainstormer.service.UserService;
import brain.brainstormer.utils.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Button loginButton;

    @FXML
    private Hyperlink registerButton;

    @FXML
    private Label usernameErrorLabel;

    @FXML
    private Label passwordErrorLabel;

    private final UserService userService;

    public LoginController() {
        this.userService = new UserService();
    }

    @FXML

    private void initialize() {
        // Wait until the scene is attached
        loginButton.sceneProperty().addListener((observable, oldScene, newScene) -> {
            if (newScene != null) {
                // Scene is now available
                StyleUtil.applyCustomStylesheet(loginButton.getScene(), "/styles/auth/login-register.css");
                System.out.println(newScene); // Should print the Scene object
            }
        });

        // Set up button actions
        loginButton.setOnAction(event -> loginUser());
        registerButton.setOnAction(event -> goToRegister());
    }

    private void loginUser() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        boolean isValid = true;

        // Step 1: Validate username
        if (!username.matches("^[a-z0-9._]+$")) {
            usernameErrorLabel.setText("Username must contain only lowercase letters, dots, or underscores.");
            usernameErrorLabel.setStyle("-fx-text-fill: #ff4d4d;");
            usernameField.setStyle("-fx-border-color: #ff4d4d;");
            isValid = false;
        } else {
            usernameErrorLabel.setText("");
            usernameField.setStyle("");
        }

        // Step 2: Validate password
        if (password.isEmpty() || password.length() < 8) {
       //Todo: use passwordErrorLabel.setText("Password must be at least 8 characters long.");
            passwordField.setStyle("-fx-border-color: #ff4d4d;");
            passwordErrorLabel.setText("Password must be at least 8 characters long.");
            // add .label-error  to passwordErrorLabel
//            passwordErrorLabel.setStyle("-fx-text-fill: #ff4d4d;");
            isValid = false;
        } else {
            passwordField.setStyle("");
        }

        // Step 3: Show validation error popup if validation fails
        if (!isValid) {

            return; // Exit early if validation fails
        }

        // Step 4: Attempt login with the backend
        if (userService.loginUser(username, password)) {


            goToHome(); // Redirect to home screen
        } else {
            // Backend login failed
            AlertUtil.showError("Login Failed", "Invalid username or password.");
        }
    }


    private void goToRegister() {
        Stage stage = (Stage) loginButton.getScene().getWindow();
        SceneSwitcher.switchScene(stage, "/brain/brainstormer/register.fxml", true, "Login");
    }

    private void goToHome() {
        Stage stage = (Stage) loginButton.getScene().getWindow();
        SceneSwitcher.switchScene(stage, "/brain/brainstormer/home.fxml", true, "Home");
    }

    public void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
