package brain.brainstormer.controller;

import brain.brainstormer.components.essentials.GameComponent;
import brain.brainstormer.components.essentials.TemplateComponent;
import brain.brainstormer.utils.SceneSwitcher;
import brain.brainstormer.utils.SessionManager;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
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
        dialog.setMinHeight(400);
        dialog.setMinWidth(400);
        dialog.setResizable(false);
        dialog.setTitle("Add New Template");
        dialog.initModality(Modality.APPLICATION_MODAL);

        Label nameLabel = new Label("Template Name:");
        nameLabel.setStyle("-fx-text-fill: #f7f5f5");
        TextField nameInput = new TextField();
        nameInput.setPromptText("Enter Template name");
        nameInput.setStyle("-fx-pref-width: 380px; -fx-max-width: 380px; -fx-background-radius: 10; -fx-border-radius: 10;" +
                " -fx-padding: 10; -fx-background-color: #333333; -fx-text-fill: #E0E0E0; -fx-prompt-text-fill: #777777;");

        Label descLabel = new Label("Description:");
        descLabel.setStyle("-fx-text-fill: #f7f5f5");
        TextArea descInput = new TextArea();
        descInput.setPromptText("Enter Description");
        descInput.setStyle("-fx-pref-width: 380px; -fx-max-width: 380px; -fx-pref-height: 180px; -fx-max-height: 180px; " +
                "-fx-background-radius: 10; -fx-border-radius: 0;" + " -fx-padding: 10; -fx-background-color: #333333;" +
                " -fx-text-fill: #E0E0E0; -fx-prompt-text-fill: #777777; -fx-control-inner-background: #333333;" +
                "-fx-prompt-text-fill: #777777;" + "-fx-border-color: transparent; -fx-focus-color: transparent; " +
                "-fx-faint-focus-color: transparent; -fx-effect: none;");
        descInput.setWrapText(true);
        descInput.setPrefRowCount(6);



        Button saveButton = new Button("Add Template");
        saveButton.setStyle("-fx-font-size: 18px; -fx-background-color: #1A1A5A; -fx-text-fill: white; -fx-background-radius: 10;");
        saveButton.setOnAction(e -> {
            String name = nameInput.getText().trim();
            String description = descInput.getText().trim();

            if (!name.isEmpty() && !description.isEmpty()) {
                templateComponent.addTemplate(name, description);
                loadTemplatesView();
                dialog.close();
            } else {
                LoginController.showAlert("Field Empty","Both field must be used.");
                System.out.println("Both fields are required.");
            }
        });
        HBox saveButtonCentre = new HBox(saveButton);
        saveButtonCentre.setAlignment(Pos.CENTER);
        VBox layout = new VBox(10, nameLabel, nameInput, descLabel, descInput, saveButtonCentre);
        layout.setStyle("-fx-padding: 10; -fx-background-color: #121212");
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
