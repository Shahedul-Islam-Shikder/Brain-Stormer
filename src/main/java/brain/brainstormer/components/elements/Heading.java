package brain.brainstormer.components.elements;

import brain.brainstormer.components.core.CoreComponent;
import brain.brainstormer.components.interfaces.Initializable;
import brain.brainstormer.service.TemplateService;
import brain.brainstormer.utils.Debouncer;
import brain.brainstormer.utils.TemplateData;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import org.bson.Document;

import java.util.List;

public class Heading extends CoreComponent implements Initializable {
    private String title;
    private int headingLevel;

    // Static Debouncer for all Heading components
    private static final Debouncer<String> debouncer = new Debouncer<>(1000); // 1-second debounce delay

    public Heading(String id, String description, String title, int headingLevel) {
        super(id, "heading", description);
        this.title = title;
        this.headingLevel = headingLevel;
    }

    @Override
    public Node render() {
        Label headingLabel = new Label(title);

        setFontSize(headingLabel, headingLevel);
        VBox container = new VBox(headingLabel);
        return container;
    }

    private void setFontSize(Label label, int level) {
        switch (level) {
            case 1:
                label.setStyle("-fx-font-size: 32px; -fx-font-family: 'Arial';");
                break;
            case 2:
                label.setStyle("-fx-font-size: 28px; -fx-font-family: 'Arial';"); // h2 size
                break;
            case 3:
                label.setStyle("-fx-font-size: 24px; -fx-font-family: 'Arial';"); // h3 size
                break;
            default:
                label.setStyle("-fx-font-size: 20px; -fx-font-family: 'Arial';"); // Default size
                break;
        }
    }

    @Override
    public List<Node> getInputFields() {
        TextField titleField = new TextField(title);
        titleField.setPromptText("Enter heading title");

        TextField levelField = new TextField(String.valueOf(headingLevel));
        levelField.setPromptText("Enter heading level (1, 2, ...)");

        titleField.textProperty().addListener((obs, oldText, newText) -> {
            title = newText;
            saveToDatabase();
        });

        levelField.textProperty().addListener((obs, oldVal, newVal) -> {
            try {
                headingLevel = Integer.parseInt(newVal);
            } catch (NumberFormatException ignored) {
                headingLevel = 1; // default to h1 if invalid
            }
            saveToDatabase();
        });

        return List.of(titleField, levelField);
    }

    @Override
    public Document toDocument() {
        return new Document("_id", getId())
                .append("type", "heading")
                .append("config", new Document("title", title)
                        .append("level", headingLevel)
                        .append("description", getDescription()));
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
            Document updatedComponent = new Document("config", new Document("title", title)
                    .append("level", headingLevel)
                    .append("description", getDescription()));

            // Debounce the save operation
            String debouncerKey = templateId + ":" + getId(); // Unique key for this Heading
            debouncer.debounce(debouncerKey, () -> {
                System.out.println("Debounced update triggered for Heading: " + getId());
                templateService.updateComponentInTemplate(templateId, getId(), updatedComponent);
            });

        } catch (Exception e) {
            System.err.println("Failed to save Heading state to database: " + e.getMessage());
        }
    }
}
