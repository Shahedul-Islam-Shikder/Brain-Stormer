package brain.brainstormer.components.essentials;

import brain.brainstormer.controller.LoginController;
import brain.brainstormer.service.TemplateService;
import brain.brainstormer.utils.SceneSwitcher;
import brain.brainstormer.utils.SessionManager;
import brain.brainstormer.utils.TemplateData;
import com.mongodb.client.model.Filters;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Priority;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.bson.Document;
import org.bson.types.ObjectId;

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
        deleteButton.setOnAction(e ->{

            //gets template ID
            String templateId = template.getObjectId("_id").toHexString();

            //for testing purpose
            System.out.println(templateId);

            //deletes template from DB
            templateService.deleteTemplate(templateId);

            //refreshes the main content area
            VBox parentContainer = (VBox) templateBox.getParent();
            loadTemplatesView(parentContainer);
        });

        Button editButton = new Button("Edit");
        editButton.setStyle("-fx-background-color:  #1A1A5A; -fx-text-fill: white; -fx-background-radius: 10; " +
                "-fx-font-size: 12px;-fx-pref-height: 20px; -fx-pref-width: 50px");
        editButton.setOnAction(ed ->{
            Stage dialog = new Stage();
            dialog.setMinHeight(400);
            dialog.setMinWidth(400);
            dialog.setResizable(false);
            dialog.setTitle("Edit Template");
            dialog.initModality(Modality.APPLICATION_MODAL);

            Label nameFieldLabel = new Label("Template Name:");
            nameFieldLabel.setStyle("-fx-text-fill: #f7f5f5");
            TextField nameInput2 = new TextField();
            String currentName = nameLabel.getText();
            nameInput2.setText(currentName);
            nameInput2.setStyle("-fx-pref-width: 380px; -fx-max-width: 380px; -fx-background-radius: 10; -fx-border-radius: 10;" +
                    " -fx-padding: 10; -fx-background-color: #333333; -fx-text-fill: #E0E0E0; -fx-prompt-text-fill: #777777;");

            Label descLabel = new Label("Description:");
            descLabel.setStyle("-fx-text-fill: #f7f5f5");
            TextArea descInput2 = new TextArea();
            String currentDescription = descriptionLabel.getText();
            descInput2.setText(currentDescription);
            descInput2.setStyle("-fx-pref-width: 380px; -fx-max-width: 380px; -fx-pref-height: 180px; -fx-max-height: 180px; " +
                    "-fx-background-radius: 10; -fx-border-radius: 0;" + " -fx-padding: 10; -fx-background-color: #333333;" +
                    " -fx-text-fill: #E0E0E0; -fx-prompt-text-fill: #777777; -fx-control-inner-background: #333333;" +
                    "-fx-prompt-text-fill: #777777;" + "-fx-border-color: transparent; -fx-focus-color: transparent; " +
                    "-fx-faint-focus-color: transparent; -fx-effect: none;");
            descInput2.setWrapText(true);
            descInput2.setPrefRowCount(6);

            Button saveButton = new Button("Save Changes");
            saveButton.setStyle("-fx-font-size: 18px; -fx-background-color: #1A1A5A; -fx-text-fill: white; -fx-background-radius: 10;");
            saveButton.setOnAction(e -> {
                String name = nameInput2.getText().trim();
                String description = descInput2.getText().trim();

                if (!name.isEmpty() && !description.isEmpty()) {
                    String templateId = template.getObjectId("_id").toHexString();
                    templateService.updateTemplate(templateId, name, description); // Update instead of adding
                    VBox parentContainer = (VBox) templateBox.getParent();
                    loadTemplatesView(parentContainer);
                    dialog.close();
                } else {
                    LoginController.showAlert("Field Empty", "Both fields must be used.");
                    System.out.println("Both fields are required.");
                }
            });
            HBox saveButtonCentre = new HBox(saveButton);
            saveButtonCentre.setAlignment(Pos.CENTER);
            VBox layout = new VBox(10, nameFieldLabel, nameInput2, descLabel, descInput2, saveButtonCentre);
            layout.setStyle("-fx-padding: 10; -fx-background-color: #121212");
            dialog.setScene(new Scene(layout));
            dialog.show();
        });

        templateBox.getChildren().addAll(textContainer, spacer,editButton, deleteButton);

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
