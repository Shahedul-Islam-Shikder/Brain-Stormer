package brain.brainstormer.controller;

import brain.brainstormer.components.core.ComponentFactory;
import brain.brainstormer.components.core.CoreComponent;
import brain.brainstormer.service.ComponentService;
import brain.brainstormer.service.TemplateService;
import brain.brainstormer.utilGui.AddComponentDialog;
import brain.brainstormer.utils.SceneSwitcher;
import brain.brainstormer.utils.TemplateData;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
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
    private final ComponentService componentService = ComponentService.getInstance();

    @FXML
    private void initialize() {
        String templateId = TemplateData.getInstance().getCurrentTemplateId();
        loadTemplateContent(templateId);
        homeButton.setOnAction(event -> switchToHome());
        addComponentButton.setOnAction(event -> addComponent(templateId));
    }

    public void loadTemplateContent(String templateId) {
        Document templateData = templateService.getTemplateById(templateId);

        if (templateData == null) {
            displayTemplateNotFound();
            return;
        }

        setTemplateDetails(templateData);
        List<Document> components = templateData.getList("components", Document.class);
        if (components == null || components.isEmpty()) {
            displayEmptyTemplateMessage();
        } else {
            addComponentsToTemplate(components);
        }
    }

    private void setTemplateDetails(Document templateData) {
        templateTitle.setText(templateData.getString("name"));
        templateDescription.setText(templateData.getString("description"));
        templateContentArea.getChildren().clear();
    }

    private void addComponentsToTemplate(List<Document> components) {
        for (Document componentDoc : components) {
            CoreComponent component = ComponentFactory.createComponent(componentDoc);

            if (component != null) {
                templateContentArea.getChildren().add(component.render());
            } else {
                addPlaceholderComponent(componentDoc);
            }
        }
    }

    private void addPlaceholderComponent(Document componentDoc) {
        String componentName = componentDoc.getString("name");
        Label componentLabel = new Label(componentName != null ? componentName : "Unnamed Component");
        componentLabel.setStyle("-fx-text-fill: #E0E0E0; -fx-font-size: 14px;");
        templateContentArea.getChildren().add(componentLabel);
    }

    private void displayEmptyTemplateMessage() {
        Label emptyLabel = new Label("This page is empty. Start adding components!");
        emptyLabel.setStyle("-fx-text-fill: #B0B0B0; -fx-font-size: 16px;");
        templateContentArea.getChildren().add(emptyLabel);
    }

    private void displayTemplateNotFound() {
        templateTitle.setText("Template not found");
        templateDescription.setText("");
        templateContentArea.getChildren().clear();

        Label notFoundLabel = new Label("The template could not be loaded.");
        notFoundLabel.setStyle("-fx-text-fill: #B0B0B0; -fx-font-size: 16px;");
        templateContentArea.getChildren().add(notFoundLabel);
    }








    private void switchToHome() {
        Stage stage = (Stage) homeButton.getScene().getWindow();
        SceneSwitcher.switchScene(stage, "/brain/brainstormer/home.fxml", true);
    }

    private void addComponent(String templateId) {
        AddComponentDialog addComponentDialog = new AddComponentDialog(templateId, componentService);
        addComponentDialog.init();
        loadTemplateContent(templateId);
    }

}
