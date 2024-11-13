package brain.brainstormer.controller;

import brain.brainstormer.chess.ChessClient;
import brain.brainstormer.components.essentials.GameComponent;
import brain.brainstormer.components.essentials.TemplateComponent;
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
    private void initialize() {
        String username = SessionManager.getInstance().getUsername();
        welcomeLabel.setText("Hello, " + username + "!");

        templatesButton.setOnAction(event -> loadTemplatesView());
        networkButton.setOnAction(event -> loadNetwork());
        gamesButton.setOnAction(event -> loadGames());

        addButton.setOnAction(event -> showAddTemplateDialog());
        logoutButton.setOnAction(event -> logout());
    }

    private void logout() {
        System.out.println("logout");
        SessionManager.getInstance().clearSession();
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
}
