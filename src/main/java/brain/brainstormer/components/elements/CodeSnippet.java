package brain.brainstormer.components.elements;

import brain.brainstormer.components.core.CoreComponent;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.bson.Document;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;

public class CodeSnippet extends CoreComponent {
    private CodeArea codeArea;
    private String code;

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

    }

    public String getCode() {
        return codeArea.getText();
    }

    public void setCode(String code) {
        this.code = code;
        codeArea.replaceText(code);
    }
}
