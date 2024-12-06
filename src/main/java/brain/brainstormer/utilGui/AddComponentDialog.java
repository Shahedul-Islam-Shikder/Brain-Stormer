package brain.brainstormer.utilGui;

import brain.brainstormer.components.core.ComponentFactory;
import brain.brainstormer.components.core.CoreComponent;
import brain.brainstormer.components.elements.Grouper;
import brain.brainstormer.components.interfaces.Initializable;
import brain.brainstormer.controller.TemplateController;
import brain.brainstormer.service.ComponentService;
import brain.brainstormer.utils.SceneSwitcher;
import brain.brainstormer.utils.StyleUtil;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.bson.Document;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;

import java.util.List;
import java.util.Optional;

public class AddComponentDialog {

    private String templateId; // For template-related components
    private final Grouper grouperComponent; // For Grouper-related components
    private final ComponentService componentService;
    private final ListView<HBox> componentList = new ListView<>();

    // Constructor for adding to a template
    public AddComponentDialog(String templateId, ComponentService componentService ) {
        this.templateId = templateId;
        this.grouperComponent = null; // Not used in this context
        this.componentService = componentService;
    }

    // Constructor for adding to a Grouper
    public AddComponentDialog(String templateId, Grouper grouperComponent, ComponentService componentService ) {
        this.templateId = templateId;
        this.grouperComponent = grouperComponent;
        this.componentService = componentService;

    }

    // Initialize the dialog box with the component list for template or Grouper

    public void init() {
        Stage dialog = new Stage();
        dialog.setMinHeight(460);
        dialog.setMinWidth(420);
        dialog.setResizable(false);
        dialog.setTitle("Add Component");
        dialog.initModality(Modality.APPLICATION_MODAL);

        Text headerText = new Text("Add Component");
        headerText.setFont(new Font("Arial", 20));
        headerText.setStyle("-fx-font-weight: bold; -fx-fill: #FFFFFF;");
        VBox headerBox = new VBox(headerText);
        headerBox.setPadding(new Insets(0, 0, 10, 0));

        componentList.setStyle("-fx-background-color: #1e1e1e;");
        componentList.setPrefHeight(300);


        Button saveButton = new Button("Add");
        saveButton.getStyleClass().add("button-primary");
        saveButton.setMinHeight(55);
        saveButton.setMinWidth(80);
        saveButton.setOnAction(e-> {
            handleButtonClick(dialog);
        });

        Button cancelButton = new Button("Cancel");
        cancelButton.getStyleClass().add("button-danger");
        cancelButton.setMinHeight(55);
        cancelButton.setMinWidth(80);
        cancelButton.setStyle("-fx-font-size: 18px;");
        cancelButton.setOnAction(e ->{
            dialog.close();
        });

        HBox buttonContainer = new HBox(50,saveButton,cancelButton);
        buttonContainer.setAlignment(Pos.CENTER);

        VBox dialogContent = new VBox(headerBox, componentList,buttonContainer);
        dialogContent.setSpacing(10);
        dialogContent.setPadding(new Insets(20));
        dialogContent.getStyleClass().add("container");
        //dialog.getDialogPane().setContent(dialogContent);

        Scene scene = new Scene(dialogContent);
//        StyleUtil.applyStylesheet(scene);
        loadComponents();


        dialog.setScene(scene);
        dialog.show();
    }

    private void handleButtonClick(Stage dialog) {
        // Get the selected item from the component list
        HBox selectedBox = componentList.getSelectionModel().getSelectedItem();
        if (selectedBox != null) {
            // Extract the component name from the selected item
            Text nameText = (Text) selectedBox.getChildren().get(0);
            String componentName = nameText.getText();

            // Add the component to the appropriate target
            if (grouperComponent == null) {
                addComponentToTemplate(componentName);
            } else {
                addComponentToGrouper(componentName);
            }

            // Close the dialog after the component is added
            dialog.close();
        } else {
            // Show a message if no item is selected (optional)
            Alert alert = new Alert(Alert.AlertType.WARNING, "Please select a component to add.", ButtonType.OK);
            alert.showAndWait();
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
                nameText.setFont(new Font("Arial", 12));
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

        // Refresh the template content
        TemplateController controller = SceneSwitcher.getCurrentController(TemplateController.class);
        if (controller != null) {
            controller.refreshTemplateContent(templateId);
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
            ComponentDialogBox dialogBox = new ComponentDialogBox(templateId, component, false, componentService, grouperComponent);
            dialogBox.showDialog();
        } else {
            componentService.addComponentsToGrouper(templateId, grouperComponent.getId(), List.of(component));
        }

        // Refresh the template content
        TemplateController controller = SceneSwitcher.getCurrentController(TemplateController.class);
        if (controller != null) {
            controller.refreshTemplateContent(templateId);
        }
    }



    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setContentText(message);
        alert.showAndWait();
    }
}
