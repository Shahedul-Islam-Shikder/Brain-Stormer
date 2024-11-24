package brain.brainstormer.components.elements;

import brain.brainstormer.components.core.CoreComponent;
import brain.brainstormer.components.interfaces.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import org.bson.Document;

import java.util.List;

public class CheckBox extends CoreComponent implements Initializable {
    private boolean isChecked;
    private String title;

    public CheckBox(String id, String description, boolean initialChecked, String title) {
        super(id, "checkbox", description);
        this.isChecked = initialChecked;
        this.title = title;
    }


    @Override

    public Node render() {
        HBox container = new HBox(10);  // Set spacing between checkbox and label
        container.setAlignment(Pos.CENTER_LEFT);  // Align items to the left
        container.getStyleClass().add("hbox-container");  // Apply container style from CSS

        javafx.scene.control.CheckBox checkBox = new javafx.scene.control.CheckBox();
        checkBox.setSelected(isChecked);
        checkBox.getStyleClass().add("check-box");  // Apply checkbox style from CSS

        checkBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
            isChecked = newValue;
            saveToDatabase();
        });

        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("label");  // Apply label style from CSS

        container.getChildren().addAll(checkBox, titleLabel);  // Add checkbox and label side by side
        return container;
    }




    @Override
    public List<Node> getInputFields() {
        // Create a TextField for the title
        TextField titleField = new TextField();
        titleField.setPromptText("Enter title");
        titleField.setText(title);  // Initialize with the current title if available

        // Create a CheckBox to set the initial checked state
        javafx.scene.control.CheckBox defaultCheckedBox = new javafx.scene.control.CheckBox("Checked by default");
        defaultCheckedBox.setSelected(isChecked);  // Initialize with current isChecked state

        // Listeners to update values when the fields change
        titleField.textProperty().addListener((observable, oldValue, newValue) -> title = newValue);
        defaultCheckedBox.selectedProperty().addListener((observable, oldValue, newValue) -> isChecked = newValue);

        // Return the fields as a list of nodes
        return List.of(titleField, defaultCheckedBox);
    }


    public Document toDocument() {
        return new Document("_id", getId())
                .append("type", "checkbox")
                .append("config", new Document("checked", isChecked)
                        .append("title", title)
                        .append("description", getDescription()))
                .append("createdAt", "2024-11-16T08:00:00Z")
                .append("lastUpdated", "2024-11-16T09:00:00Z");
    }

    public void saveToDatabase() {
        // Implement save logic here
    }


}
