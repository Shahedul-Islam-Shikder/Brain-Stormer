package brain.brainstormer.components.essentials;

import brain.brainstormer.service.TemplateService;
import brain.brainstormer.utils.SessionManager;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Priority;
import org.bson.Document;

import java.text.SimpleDateFormat;
import java.util.List;

public class TemplateComponent {

    private final TemplateService templateService = new TemplateService();

    // Method to load templates into the main content area
    public void loadTemplatesView(VBox mainContentArea) {
        mainContentArea.getChildren().clear();

        // Fetch templates for the logged-in user
        String userId = SessionManager.getInstance().getUserId();
        List<Document> templates = templateService.getUserTemplates(userId);

        if (templates.isEmpty()) {
            Label noTemplatesLabel = new Label("No templates available. Create one to get started!");
            noTemplatesLabel.setStyle("-fx-text-fill: #B0B0B0; -fx-font-size: 16px;");
            mainContentArea.getChildren().add(noTemplatesLabel);
        } else {
            for (Document template : templates) {
                HBox templateBox = createTemplateBox(template);
                mainContentArea.getChildren().add(templateBox);
            }
        }
    }

    // Method to create a template box
    private HBox createTemplateBox(Document template) {
        HBox templateBox = new HBox(10);
        templateBox.setStyle("-fx-background-color: #1E1E1E; -fx-padding: 15; -fx-background-radius: 10;");

        VBox textContainer = new VBox(5);

        // Display the ObjectId above the title
        Label objectIdLabel = new Label("ID: " + template.getObjectId("_id").toString());
        objectIdLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #A0A0A0;");  // Grayish color for the ID

        Label nameLabel = new Label(template.getString("name"));
        nameLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #E0E0E0;");

        Label descriptionLabel = new Label(template.getString("description"));
        descriptionLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #B0B0B0;");

        textContainer.getChildren().addAll(objectIdLabel, nameLabel, descriptionLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        String dateCreated = new SimpleDateFormat("dd/MM/yyyy").format(template.getDate("dateCreated"));
        Label dateLabel = new Label("Date: " + dateCreated);
        dateLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #B0B0B0;");

        Button editButton = new Button("Edit");
        editButton.setStyle("-fx-background-color: #1A1A5A; -fx-text-fill: white; -fx-background-radius: 10; -fx-font-size: 12px;");
        editButton.setOnAction(event -> editTemplate(template));

        Button deleteButton = new Button("Delete");
        deleteButton.setStyle("-fx-background-color: #B22222; -fx-text-fill: white; -fx-background-radius: 10; -fx-font-size: 12px;");
        deleteButton.setOnAction(event -> deleteTemplate(template));

        templateBox.getChildren().addAll(textContainer, spacer, dateLabel, editButton, deleteButton);

        return templateBox;
    }


    // Method to edit template
    private void editTemplate(Document template) {
        // Implement edit functionality here
        System.out.println("Edit template: " + template.getString("name"));
    }

    // Method to delete template
    private void deleteTemplate(Document template) {
        // Implement delete functionality here
        System.out.println("Delete template: " + template.getString("name"));
    }

    // Method to add a new template
    public void addTemplate(String name, String description) {
        String userId = SessionManager.getInstance().getUserId();
        templateService.addTemplate(userId, name, description);
    }
}
