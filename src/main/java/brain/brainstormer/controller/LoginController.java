package brain.brainstormer.controller;

import brain.brainstormer.service.UserService;
import brain.brainstormer.utils.SceneSwitcher;
import brain.brainstormer.utils.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.bson.Document;

public class LoginController {

    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Button loginButton;
    @FXML
    private Hyperlink registerButton;

    private final UserService userService;

    public LoginController() {
        userService = new UserService();
    }

    @FXML
    private void initialize() {
        loginButton.setOnAction(event -> loginUser());
        registerButton.setOnAction(event -> goToRegister());
    }

    private void loginUser() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        // Authenticate user
        if (userService.loginUser(username, password)) {
            Document userDocument = userService.getUser(username); // Get user document

            // Set session data
            SessionManager.getInstance().setUserId(userDocument.getObjectId("_id").toHexString());
            SessionManager.getInstance().setUsername(username);
            SessionManager.getInstance().setEmail(userDocument.getString("email"));

            //showAlert("Login Successful", "Welcome back, " + username + "!");
            goToHome(); // Transition to home screen
        } else {
            showAlert("Login Failed", "Invalid username or password.");
        }
    }

    private void goToRegister() {
        Stage stage = (Stage) loginButton.getScene().getWindow();
        SceneSwitcher.switchScene(stage, "/brain/brainstormer/register.fxml", true);
    }

    private void goToHome() {
        Stage stage = (Stage) loginButton.getScene().getWindow();
        SceneSwitcher.switchScene(stage, "/brain/brainstormer/home.fxml", true);
    }

    public static void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
