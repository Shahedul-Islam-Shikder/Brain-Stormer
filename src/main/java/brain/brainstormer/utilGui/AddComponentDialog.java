package brain.brainstormer.utilGui;

import brain.brainstormer.components.core.ComponentFactory;
import brain.brainstormer.components.core.CoreComponent;
import brain.brainstormer.components.interfaces.Initializable;
import brain.brainstormer.service.ComponentService;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
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

    private final String templateId;
    private final ComponentService componentService;
    private final ListView<HBox> componentList = new ListView<>();

    public AddComponentDialog(String templateId, ComponentService componentService) {
        this.templateId = templateId;
        this.componentService = componentService;
    }

    public void init() {
        Alert alert = new Alert(AlertType.CONFIRMATION);
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
                addComponent(componentName);
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

    private void addComponent(String componentName) {
        Document componentData = componentService.getComponentsCollection().find(new Document("type", componentName)).first();
        CoreComponent component = ComponentFactory.createComponent(componentData);
        if (component == null) {
            showError("Component creation failed.");
            return;
        }

        if (component instanceof Initializable) {
            // Pass all required parameters to ComponentDialogBox
            ComponentDialogBox dialogBox = new ComponentDialogBox(component, false, componentService, templateId);
            dialogBox.showDialog();
        } else {
            // Directly add to the database if no initial configuration is needed
            componentService.addComponentToTemplate(templateId, component);
        }
    }


    private void showError(String message) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle("Error");
        alert.setContentText(message);
        alert.showAndWait();
    }
}
