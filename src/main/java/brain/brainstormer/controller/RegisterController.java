package brain.brainstormer.controller;

import brain.brainstormer.service.UserService;
import brain.brainstormer.utilGui.AlertUtil;
import brain.brainstormer.config.SceneSwitcher;
import brain.brainstormer.utils.StyleUtil;
import javafx.fxml.FXML;
import javafx.scene.control.*;
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

    @FXML
    private Label usernameErrorLabel;

    @FXML
    private Label emailErrorLabel;

    @FXML
    private Label nameErrorLabel;

    @FXML
    private Label passwordErrorLabel;

    private final UserService userService;

    public RegisterController() {
        this.userService = new UserService();
    }

    @FXML
    private void initialize() {
        // Apply global and custom styles

        registerButton.sceneProperty().addListener((observable, oldScene, newScene) -> {
            if (newScene != null) {
                // Scene is now available
                StyleUtil.applyCustomStylesheet(registerButton.getScene(), "/styles/auth/login-register.css");

            }
        });

        // Set up button actions
        registerButton.setOnAction(event -> registerUser());
        loginButton.setOnAction(event -> goToLogin());
    }

    private void registerUser() {
        String username = usernameField.getText();
        String email = emailField.getText();
        String name = nameField.getText();
        String password = passwordField.getText();

        boolean isValid = true;

        // Step 1: Validate username
        if (!username.matches("^[a-z0-9._]+$")) {
            usernameErrorLabel.setText("Username must contain only lowercase letters, dots, or underscores.");

            isValid = false;
        } else {
            usernameErrorLabel.setText("");
            usernameField.setStyle("");
        }

        // Step 2: Validate email
        if (!email.matches("^[\\w._%+-]+@[\\w.-]+\\.[a-zA-Z]{2,}$")) {
            emailErrorLabel.setText("Enter a valid email address.");

            isValid = false;
        } else {
            emailErrorLabel.setText("");
            emailField.setStyle("");
        }

        // Check Name too only A-z

        if (!name.matches("^[a-zA-Z]+$")) {
            nameErrorLabel.setText("Name must contain only letters.");

            isValid = false;
        } else {
            nameErrorLabel.setText("");
            nameField.setStyle("");
        }


        // Step 3: Validate password
        if (password.length() < 8) {
            passwordErrorLabel.setText("Password must be at least 8 characters.");

            isValid = false;
        } else {
            passwordErrorLabel.setText("");
            passwordField.setStyle("");
        }

        // Step 4: Show validation error popup if validation fails
        if (!isValid) {

            return; // Exit early if validation fails
        }

        // Step 5: Attempt registration with the backend
        if (userService.registerUser(name, username, email, password)) {
            // Registration successful
            AlertUtil.showAlert("Registration Successful", "Welcome, " + username + "! You can now log in.");
            goToLogin(); // Redirect to login screen
        } else {
            // Backend registration failed
            AlertUtil.showError("Registration Failed", "Username or email already taken.");
        }
    }


    private void goToLogin() {
        Stage stage = (Stage) registerButton.getScene().getWindow();
        SceneSwitcher.switchScene(stage, "/brain/brainstormer/hello-view.fxml", true,"Login");
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
