package brain.brainstormer.controller;

import brain.brainstormer.service.ComponentService;
import brain.brainstormer.service.TemplateService;
import brain.brainstormer.utilGui.AddComponentDialog;
import brain.brainstormer.utils.SceneSwitcher;
import brain.brainstormer.utils.TemplateData;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.bson.Document;

import java.util.List;

public class TemplateController {

    @FXML
    private VBox templateContentArea;
    @FXML
    private Button homeButton;
    @FXML
    private Label templateTitle;
    @FXML
    private Label templateDescription;
    @FXML
    private Button addComponentButton;

    private final TemplateService templateService = new TemplateService();
    private final ComponentService componentService = ComponentService.getInstance(); // Access singleton instance

    @FXML
    private void initialize() {
        // Retrieve the template ID from TemplateData and load content
        String templateId = TemplateData.getInstance().getCurrentTemplateId();
        loadTemplateContent(templateId);

        // Set up buttons
        homeButton.setOnAction(event -> switchToHome());
        addComponentButton.setOnAction(event -> addComponent(templateId));
    }

    public void loadTemplateContent(String templateId) {
        Document templateData = templateService.getTemplateById(templateId);

        if (templateData != null) {
            // Set title and description
            templateTitle.setText(templateData.getString("name"));
            templateDescription.setText(templateData.getString("description"));

            // Clear previous content and load new content
            templateContentArea.getChildren().clear();

            // Retrieve components as a list of objects and cast each one individually
            List<Object> components = templateData.getList("components", Object.class);
            if (components != null && !components.isEmpty()) {
                for (Object componentObj : components) {
                    if (componentObj instanceof Document) {
                        Document component = (Document) componentObj;
                        String componentName = component.getString("name");

                        Control control = createComponentControl(componentName, component);
                        if (control != null) {
                            templateContentArea.getChildren().add(control);
                        } else {
                            // Fallback: show the name if control creation fails
                            Label componentLabel = new Label(componentName);
                            componentLabel.setStyle("-fx-text-fill: #E0E0E0; -fx-font-size: 14px;");
                            templateContentArea.getChildren().add(componentLabel);
                        }
                    }
                }
            } else {
                // Show a placeholder if the content area is empty
                Label emptyLabel = new Label("This page is empty. Start adding components!");
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

    private void addComponent(String templateId) {
        // Open the AddComponentDialog with the template ID and component service
        AddComponentDialog addComponentDialog = new AddComponentDialog(templateId, componentService);
        addComponentDialog.showComponentDialog();

        // Reload template content to reflect any new additions
        loadTemplateContent(templateId);
    }

    // Method to create the actual JavaFX control for each component type
    private Control createComponentControl(String componentName, Document component) {
        switch (componentName) {
            case "TextArea":
                String initialText = component.getString("initialText");
                int rows = component.getInteger("rows", 5); // Default to 5 if "rows" is not specified

                TextArea textArea = new TextArea(initialText);
                textArea.setPrefRowCount(rows);
                textArea.setWrapText(true);
                textArea.setStyle("-fx-control-inner-background: rgba(0, 0, 0, 0.7); -fx-text-fill: #FFFFFF; -fx-border-color: #666666; -fx-font-size: 14px; -fx-padding: 10px;");

                return textArea;

            // Add more cases for additional component types here

            default:
                System.out.println("Unknown component type: " + componentName);
                return null;
        }
    }
}