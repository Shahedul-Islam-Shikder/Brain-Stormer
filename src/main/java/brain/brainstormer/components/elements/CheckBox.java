package brain.brainstormer.components.elements;

import brain.brainstormer.components.core.CoreComponent;
import brain.brainstormer.components.interfaces.Initializable;
import brain.brainstormer.service.ComponentService;
import brain.brainstormer.service.TemplateService;
import brain.brainstormer.utilGui.ComponentDialogBox;
import brain.brainstormer.config.Debouncer;
import brain.brainstormer.config.TemplateData;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
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
        // Container for the checkbox and label
        HBox checkBoxContainer = new HBox(10);
        checkBoxContainer.setAlignment(Pos.CENTER_LEFT);
        checkBoxContainer.getStyleClass().add("checkbox-container"); // CSS class for styling

        // Create the checkbox
        javafx.scene.control.CheckBox checkBox = new javafx.scene.control.CheckBox();
        checkBox.setSelected(isChecked);
        checkBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
            isChecked = newValue;
            saveToDatabase(); // Save changes with debouncing
        });

        // Label for the checkbox
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("checkbox-label");

        checkBoxContainer.getChildren().addAll(checkBox, titleLabel);

        // Create the Edit and Delete buttons
        HBox buttonContainer = new HBox(10);
        buttonContainer.setAlignment(Pos.CENTER_LEFT);
        buttonContainer.getStyleClass().add("button-container"); // CSS class for styling

        // Edit button with FontAwesomeIcon
        FontAwesomeIconView editIcon = new FontAwesomeIconView(FontAwesomeIcon.PENCIL);
        editIcon.getStyleClass().add("edit-icon");

        Button editButton = new Button("", editIcon); // Icon-only button
        editButton.setOnAction(event -> {
            ComponentDialogBox editDialog = new ComponentDialogBox(this, true, ComponentService.getInstance(), TemplateData.getInstance().getCurrentTemplateId());
            editDialog.showDialog();
            System.out.println("Editing component with ID: " + getId());
        });

        // Delete button with FontAwesomeIcon
        FontAwesomeIconView deleteIcon = new FontAwesomeIconView(FontAwesomeIcon.TRASH);
        deleteIcon.getStyleClass().add("delete-icon");

        Button deleteButton = new Button("", deleteIcon); // Icon-only button
        deleteButton.setOnAction(event -> {
            System.out.println("Deleting component with ID: " + getId());
            delete(); // Log the delete action
        });

        buttonContainer.getChildren().addAll(editButton, deleteButton);

        // Wrap everything in a VBox
        VBox container = new VBox(5); // Spacing between checkbox and buttons
        container.getChildren().addAll(checkBoxContainer, buttonContainer);
        container.getStyleClass().add("checkbox-wrapper"); // CSS class for styling

        // Apply global and component-specific styles
        applyGlobalComponentStyles(container);
        applyStyles(container, "/styles/checkbox.css"); // Apply specific styles for this component

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
