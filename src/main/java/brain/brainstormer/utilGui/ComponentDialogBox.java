package brain.brainstormer.utilGui;

import brain.brainstormer.components.core.CoreComponent;
import brain.brainstormer.components.interfaces.Initializable;
import brain.brainstormer.service.ComponentService;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import org.bson.Document;

import java.util.Optional;

public class ComponentDialogBox {

    private final CoreComponent component;
    private final boolean isEditing;
    private final ComponentService componentService;
    private final String templateId;

    public ComponentDialogBox(CoreComponent component, boolean isEditing, ComponentService componentService, String templateId) {
        this.component = component;
        this.isEditing = isEditing;
        this.componentService = componentService;
        this.templateId = templateId;
    }

    public void showDialog() {
        Alert dialog = new Alert(Alert.AlertType.CONFIRMATION);
        dialog.setTitle(isEditing ? "Edit Component" : "Initialize Component");

        VBox dialogContent = new VBox(10);
        dialogContent.setPadding(new Insets(20));

        // Header
        Label headerLabel = new Label(isEditing ? "Edit Component" : "Set Up Component");
        headerLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16;");
        dialogContent.getChildren().add(headerLabel);

        // Add input fields for the initial configuration if the component is Initializable
        if (component instanceof Initializable) {
            Initializable initializableComponent = (Initializable) component;
            dialogContent.getChildren().addAll(initializableComponent.getInputFields());
        }

        dialog.getDialogPane().setContent(dialogContent);

        // Show the dialog and save component if confirmed
        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            saveComponentData();
        }
    }

    private void saveComponentData() {
        if (component instanceof Initializable) {
            Initializable initializableComponent = (Initializable) component;
            Document componentData = initializableComponent.toDocument();

            if (isEditing) {
                editComponentInDatabase(componentData);
            } else {
                addComponentToDatabase(componentData);
            }
        }
    }

    private void addComponentToDatabase(Document componentData) {
        System.out.println("Adding component to database: " + componentData.toJson());
        componentService.addComponentToTemplate(templateId, component);  // Saves the component with initial values
    }

    private void editComponentInDatabase(Document componentData) {
        System.out.println("Editing component in database: " + componentData.toJson());
        // Implement database update logic here for editing
    }
}
