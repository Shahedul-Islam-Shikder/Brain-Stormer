package brain.brainstormer.utilGui;

import brain.brainstormer.components.elements.Components;
import brain.brainstormer.components.elements.InitialPopup;
import brain.brainstormer.components.elements.TextAreaComponent;
import brain.brainstormer.service.ComponentService;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import org.bson.Document;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Region;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import java.util.Optional;

public class AddComponentDialog {

    private final String templateId;
    private final ComponentService componentService;

    // Constructor accepting templateId and componentService
    public AddComponentDialog(String templateId, ComponentService componentService) {
        this.templateId = templateId;
        this.componentService = componentService;
    }

    public void showComponentDialog() {
        // Fetch components from MongoDB
        MongoCollection<Document> collection = componentService.getComponentsCollection();
        MongoCursor<Document> cursor = collection.find().iterator();

        // Set up JavaFX dialog to display components
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("Add Component");

        // Customize the header
        Text headerText = new Text("Add Component");
        headerText.setFont(new Font("Arial", 20));
        headerText.setStyle("-fx-font-weight: bold; -fx-fill: #FFFFFF;");
        VBox headerBox = new VBox(headerText);
        headerBox.setPadding(new Insets(0, 0, 10, 0));

        // Create ListView with dark style
        ListView<HBox> componentList = new ListView<>();
        componentList.setStyle("-fx-background-color: #1e1e1e; -fx-background-radius: 8;");
        componentList.setPrefHeight(300);  // Allow for scrolling
        componentList.setMaxHeight(Region.USE_PREF_SIZE);  // Restrict height for scrolling

        try {
            while (cursor.hasNext()) {
                Document component = cursor.next();
                String name = component.getString("name");
                String description = component.getString("description");

                // Create an HBox for each component item with name and description
                Text nameText = new Text(name);
                nameText.setFont(new Font("Arial", 16));
                nameText.setStyle("-fx-font-weight: bold; -fx-fill: #FFFFFF;");

                Text descriptionText = new Text(description);
                descriptionText.setFont(new Font("Arial", 12));
                descriptionText.setStyle("-fx-fill: #BBBBBB;");

                HBox componentBox = new HBox(10, nameText, descriptionText);
                componentBox.setStyle("-fx-background-color: #2d2d2d; -fx-background-radius: 8; -fx-padding: 10;");
                componentBox.setPadding(new Insets(10));
                componentList.getItems().add(componentBox);
            }
        } finally {
            cursor.close();
        }

        // Organize dialog content with VBox and add to alert dialog
        VBox dialogContent = new VBox(headerBox, componentList);
        dialogContent.setSpacing(10);
        dialogContent.setPadding(new Insets(20));
        dialogContent.setStyle("-fx-background-color: #1e1e1e; -fx-background-radius: 10;");

        alert.getDialogPane().setContent(dialogContent);
        alert.getDialogPane().setStyle("-fx-background-color: #1e1e1e; -fx-border-radius: 10;");

        // Show the dialog and wait for the user's confirmation (OK button click)
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            HBox selectedComponentBox = componentList.getSelectionModel().getSelectedItem();
            if (selectedComponentBox != null) {
                Text nameText = (Text) selectedComponentBox.getChildren().get(0);
                String componentName = nameText.getText();

                // Instantiate the selected component based on the name
                Components component = createComponentInstance(componentName);
                if (component != null) {
                    // Check if the component implements InitialPopup for configuration
                    if (component instanceof InitialPopup) {
                        ((InitialPopup) component).showInitialPopup();
                    }

                    // Add the configured component to the template in MongoDB
                    componentService.addComponentToTemplate(templateId, componentName);
                } else {
                    System.out.println("Component could not be created.");
                }
            } else {
                System.out.println("No component selected.");
            }
        }
    }

    // Method to create an instance of the component based on its name
    private Components createComponentInstance(String componentName) {
        switch (componentName) {
            case "TextArea":
                return new TextAreaComponent("1", "Text Area", "A text area component", "Default text", 5);
            // Add cases for additional components here
            default:
                System.out.println("Component not recognized: " + componentName);
                return null;
        }
    }
}
