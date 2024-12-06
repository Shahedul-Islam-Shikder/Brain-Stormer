package brain.brainstormer.components.elements;

import brain.brainstormer.components.core.CoreComponent;
import brain.brainstormer.service.TemplateService;
import brain.brainstormer.utils.TemplateData;
import brain.brainstormer.utils.Debouncer;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.bson.Document;

import java.util.Date;

public class TextArea extends CoreComponent {
    private String text;
    private int rows;

    // Create a static Debouncer for all TextArea components (optional: make it instance-specific)
    private static final Debouncer<String> debouncer = new Debouncer<>(1000); // 1-second debounce

    public TextArea(String id, String description, String text, int rows) {
        super(id, "textarea", description);
        this.text = text != null ? text : "";
        this.rows = rows > 0 ? rows : 5;
    }

    @Override

    public Node render() {
        VBox container = new VBox(10); // Add spacing between components
        container.getStyleClass().add("vbox-container"); // Apply container style

        // Create the TextArea
        javafx.scene.control.TextArea textArea = new javafx.scene.control.TextArea(text);
        textArea.setPrefRowCount(rows);
        textArea.setWrapText(true);
        textArea.getStyleClass().add("text-area"); // Apply TextArea style
        textArea.setPadding(new javafx.geometry.Insets(10, 0, 0, 0)); // Add padding (10px top)

        textArea.textProperty().addListener((observable, oldValue, newValue) -> {
            text = newValue;
            saveToDatabase(); // Save changes to the database with debouncing
        });

        // Create the button container
        HBox buttonContainer = new HBox(10); // Add spacing between buttons
        buttonContainer.getStyleClass().add("button-container"); // Apply button container style

        // Create the Delete button
        FontAwesomeIconView deleteIcon = new FontAwesomeIconView(FontAwesomeIcon.TRASH);
        deleteIcon.getStyleClass().add("delete-icon");

        Button deleteButton = new Button("", deleteIcon); // Icon-only button
        deleteButton.setOnAction(event -> {
            System.out.println("Deleting TextArea component with ID: " + getId());
            delete(); // Call the inherited delete method
        });

        // Add Delete button to the button container
        buttonContainer.getChildren().add(deleteButton);

        // Add TextArea and button container to the main container
        container.getChildren().addAll(textArea, buttonContainer);

        // Apply global and component-specific styles
        applyGlobalComponentStyles(container);// Apply global component styles
        applyStyles(container, "/styles/textarea.css"); // Apply specific styles for this component

        return container;
    }



    @Override
    public Document toDocument() {
        return new Document("_id", getId())
                .append("type", "textarea")
                .append("config", new Document("text", text)
                        .append("description", getDescription()))
                .append("createdAt", "2024-11-16T08:00:00Z")
                .append("lastUpdated", "2024-11-16T09:00:00Z");
    }

    @Override
    public void saveToDatabase() {
        try {
            TemplateService templateService = TemplateService.getInstance(); // Use singleton TemplateService

            // Get the current template ID from TemplateData
            String templateId = TemplateData.getInstance().getCurrentTemplateId();
            if (templateId == null || templateId.isEmpty()) {
                System.err.println("No current template ID set in TemplateData.");
                return;
            }

            // Prepare the updated component document
            Document updatedComponent = new Document("config", new Document("text", text)
                    .append("description", getDescription()))
                    .append("lastUpdated", new Date());

            // Debounce the save operation
            String debouncerKey = templateId + ":" + getId(); // Unique key for this TextArea
            debouncer.debounce(debouncerKey, () -> {
                System.out.println("Debounced update triggered for TextArea: " + getId());
                templateService.updateComponentInTemplate(templateId, getId(), updatedComponent);
            });

        } catch (Exception e) {
            System.err.println("Failed to save TextArea state to database: " + e.getMessage());
        }
    }


}
