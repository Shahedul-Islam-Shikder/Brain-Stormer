package brain.brainstormer.components.essentials;

import brain.brainstormer.service.TemplateService;
import brain.brainstormer.utils.SceneSwitcher;
import brain.brainstormer.utils.SessionManager;
import brain.brainstormer.utils.TemplateData;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;
import org.bson.Document;
import java.util.List;
import java.util.function.Consumer;

public class TemplateComponent {

    private final TemplateService templateService = new TemplateService();

    public void loadTemplatesView(VBox mainContentArea) {
        mainContentArea.getChildren().clear();
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

    private HBox createTemplateBox(Document template) {
        HBox templateBox = new HBox(10);
        templateBox.setStyle("-fx-background-color: #1E1E1E; -fx-padding: 15; -fx-background-radius: 10;");
        VBox textContainer = new VBox(5);

        Label nameLabel = new Label(template.getString("name"));
        nameLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #E0E0E0;");

        Label descriptionLabel = new Label(template.getString("description"));
        descriptionLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #B0B0B0;");
        textContainer.getChildren().addAll(nameLabel, descriptionLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button deleteButton = new Button("Delete");
        deleteButton.setStyle("-fx-background-color: #B22222; -fx-text-fill: white; -fx-background-radius: 10; -fx-font-size: 12px;");

        templateBox.getChildren().addAll(textContainer, spacer, deleteButton);

        // Set the template ID and switch scenes
        templateBox.setOnMouseClicked(event -> {
            TemplateData.getInstance().setCurrentTemplateId(template.getObjectId("_id").toHexString());
            Stage stage = (Stage) templateBox.getScene().getWindow();
            SceneSwitcher.switchScene(stage, "/brain/brainstormer/template-view.fxml", true);
        });

        return templateBox;
    }


    public void addTemplate(String name, String description) {
        String userId = SessionManager.getInstance().getUserId();
        templateService.addTemplate(userId, name, description);
    }
}
