package brain.brainstormer.controller;

import brain.brainstormer.components.essentials.GameComponent;
import brain.brainstormer.components.essentials.TemplateComponent;
import brain.brainstormer.service.ComponentService;
import brain.brainstormer.utils.SceneSwitcher;
import brain.brainstormer.utils.SessionManager;
import brain.brainstormer.utils.StyleUtil;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.List;

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
    private HBox templateButtonRow;  // HBox for template buttons

    @FXML
    private void initialize() {
        String username = SessionManager.getInstance().getUsername();
        welcomeLabel.setText("Hello, " + username + "!");

        // Load the template buttons into the HBox
        templateComponent.loadTemplateButtons(templateButtonRow);

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

    private void addTemplateDialog() {
        Stage dialog = new Stage();
        dialog.setMinHeight(400);
        dialog.setMinWidth(400);
        dialog.setResizable(false);
        dialog.setTitle("Add New Template");
        dialog.initModality(Modality.APPLICATION_MODAL);

        // Template Name
        Label nameLabel = new Label("Template Name:");
        nameLabel.getStyleClass().add("label-text");
        TextField nameInput = new TextField();
        nameInput.getStyleClass().add("input-field");
        nameInput.setPromptText("Enter Template name");

        // Description
        Label descLabel = new Label("Description:");
        descLabel.getStyleClass().add("label-text");
        TextArea descInput = new TextArea();
        descInput.getStyleClass().add("text-area");
        descInput.setPromptText("Enter Description");

        // Template Type (Dropdown)
        Label typeLabel = new Label("Template Type:");
        typeLabel.getStyleClass().add("label-text");
        ComboBox<String> typeDropdown = new ComboBox<>();
        typeDropdown.getItems().addAll("private", "public");
        typeDropdown.setValue("private"); // Default to "private"
        typeDropdown.getStyleClass().add("combo-box");

        // Save Button
        Button saveButton = new Button("Add Template");
        saveButton.getStyleClass().add("button-primary");

        saveButton.setOnAction(e -> {
            String name = nameInput.getText().trim();
            String description = descInput.getText().trim();
            String type = typeDropdown.getValue(); // Get the selected type

            if (!name.isEmpty() && !description.isEmpty() && type != null) {
                // Pass the type to the TemplateComponent's addTemplate method
                templateComponent.addTemplate(name, description, type);
                loadTemplatesView();
                templateComponent.loadTemplateButtons(templateButtonRow); // Reload template buttons after adding
                dialog.close();
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Field Empty");
                alert.setContentText("All fields must be filled!");
                alert.show();
            }
        });

        HBox saveButtonCentre = new HBox(saveButton);
        saveButtonCentre.setAlignment(Pos.CENTER);

        // Layout
        VBox layout = new VBox(10, nameLabel, nameInput, descLabel, descInput, typeLabel, typeDropdown, saveButtonCentre);
        layout.getStyleClass().add("container");

        Scene scene = new Scene(layout);
        StyleUtil.applyStylesheet(scene);

        dialog.setScene(scene);
        dialog.show();
    }

}
