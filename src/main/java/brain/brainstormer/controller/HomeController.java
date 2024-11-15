package brain.brainstormer.controller;

import brain.brainstormer.components.essentials.GameComponent;
import brain.brainstormer.components.essentials.TemplateComponent;
import brain.brainstormer.utils.SceneSwitcher;
import brain.brainstormer.utils.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

public class HomeController {

    private final TemplateComponent templateComponent = new TemplateComponent();
    private final GameComponent gameComponent = new GameComponent();

    @FXML
    private Label welcomeLabel;
    @FXML
    private Button addButton, logoutButton;
    @FXML
    private VBox mainContentArea;
    @FXML
    private Button templatesButton;

    @FXML
    private Button networkButton;

    @FXML
    private Button gamesButton;

    @FXML
    private void initialize() {
        String username = SessionManager.getInstance().getUsername();
        welcomeLabel.setText("Hello, " + username + "!");


        loadTemplatesView();

        templatesButton.setOnAction(e -> loadTemplatesView());
        networkButton.setOnAction(e -> loadNetwork());
        gamesButton.setOnAction(e -> loadGames());


        addButton.setOnAction(e -> addTemplateDialog());
        logoutButton.setOnAction(event -> logout());
    }



    private void loadTemplatesView() {
        mainContentArea.getChildren().clear();
        templateComponent.loadTemplatesView(mainContentArea);
    }
    private void addTemplateDialog() {
        Stage dialog = new Stage();
        dialog.setTitle("Add New Template");
        dialog.initModality(Modality.APPLICATION_MODAL);

        Label nameLabel = new Label("Template Name:");
        TextField nameInput = new TextField();

        Label descLabel = new Label("Description:");
        TextField descInput = new TextField();

        Button saveButton = new Button("Add Template");
        saveButton.setOnAction(e -> {
            String name = nameInput.getText().trim();
            String description = descInput.getText().trim();

            if (!name.isEmpty() && !description.isEmpty()) {
                templateComponent.addTemplate(name, description);
                dialog.close();
                loadTemplatesView();
            } else {
                System.out.println("Both fields are required.");
            }
        });

        VBox layout = new VBox(10, nameLabel, nameInput, descLabel, descInput, saveButton);
        layout.setStyle("-fx-padding: 10;");
        dialog.setScene(new Scene(layout));
        dialog.show();
    }

    private void loadNetwork() {
        System.out.println("Network button clicked!");
    }

    private void loadGames() {
        mainContentArea.getChildren().clear();
        gameComponent.loadGameView(mainContentArea);
    }

    private void logout() {
        Stage stage = (Stage) logoutButton.getScene().getWindow();
        SessionManager.getInstance().clearSession();
        SceneSwitcher.switchScene(stage, "/brain/brainstormer/hello-view.fxml", true);
    }
}
