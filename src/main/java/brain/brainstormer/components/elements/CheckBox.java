package brain.brainstormer.components.elements;

import brain.brainstormer.components.core.CoreComponent;
import brain.brainstormer.components.interfaces.Initializable;
import brain.brainstormer.service.TemplateService;
import brain.brainstormer.utils.Debouncer;
import brain.brainstormer.utils.TemplateData;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import org.bson.Document;

import java.util.Date;
import java.util.List;

public class CheckBox extends CoreComponent implements Initializable {
    private boolean isChecked;
    private String title;

    // Static Debouncer for all CheckBox components
    private static final Debouncer<String> debouncer = new Debouncer<>(1000); // 1-second debounce delay

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
            saveToDatabase(); // Save changes to the database with debouncing
        });

        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("label");  // Apply label style from CSS

        container.getChildren().addAll(checkBox, titleLabel);  // Add checkbox and label side by side
        return container;
    }

    @Override
    public List<Node> getInputFields() {
        TextField titleField = new TextField();
        titleField.setPromptText("Enter title");
        titleField.setText(title);

        javafx.scene.control.CheckBox defaultCheckedBox = new javafx.scene.control.CheckBox("Checked by default");
        defaultCheckedBox.setSelected(isChecked);

        titleField.textProperty().addListener((observable, oldValue, newValue) -> title = newValue);
        defaultCheckedBox.selectedProperty().addListener((observable, oldValue, newValue) -> isChecked = newValue);

        return List.of(titleField, defaultCheckedBox);
    }

    @Override
    public Document toDocument() {
        return new Document("_id", getId())
                .append("type", "checkbox")
                .append("config", new Document("checked", isChecked)
                        .append("title", title)
                        .append("description", getDescription()))
                .append("createdAt", "2024-11-16T08:00:00Z")
                .append("lastUpdated", "2024-11-16T09:00:00Z");
    }

    @Override
    public void saveToDatabase() {
        try {
            TemplateService templateService = TemplateService.getInstance(); // Use singleton TemplateService

            String templateId = TemplateData.getInstance().getCurrentTemplateId();
            if (templateId == null || templateId.isEmpty()) {
                System.err.println("No current template ID set in TemplateData.");
                return;
            }

            // Prepare the updated component document
            Document updatedComponent = new Document("config", new Document("checked", isChecked)
                    .append("title", title)
                    .append("description", getDescription()))
                    .append("lastUpdated", new Date());

            // Debounce the save operation
            String debouncerKey = templateId + ":" + getId(); // Unique key for this CheckBox
            debouncer.debounce(debouncerKey, () -> {
                System.out.println("Debounced update triggered for CheckBox: " + getId());
                templateService.updateComponentInTemplate(templateId, getId(), updatedComponent);
            });

        } catch (Exception e) {
            System.err.println("Failed to save component state to database: " + e.getMessage());
        }
    }
}
