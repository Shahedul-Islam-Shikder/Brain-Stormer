package brain.brainstormer.controller;

import brain.brainstormer.service.TemplateService;
import brain.brainstormer.utils.SessionManager;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.Modality;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import org.bson.Document;

import java.text.SimpleDateFormat;
import java.util.List;

public class HomeController {

    private final TemplateService templateService = new TemplateService();

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
        // Retrieve the username from SessionManager
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
        // Code to redirect to the login screen can go here
    }

    private void loadTemplatesView() {
        mainContentArea.getChildren().clear();
        addButton.setVisible(true);

        // Fetch templates for the logged-in user
        String userId = SessionManager.getInstance().getUserId();
        List<Document> templates = templateService.getUserTemplates(userId);

        if (templates.isEmpty()) {
            Label noTemplatesLabel = new Label("No templates available. Create one to get started!");
            noTemplatesLabel.setStyle("-fx-text-fill: #B0B0B0; -fx-font-size: 16px;");
            mainContentArea.getChildren().add(noTemplatesLabel);
        } else {
            for (Document template : templates) {
                HBox templateBox = createTemplateBox(template);  // Fixed: add HBox directly
                mainContentArea.getChildren().add(templateBox);
            }
        }
    }

    private HBox createTemplateBox(Document template) {
        HBox templateBox = new HBox(10);
        templateBox.setStyle("-fx-background-color: #1E1E1E; -fx-padding: 15; -fx-background-radius: 10;");
        templateBox.setAlignment(Pos.CENTER_LEFT);

        // VBox for title and description
        VBox textContainer = new VBox(5);
        Label nameLabel = new Label(template.getString("name"));
        nameLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #E0E0E0;");
        Label descriptionLabel = new Label(template.getString("description"));
        descriptionLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #B0B0B0;");
        textContainer.getChildren().addAll(nameLabel, descriptionLabel);

        // Spacer region
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Date label
        String dateCreated = new SimpleDateFormat("dd/MM/yyyy").format(template.getDate("dateCreated"));
        Label dateLabel = new Label("Date: " + dateCreated);
        dateLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #B0B0B0;");

        // Edit button
        Button editButton = new Button("Edit");
        editButton.setStyle("-fx-background-color: #1A1A5A; -fx-text-fill: white; -fx-background-radius: 10; -fx-font-size: 12px;");
        editButton.setOnAction(event -> editTemplate(template));

        // Delete button
        Button deleteButton = new Button("Delete");
        deleteButton.setStyle("-fx-background-color: #B22222; -fx-text-fill: white; -fx-background-radius: 10; -fx-font-size: 12px;");
        deleteButton.setOnAction(event -> deleteTemplate(template));

        // Add components to HBox
        templateBox.getChildren().addAll(textContainer, spacer, dateLabel, editButton, deleteButton);

        return templateBox;
    }

    private void editTemplate(Document template) {
        // Implement edit functionality here
        System.out.println("Edit template: " + template.getString("name"));
    }

    private void deleteTemplate(Document template) {
        // Implement delete functionality here
        System.out.println("Delete template: " + template.getString("name"));
    }

    private void showAddTemplateDialog() {
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
        descInput.setStyle("-fx-pref-width: 380px; -fx-max-width: 380px; -fx-pref-height: 180px; fx-max-height: 180px; " +
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
                addTemplate(name, description);
                dialog.close();
            } else {
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

    private void addTemplate(String name, String description) {
        String userId = SessionManager.getInstance().getUserId();
        templateService.addTemplate(userId, name, description);
        loadTemplatesView();  // Refresh the view to show the new template
    }

    private void loadNetwork() {
        System.out.println("Network button clicked!");
        // Placeholder for network view
    }

    private void loadGames() {
        System.out.println("Games button clicked!");
        // Placeholder for games view
    }
}
