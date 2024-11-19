package brain.brainstormer.utilGui;

import brain.brainstormer.components.core.ComponentFactory;
import brain.brainstormer.components.core.CoreComponent;
import brain.brainstormer.components.elements.GrouperComponent;
import brain.brainstormer.components.interfaces.Initializable;
import brain.brainstormer.service.ComponentService;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import org.bson.Document;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;

import java.util.Optional;

public class AddComponentDialog {

    private final String templateId; // For template-related components
    private final GrouperComponent grouperComponent; // For Grouper-related components
    private final ComponentService componentService;
    private final ListView<HBox> componentList = new ListView<>();

    // Constructor for adding to a template
    public AddComponentDialog(String templateId, ComponentService componentService) {
        this.templateId = templateId;
        this.grouperComponent = null; // Not used in this context
        this.componentService = componentService;
    }

    // Constructor for adding to a Grouper
    public AddComponentDialog(GrouperComponent grouperComponent, ComponentService componentService) {
        this.templateId = null; // Not used in this context
        this.grouperComponent = grouperComponent;
        this.componentService = componentService;
    }

    public void init() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Add Component");

        Text headerText = new Text("Add Component");
        headerText.setFont(new Font("Arial", 20));
        headerText.setStyle("-fx-font-weight: bold; -fx-fill: #FFFFFF;");
        VBox headerBox = new VBox(headerText);
        headerBox.setPadding(new Insets(0, 0, 10, 0));

        componentList.setStyle("-fx-background-color: #1e1e1e;");
        componentList.setPrefHeight(300);

        VBox dialogContent = new VBox(headerBox, componentList);
        dialogContent.setSpacing(10);
        dialogContent.setPadding(new Insets(20));
        alert.getDialogPane().setContent(dialogContent);

        loadComponents();

        handleButtonClick(alert);
    }

    private void handleButtonClick(Alert alert) {
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            HBox selectedBox = componentList.getSelectionModel().getSelectedItem();
            if (selectedBox != null) {
                Text nameText = (Text) selectedBox.getChildren().get(0);
                String componentName = nameText.getText();

                // Add component to the appropriate target
                if (templateId != null) {
                    addComponentToTemplate(componentName);
                } else if (grouperComponent != null) {
                    addComponentToGrouper(componentName);
                }
            }
        }
    }

    private void loadComponents() {
        MongoCollection<Document> collection = componentService.getComponentsCollection();
        try (MongoCursor<Document> cursor = collection.find().iterator()) {
            componentList.getItems().clear();
            while (cursor.hasNext()) {
                Document component = cursor.next();
                String name = component.getString("type");
                String description = component.getString("description");

                Text nameText = new Text(name);
                nameText.setFont(new Font("Arial", 16));
                nameText.setStyle("-fx-font-weight: bold; -fx-fill: #FFFFFF;");
                Text descriptionText = new Text(description);
                descriptionText.setFont(new Font("Arial", 12));
                descriptionText.setStyle("-fx-fill: #BBBBBB;");

                HBox componentBox = new HBox(10, nameText, descriptionText);
                componentBox.setStyle("-fx-background-color: #2d2d2d; -fx-padding: 10;");
                componentList.getItems().add(componentBox);
            }
        } catch (Exception e) {
            showError("Error loading components.");
        }
    }

    private void addComponentToTemplate(String componentName) {
        Document componentData = componentService.getComponentsCollection().find(new Document("type", componentName)).first();
        CoreComponent component = ComponentFactory.createComponent(componentData);
        if (component == null) {
            showError("Component creation failed.");
            return;
        }

        if (component instanceof Initializable) {
            ComponentDialogBox dialogBox = new ComponentDialogBox(component, false, componentService, templateId);
            dialogBox.showDialog();
        } else {
            componentService.addComponentToTemplate(templateId, component);
        }
    }

    private void addComponentToGrouper(String componentName) {
        // Retrieve the component metadata from MongoDB
        Document componentData = componentService.getComponentsCollection().find(new Document("type", componentName)).first();
        if (componentData == null) {
            showError("Component metadata not found for: " + componentName);
            return;
        }

        // Create the component using the factory
        CoreComponent component = ComponentFactory.createComponent(componentData);
        if (component == null) {
            showError("Component creation failed for: " + componentName);
            return;
        }

        // If the component is Initializable, handle it exclusively through the dialog
        if (component instanceof Initializable) {
            ComponentDialogBox dialogBox = new ComponentDialogBox(component, false, componentService, grouperComponent);
            dialogBox.showDialog();
            return; // Exit to avoid duplicate addition
        }

        // Render the component and add it to the Grouper
        javafx.scene.Node renderedComponent = component.render();
        grouperComponent.addComponent(renderedComponent);

        // Save the component to the Grouper's database entry
        componentService.addComponentsToGrouper(grouperComponent.getId(), java.util.List.of(component));
    }



    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setContentText(message);
        alert.showAndWait();
    }
}
