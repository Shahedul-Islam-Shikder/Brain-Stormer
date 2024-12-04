package brain.brainstormer.components.elements;

import brain.brainstormer.components.core.CoreComponent;
import brain.brainstormer.components.interfaces.Initializable;
import brain.brainstormer.controller.TemplateController;
import brain.brainstormer.service.ComponentService;
import brain.brainstormer.service.TemplateService;
import brain.brainstormer.utilGui.ComponentDialogBox;
import brain.brainstormer.utils.Debouncer;
import brain.brainstormer.utils.SceneSwitcher;
import brain.brainstormer.utils.TemplateData;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
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
        // Create the heading label
        Label headingLabel = new Label(title);
        setFontSize(headingLabel, headingLevel);

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
            System.out.println("Deleting Heading component with ID: " + getId());
            delete(); // Call the delete method
        });

        buttonContainer.getChildren().addAll(editButton, deleteButton);

        // Wrap everything in a VBox
        VBox container = new VBox(10); // Spacing between Heading and buttons
        container.getChildren().addAll(headingLabel, buttonContainer);
        container.getStyleClass().add("heading-wrapper"); // CSS class for styling

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
