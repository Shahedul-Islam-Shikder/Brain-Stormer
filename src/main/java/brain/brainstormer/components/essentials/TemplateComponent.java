package brain.brainstormer.components.essentials;

import brain.brainstormer.controller.LoginController;
import brain.brainstormer.service.TemplateService;
import brain.brainstormer.utilGui.AlertUtil;
import brain.brainstormer.utils.SceneSwitcher;
import brain.brainstormer.utils.SessionManager;
import brain.brainstormer.utils.StyleUtil;
import brain.brainstormer.utils.TemplateData;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.bson.Document;

import java.util.List;

public class TemplateComponent {

    private final TemplateService templateService = TemplateService.getInstance();

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
                HBox templateBox = createTemplateBox(template, mainContentArea);
                mainContentArea.getChildren().add(templateBox);
            }
        }
    }

    public void loadTemplateButtons(HBox templateButtonRow) {
        templateButtonRow.getChildren().clear();

        String userId = SessionManager.getInstance().getUserId();
        List<Document> templates = templateService.getUserTemplates(userId);

        if (templates.isEmpty()) {
            Button placeholderButton = new Button("No templates available");
            placeholderButton.setStyle("-fx-background-color: #333333; -fx-text-fill: #E0E0E0; -fx-font-size: 14px; -fx-background-radius: 10;");
            templateButtonRow.getChildren().add(placeholderButton);
        } else {
            for (Document template : templates) {
                Button templateButton = new Button(template.getString("name"));
                templateButton.setStyle("-fx-background-color: #333333; -fx-text-fill: #E0E0E0; -fx-font-size: 14px; -fx-background-radius: 10;");

                templateButton.setOnAction(e -> handleTemplateButtonClick(template, templateButton));
                templateButtonRow.getChildren().add(templateButton);
            }
        }
    }

    public void addTemplate(String name, String description, String type) {
        String userId = SessionManager.getInstance().getUserId();
        templateService.addTemplate(userId, name, description, type);
    }

    private void handleTemplateButtonClick(Document template, Button templateButton) {
        String templateId = template.getObjectId("_id").toHexString();
        TemplateData.getInstance().setCurrentTemplateId(templateId);



        Stage stage = (Stage) templateButton.getScene().getWindow();
        SceneSwitcher.switchScene(stage, "/brain/brainstormer/template-view.fxml", true);
    }

    private HBox createTemplateBox(Document template, VBox parentContainer) {
        HBox templateBox = new HBox(10);
        templateBox.setStyle("-fx-background-color: #1E1E1E; -fx-padding: 15; -fx-background-radius: 10;");
        VBox textContainer = new VBox(5);

        Label nameLabel = new Label(template.getString("name"));
        nameLabel.getStyleClass().add("label-text");

        Label descriptionLabel = new Label(template.getString("description"));
        descriptionLabel.getStyleClass().add("label-text");

        String templateType = template.getString("type");
        Label typeLabel = new Label("Type: " + templateType);
        typeLabel.getStyleClass().add("label-type");

        textContainer.getChildren().addAll(nameLabel, descriptionLabel, typeLabel);
//        StyleUtil.applyStylesheet(textContainer);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button deleteButton = createDeleteButton(template, parentContainer);
        Button editButton = createEditDialog(template, parentContainer);
        deleteButton.getStyleClass().add("button-danger");
        editButton.getStyleClass().add("button-secondary");

        templateBox.getChildren().addAll(textContainer, spacer, editButton, deleteButton);

        templateBox.setOnMouseClicked(event -> {
            TemplateData.getInstance().setCurrentTemplateId(template.getObjectId("_id").toHexString());
            TemplateData.getInstance().setCurrentTemplateType(templateType);

            // Check if "userId" exists and is not null
            String userId = template.getString("userId");
            if (userId != null && userId.equals(SessionManager.getInstance().getUserId())) {
                // Author is the userId
                TemplateData.getInstance().setAuthor(SessionManager.getInstance().getUserId());
                System.out.println("userId: " + userId);
                System.out.println("24"+TemplateData.getInstance().getAuthor());
            }

            Stage stage = (Stage) templateBox.getScene().getWindow();
            SceneSwitcher.switchScene(stage, "/brain/brainstormer/template-view.fxml", true);
        });


        return templateBox;
    }

    private Button createEditDialog(Document template, VBox parentContainer) {
        Button editButton = new Button("Edit");
        editButton.getStyleClass().add("button-secondary");
        editButton.setOnAction(event -> {
            Stage dialog = new Stage();
            dialog.setMinHeight(400);
            dialog.setMinWidth(400);
            dialog.setResizable(false);
            dialog.setTitle("Edit Template");
            dialog.initModality(Modality.APPLICATION_MODAL);

            // Input fields
            TextField nameInput = new TextField(template.getString("name"));
            nameInput.getStyleClass().add("input-field");

            TextArea descInput = new TextArea(template.getString("description"));
            descInput.getStyleClass().add("text-area");

            TextField typeInput = new TextField(template.getString("type"));
            typeInput.getStyleClass().add("input-field");

            // Save button

            Button saveButton = new Button("Save Changes");
            saveButton.getStyleClass().add("button-primary");
            HBox buttonContainer = new HBox(saveButton);
            buttonContainer.setAlignment(Pos.CENTER);
            saveButton.setOnAction(e -> {
                String name = nameInput.getText().trim();
                String description = descInput.getText().trim();
                String type = typeInput.getText().trim();

                if (!name.isEmpty() && !description.isEmpty() && !type.isEmpty()) {
                    String templateId = template.getObjectId("_id").toHexString();
                    templateService.updateTemplate(templateId, name, description, type);
                    loadTemplatesView(parentContainer);
                    dialog.close();
                } else {
                    AlertUtil.showAlert("Field Empty", "All fields must be filled."); // Fixed the alert call
                }
            });

            // Layout for dialog
            VBox layout = new VBox(10,
                    new Label("Name:"), nameInput,
                    new Label("Description:"), descInput,
                    new Label("Type:"), typeInput,
                    buttonContainer
            );
            layout.getStyleClass().add("container");

            // Create scene and apply styles
            Scene scene = new Scene(layout);
            StyleUtil.applyCustomStylesheet(scene, "/styles/base/global.css"); // Example of applying custom dialog styles

            // Set scene and show dialog
            dialog.setScene(scene);
            dialog.show();
        });
        return editButton;
    }


    private Button createDeleteButton(Document template, VBox parentContainer) {
        Button deleteButton = new Button("Delete");
        deleteButton.getStyleClass().add("button-danger");
        deleteButton.setOnAction(event -> {
            String templateId = template.getObjectId("_id").toHexString();
            templateService.deleteTemplate(templateId);
            loadTemplatesView(parentContainer);
        });
        return deleteButton;
    }
}
