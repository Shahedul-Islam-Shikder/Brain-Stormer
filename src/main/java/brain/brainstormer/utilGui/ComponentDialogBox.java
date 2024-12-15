package brain.brainstormer.utilGui;

import brain.brainstormer.components.core.CoreComponent;
import brain.brainstormer.components.elements.Grouper;
import brain.brainstormer.components.interfaces.Initializable;
import brain.brainstormer.controller.TemplateController;
import brain.brainstormer.service.ComponentService;
import brain.brainstormer.utils.SceneSwitcher;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import org.bson.Document;

import java.util.Optional;

public class ComponentDialogBox {


    private final CoreComponent component;
    private final boolean isEditing;
    private final ComponentService componentService;
    private final String templateId; // For template-related operations
    private final Grouper grouperComponent; // For Grouper-related operations

    // Constructor for adding/editing components in a Template
    public ComponentDialogBox(CoreComponent component, boolean isEditing, ComponentService componentService, String templateId) {
        this.component = component;
        this.isEditing = isEditing;
        this.componentService = componentService;
        this.templateId = templateId;
        this.grouperComponent = null; // Not used in this context
    }


    // Constructor for adding to a Grouper
    public ComponentDialogBox(String templateId, CoreComponent component, boolean isEditing, ComponentService componentService, Grouper grouperComponent) {
        this.component = component;
        this.isEditing = isEditing;
        this.componentService = componentService;
        this.templateId = templateId; // Not used when adding to Grouper
        this.grouperComponent = grouperComponent;
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
                addComponentToTarget(componentData);
            }
        }
    }

    private void addComponentToTarget(Document componentData) {
        if (grouperComponent == null) {
            // Add to the template
            System.out.println("Adding component to template: " + componentData.toJson());
            componentService.addComponentToTemplate(templateId, component); // Saves the component with initial values
        } else {
            // Add to the Grouper
            System.out.println("Adding component to Grouper: " + componentData.toJson());


            // Save the component to the Grouper's database entry
            componentService.addComponentsToGrouper(templateId, grouperComponent.getId(), java.util.List.of(component));
        }
    }

    private void editComponentInDatabase(Document componentData) {
        try {
            componentService.editComponents(templateId, component.getId(), componentData);

            // Refresh the template
            TemplateController controller = SceneSwitcher.getCurrentController(TemplateController.class);
            if (controller != null) {
                controller.refreshTemplateContent(templateId);

                // Send WebSocket update

            }
        } catch (Exception e) {
            System.err.println("Failed to edit component in Grouper: " + e.getMessage());
        }
    }





}
