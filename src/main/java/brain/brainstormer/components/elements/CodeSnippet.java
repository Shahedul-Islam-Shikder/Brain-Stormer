package brain.brainstormer.components.elements;

import brain.brainstormer.components.core.CoreComponent;
import brain.brainstormer.service.TemplateService;
import brain.brainstormer.utils.Debouncer;
import brain.brainstormer.utils.TemplateData;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.bson.Document;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;

import java.util.Date;

public class CodeSnippet extends CoreComponent {
    private CodeArea codeArea;
    private String code;

    // Static Debouncer for all CodeSnippet components
    private static final Debouncer<String> debouncer = new Debouncer<>(1000); // 1-second debounce delay

    public CodeSnippet(String id, String description, String code) {
        super(id, "code_snippet", description);
        this.code = code;
        initializeCodeArea();
    }

    private void initializeCodeArea() {
        codeArea = new CodeArea();
        codeArea.setWrapText(true);
        codeArea.setParagraphGraphicFactory(LineNumberFactory.get(codeArea)); // Adds line numbers
        codeArea.replaceText(code);

        // Apply brand and dark theme styling
        codeArea.setStyle("-fx-background-color: #121212; -fx-text-fill: #ffffff; -fx-font-family: 'Courier New'; -fx-font-size: 14px;");
        codeArea.setPadding(new Insets(10));

        // Listen for changes to the code text
        codeArea.textProperty().addListener((observable, oldValue, newValue) -> {
            code = newValue; // Update the local code field
            saveToDatabase(); // Save changes to the database with debouncing
        });
    }

    @Override
    public Node render() {
        // Wrap CodeArea in ScrollPane for scrollability
        ScrollPane scrollPane = new ScrollPane(codeArea);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);

        // Make the CodeArea resizable within VBox
        VBox container = new VBox(scrollPane);
        VBox.setVgrow(scrollPane, Priority.ALWAYS); // Allow the VBox to expand as needed
        container.setStyle("-fx-background-color: #121212; -fx-background-radius: 10; -fx-padding: 10;");
        container.setPrefHeight(150); // Set an initial height, but allows resizing

        return container;
    }

    @Override
    public Document toDocument() {
        code = codeArea.getText();
        return new Document("_id", getId())
                .append("type", "code_snippet")
                .append("config", new Document("code", code)
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
            Document updatedComponent = new Document("config", new Document("code", code)
                    .append("description", getDescription()))
                    .append("lastUpdated", new Date());

            // Debounce the save operation
            String debouncerKey = templateId + ":" + getId(); // Unique key for this CodeSnippet
            debouncer.debounce(debouncerKey, () -> {
                System.out.println("Debounced update triggered for CodeSnippet: " + getId());
                templateService.updateComponentInTemplate(templateId, getId(), updatedComponent);
            });

        } catch (Exception e) {
            System.err.println("Failed to save CodeSnippet state to database: " + e.getMessage());
        }
    }

    public String getCode() {
        return codeArea.getText();
    }

    public void setCode(String code) {
        this.code = code;
        codeArea.replaceText(code);
    }
}
