package brain.brainstormer.components.elements;

import brain.brainstormer.components.core.CoreComponent;
import brain.brainstormer.service.TemplateService;
import brain.brainstormer.utils.TemplateData;
import brain.brainstormer.utils.Debouncer;
import javafx.scene.Node;
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
        VBox container = new VBox(5);
        container.getStyleClass().add("vbox-container");  // Apply container style

        javafx.scene.control.TextArea textArea = new javafx.scene.control.TextArea(text);
        textArea.setPrefRowCount(rows);
        textArea.setWrapText(true);
        textArea.getStyleClass().add("text-area");  // Apply TextArea style
        // Add padding top 10 px
        textArea.setPadding(new javafx.geometry.Insets(10, 0, 0, 0));

        textArea.textProperty().addListener((observable, oldValue, newValue) -> {
            text = newValue;
            saveToDatabase(); // Save changes to the database with debouncing
        });

        container.getChildren().add(textArea);
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

    @Override
    public void delete() {

    }
}
