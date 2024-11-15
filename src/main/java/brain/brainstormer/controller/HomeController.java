package brain.brainstormer.controller;

import brain.brainstormer.chess.ChessClient;
import brain.brainstormer.components.essentials.GameComponent;
import brain.brainstormer.components.essentials.TemplateComponent;
import brain.brainstormer.utils.SceneSwitcher;
import brain.brainstormer.utils.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class HomeController {

    private final TemplateComponent templateComponent = new TemplateComponent();
    private final GameComponent gameComponent = new GameComponent();

    @FXML
    private Button templatesButton, networkButton, gamesButton;
    @FXML
    private Label welcomeLabel;
    @FXML
    private Button addButton;
    @FXML
    private Button logoutButton;
    @FXML
    private VBox mainContentArea;
    @FXML
    private VBox sidebarArea;

    @FXML
    private void initialize() {
        String username = SessionManager.getInstance().getUsername();
        welcomeLabel.setText("Hello, " + username + "!");

        homeSideBar();




    }

    private void logout() {
        System.out.println("logout");
        Stage stage = (Stage) logoutButton.getScene().getWindow();
        SessionManager.getInstance().clearSession();
        SceneSwitcher.switchScene(stage, "/brain/brainstormer/hello-view.fxml", true);
    }

    private void loadTemplatesView() {
        mainContentArea.getChildren().clear();
        addButton.setVisible(true);
        templateComponent.loadTemplatesView(mainContentArea);
    }

    private void showAddTemplateDialog() {
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
        System.out.println("Games button clicked!");
        mainContentArea.getChildren().clear();
        gameComponent.loadGameView(mainContentArea);
    }


    // Method to clear the main content area and set up the standard sidebar
    private void homeSideBar() {
        // Clear sidebar and add standard buttons
        sidebarArea.getChildren().clear();

        VBox buttonContainer = new VBox(10);
        buttonContainer.setStyle("-fx-padding: 40 0;");

        Button templatesButton = new Button("Templates");
        templatesButton.setId("templatesButton"); // Assign fx:id programmatically
        templatesButton.setStyle("-fx-background-color: transparent; -fx-text-fill: #B0B0B0; -fx-font-size: 16px; -fx-padding: 10 0; -fx-border-color: #B0B0B0; -fx-border-width: 0 0 1 0; -fx-border-style: solid; -fx-pref-width: 280px; -fx-alignment: CENTER_LEFT;");

        Button networkButton = new Button("Network");
        networkButton.setId("networkButton"); // Assign fx:id programmatically
        networkButton.setStyle("-fx-background-color: transparent; -fx-text-fill: #B0B0B0; -fx-font-size: 16px; -fx-padding: 10 0; -fx-border-color: #B0B0B0; -fx-border-width: 0 0 1 0; -fx-border-style: solid; -fx-pref-width: 280px; -fx-alignment: CENTER_LEFT;");

        Button gamesButton = new Button("Games");
        gamesButton.setId("gamesButton"); // Assign fx:id programmatically
        gamesButton.setStyle("-fx-background-color: transparent; -fx-text-fill: #B0B0B0; -fx-font-size: 16px; -fx-padding: 10 0; -fx-border-color: #B0B0B0; -fx-border-width: 0 0 1 0; -fx-border-style: solid; -fx-pref-width: 280px; -fx-alignment: CENTER_LEFT;");

        // Add buttons to the container
        buttonContainer.getChildren().addAll(templatesButton, networkButton, gamesButton);

        // Add the container to the sidebar
        sidebarArea.getChildren().add(buttonContainer);

        templatesButton.setOnAction(event -> loadTemplatesView());
        networkButton.setOnAction(event -> loadNetwork());
        gamesButton.setOnAction(event -> loadGames());

        addButton.setOnAction(event -> showAddTemplateDialog());
        logoutButton.setOnAction(event -> logout());


    }
}
