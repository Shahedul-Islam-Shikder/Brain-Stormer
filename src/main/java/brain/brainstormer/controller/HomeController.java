package brain.brainstormer.controller;

import brain.brainstormer.components.essentials.GameComponent;
import brain.brainstormer.components.essentials.TemplateComponent;
import brain.brainstormer.utils.SceneSwitcher;
import brain.brainstormer.utils.SessionManager;
import brain.brainstormer.utils.StyleUtil;
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

    //TODO make style moduler && seperate the gui and the logic

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
        nameLabel.getStyleClass().add("label-text");

        TextField nameInput = new TextField();
        nameInput.setPromptText("Enter Template name");
        nameInput.getStyleClass().add("input-field");

        Label descLabel = new Label("Description:");
        descLabel.getStyleClass().add("label-text");

        TextArea descInput = new TextArea();
        descInput.setPromptText("Enter Description");
        descInput.getStyleClass().add("text-area");

        Button saveButton = new Button("Add Template");
        saveButton.getStyleClass().add("button-primary");
        saveButton.setOnAction(e -> {
            String name = nameInput.getText().trim();
            String description = descInput.getText().trim();

            if (!name.isEmpty() && !description.isEmpty()) {
                templateComponent.addTemplate(name, description);
                loadTemplatesView();
                dialog.close();
            } else {
                LoginController.showAlert("Field Empty", "Both fields must be used.");
            }
        });

        HBox saveButtonCentre = new HBox(saveButton);
        saveButtonCentre.setAlignment(Pos.CENTER);

        VBox layout = new VBox(10, nameLabel, nameInput, descLabel, descInput, saveButtonCentre);
        layout.getStyleClass().add("container");

        Scene scene = new Scene(layout);
        StyleUtil.applyStylesheet(scene);

        dialog.setScene(scene);
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
