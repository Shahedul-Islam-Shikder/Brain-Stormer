package brain.brainstormer.components.elements;

import brain.brainstormer.components.core.CoreComponent;
import brain.brainstormer.service.TemplateService;
import brain.brainstormer.utils.Debouncer;
import brain.brainstormer.utils.TemplateData;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.scene.web.HTMLEditor;
import org.bson.Document;

import java.util.Date;

public class RichTextEditor extends CoreComponent {
    private String htmlContent;

    // Static Debouncer for all RichTextEditor components
    private static final Debouncer<String> debouncer = new Debouncer<>(1000); // 1-second debounce delay

    public RichTextEditor(String id, String description, String initialHtmlContent) {
        super(id, "rich_text_editor", description);
        this.htmlContent = initialHtmlContent != null ? initialHtmlContent : "<p></p>"; // Default empty paragraph
    }

    @Override
    public Node render() {
        // Create the container for the RichTextEditor
        VBox container = new VBox(10); // Add spacing between components
        container.getStyleClass().add("vbox-container"); // Style for the container

        // Create the HTMLEditor
        HTMLEditor htmlEditor = new HTMLEditor();
        htmlEditor.setHtmlText(htmlContent); // Set initial content
        htmlEditor.setPrefHeight(400); // Set a default height
        htmlEditor.getStyleClass().add("rich-text-editor"); // Style for the editor

        // Link the external stylesheet
        htmlEditor.getStylesheets().add(getClass().getResource("/styles/rich-text-editor.css").toExternalForm());

        // Update htmlContent when user modifies the content
        htmlEditor.setOnKeyReleased(event -> {
            htmlContent = htmlEditor.getHtmlText();
            saveToDatabase(); // Save changes to the database with debouncing
        });

        // Create the Delete button
        FontAwesomeIconView deleteIcon = new FontAwesomeIconView(FontAwesomeIcon.TRASH);
        deleteIcon.getStyleClass().add("delete-icon");

        Button deleteButton = new Button("", deleteIcon); // Icon-only button
        deleteButton.setOnAction(event -> {
            System.out.println("Deleting RichTextEditor component with ID: " + getId());
            delete(); // Call the inherited delete method
        });

        // Add HTMLEditor and Delete button to the container
        container.getChildren().addAll(htmlEditor, deleteButton);

        return container;
    }

    @Override
    public Document toDocument() {
        return new Document("_id", getId())
                .append("type", "rich_text_editor")
                .append("config", new Document("htmlContent", htmlContent)
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
            Document updatedComponent = new Document("config", new Document("htmlContent", htmlContent)
                    .append("description", getDescription()))
                    .append("lastUpdated", new Date());

            // Debounce the save operation
            String debouncerKey = templateId + ":" + getId(); // Unique key for this RichTextEditor
            debouncer.debounce(debouncerKey, () -> {
                System.out.println("Debounced update triggered for RichTextEditor: " + getId());
                templateService.updateComponentInTemplate(templateId, getId(), updatedComponent);
            });

        } catch (Exception e) {
            System.err.println("Failed to save RichTextEditor state to database: " + e.getMessage());
        }
    }



    // Getter for the current HTML content
    public String getHtmlContent() {
        return htmlContent;
    }

    // Setter to programmatically update the HTML content
    public void setHtmlContent(String htmlContent) {
        this.htmlContent = htmlContent != null ? htmlContent : "<p></p>";
    }
}
