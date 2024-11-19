package brain.brainstormer.components.elements;

import brain.brainstormer.components.core.CoreComponent;
import javafx.scene.Node;
import javafx.scene.layout.VBox;
import javafx.scene.web.HTMLEditor;
import org.bson.Document;

public class RichTextEditorComponent extends CoreComponent {
    private String htmlContent;

    public RichTextEditorComponent(String id, String description, String initialHtmlContent) {
        super(id, "rich_text_editor", description);
        this.htmlContent = initialHtmlContent != null ? initialHtmlContent : "<p></p>"; // Default empty paragraph
    }

    @Override
    public Node render() {
        VBox container = new VBox(5);
        container.getStyleClass().add("vbox-container"); // Style for the container

        HTMLEditor htmlEditor = new HTMLEditor();
        htmlEditor.setHtmlText(htmlContent); // Set initial content
        htmlEditor.setPrefHeight(400); // Set a default height
        htmlEditor.getStyleClass().add("rich-text-editor"); // Style for the editor

        // Link the external stylesheet
        htmlEditor.getStylesheets().add(getClass().getResource("/styles/rich-text-editor.css").toExternalForm());

        // Update htmlContent when user modifies the content
        htmlEditor.setOnKeyReleased(event -> {
            htmlContent = htmlEditor.getHtmlText();
        });

        container.getChildren().add(htmlEditor);
        return container;
    }

    public Document toDocument() {
        return new Document("_id", getId())
                .append("type", "rich_text_editor")
                .append("config", new Document("htmlContent", htmlContent)
                        .append("description", getDescription()))
                .append("createdAt", "2024-11-16T08:00:00Z")
                .append("lastUpdated", "2024-11-16T09:00:00Z");
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
