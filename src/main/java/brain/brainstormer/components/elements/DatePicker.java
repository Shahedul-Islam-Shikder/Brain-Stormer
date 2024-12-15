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
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.bson.Document;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

public class DatePicker extends CoreComponent implements Initializable {
    private LocalDate selectedDate;
    private static final Debouncer<String> debouncer = new Debouncer<>(1000); // 1-second debounce delay

    public DatePicker(String id, String type, String description, LocalDate initialDate) {
        super(id, type, description);
        this.selectedDate = initialDate != null ? initialDate : LocalDate.now();
    }

    @Override
    public Node render() {
        // Create the DatePicker
        javafx.scene.control.DatePicker datePicker = new javafx.scene.control.DatePicker(selectedDate);
        datePicker.getStyleClass().add("date-picker"); // Apply custom style from CSS

        datePicker.valueProperty().addListener((observable, oldValue, newValue) -> {
            selectedDate = newValue;
            saveToDatabase(); // Save changes with debouncing
        });

        // Create the Edit and Delete buttons
        HBox buttonContainer = new HBox(10);
        buttonContainer.setAlignment(Pos.CENTER_LEFT);
        buttonContainer.getStyleClass().add("button-container"); // CSS class for styling

        FontAwesomeIconView editIcon = new FontAwesomeIconView(FontAwesomeIcon.PENCIL);
        editIcon.getStyleClass().add("edit-icon");

        Button editButton = new Button("", editIcon); // Icon-only button
        editButton.setOnAction(event -> {
            ComponentDialogBox editDialog = new ComponentDialogBox(this, true, ComponentService.getInstance(), TemplateData.getInstance().getCurrentTemplateId());
            editDialog.showDialog();
            System.out.println("Editing component with ID: " + getId());
        });

        FontAwesomeIconView deleteIcon = new FontAwesomeIconView(FontAwesomeIcon.TRASH);
        deleteIcon.getStyleClass().add("delete-icon");

        Button deleteButton = new Button("", deleteIcon); // Icon-only button
        deleteButton.setOnAction(event -> {
            System.out.println("Deleting DatePicker component with ID: " + getId());
            delete(); // Call the delete method
        });

        buttonContainer.getChildren().addAll(editButton, deleteButton);

        // Wrap everything in a VBox
        VBox container = new VBox(10); // Spacing between DatePicker and buttons
        container.getChildren().addAll(datePicker, buttonContainer);
        container.getStyleClass().add("date-picker-wrapper"); // CSS class for styling

        applyGlobalComponentStyles(container);
        applyStyles(container, "/styles/datepicker.css"); // Apply specific styles for this component


        return container;
    }



    @Override
    public List<Node> getInputFields() {
        javafx.scene.control.DatePicker datePickerField = new javafx.scene.control.DatePicker(selectedDate);
        datePickerField.setPromptText("Select a date");
        datePickerField.getStyleClass().add("date-picker");

        // Return the DatePicker as a list of nodes to display in the dialog
        return List.of(datePickerField);
    }

    public Document toDocument() {
        return new Document("_id", getId())
                .append("type", "datepicker")
                .append("config", new Document("selectedDate", selectedDate != null ? selectedDate.toString() : "")
                        .append("description", getDescription()))
                .append("createdAt", "2024-11-16T08:00:00Z")  // Placeholder for timestamp
                .append("lastUpdated", "2024-11-16T09:00:00Z");  // Placeholder for timestamp
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
            Document updatedComponent = new Document("config", new Document("selectedDate", selectedDate != null ? selectedDate.toString() : "")
                    .append("description", getDescription()))
                    .append("lastUpdated", new Date());

            // Debounce the save operation
            String debouncerKey = templateId + ":" + getId(); // Unique key for this DatePicker
            debouncer.debounce(debouncerKey, () -> {
                System.out.println("Debounced update triggered for DatePicker: " + getId());
                templateService.updateComponentInTemplate(templateId, getId(), updatedComponent);
            });

        } catch (Exception e) {
            System.err.println("Failed to save DatePicker state to database: " + e.getMessage());
        }
    }


}
