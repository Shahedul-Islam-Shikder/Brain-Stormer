package brain.brainstormer.controller;

import brain.brainstormer.service.TemplateService;
import brain.brainstormer.utils.SceneSwitcher;
import brain.brainstormer.utils.TemplateData;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.bson.Document;

public class TemplateController {

    @FXML
    private VBox templateContentArea;
    @FXML
    private Button homeButton;
    @FXML
    private Label templateTitle;
    @FXML
    private Label templateDescription;

    private final TemplateService templateService = new TemplateService();

    @FXML
    private void initialize() {
        // Retrieve the template ID from TemplateData and load content
        String templateId = TemplateData.getInstance().getCurrentTemplateId();
        loadTemplateContent(templateId);

        // Set up the home button to switch back to home
        homeButton.setOnAction(event -> switchToHome());
    }

    public void loadTemplateContent(String templateId) {
        // Fetch the template document by ID using TemplateService
        Document templateData = templateService.getTemplateById(templateId);

        // Check if templateData is not null before accessing fields
        if (templateData != null) {
            // Set title and description
            templateTitle.setText(templateData.getString("name"));
            templateDescription.setText(templateData.getString("description"));

            // Clear previous content and load new content
            templateContentArea.getChildren().clear();

            // Check if the template has any content components
            if (templateData.containsKey("content") && !templateData.getList("content", String.class).isEmpty()) {
                for (String content : templateData.getList("content", String.class)) {
                    Label contentLabel = new Label(content);
                    contentLabel.setStyle("-fx-text-fill: #E0E0E0; -fx-font-size: 14px;");
                    templateContentArea.getChildren().add(contentLabel);
                }
            } else {
                // Show a placeholder if the content area is empty
                Label emptyLabel = new Label("This page is empty. Start adding content!");
                emptyLabel.setStyle("-fx-text-fill: #B0B0B0; -fx-font-size: 16px;");
                templateContentArea.getChildren().add(emptyLabel);
            }
        } else {
            // Handle case where the template ID is invalid or the template was not found
            templateTitle.setText("Template not found");
            templateDescription.setText("");
            templateContentArea.getChildren().clear();
            Label notFoundLabel = new Label("The template could not be loaded.");
            notFoundLabel.setStyle("-fx-text-fill: #B0B0B0; -fx-font-size: 16px;");
            templateContentArea.getChildren().add(notFoundLabel);
        }
    }

    private void switchToHome() {
        Stage stage = (Stage) homeButton.getScene().getWindow();
        SceneSwitcher.switchScene(stage, "/brain/brainstormer/home.fxml", true);
    }
}
