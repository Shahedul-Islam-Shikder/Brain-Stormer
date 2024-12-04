package brain.brainstormer.components.elements;

import brain.brainstormer.components.core.CoreComponent;
import brain.brainstormer.service.TemplateService;
import brain.brainstormer.utils.Debouncer;
import brain.brainstormer.utils.TemplateData;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.bson.Document;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CodeSnippet extends CoreComponent {
    private CodeArea codeArea;
    private String code;

    // Static Debouncer for all CodeSnippet components
    private static final Debouncer<String> debouncer = new Debouncer<>(1000); // 1-second debounce delay

    // Regex Patterns for Syntax Highlighting
    private static final String[] KEYWORDS = new String[]{
            "abstract", "assert", "boolean", "break", "byte",
            "case", "catch", "char", "class", "const",
            "continue", "default", "do", "double", "else",
            "enum", "extends", "final", "finally", "float",
            "for", "goto", "if", "implements", "import",
            "instanceof", "int", "interface", "long", "native",
            "new", "package", "private", "protected", "public",
            "return", "short", "static", "strictfp", "super",
            "switch", "synchronized", "this", "throw", "throws",
            "transient", "try", "void", "volatile", "while"
    };

    private static final String KEYWORD_PATTERN = "\\b(" + String.join("|", KEYWORDS) + ")\\b";
    private static final String PAREN_PATTERN = "\\(|\\)";
    private static final String BRACE_PATTERN = "\\{|\\}";
    private static final String BRACKET_PATTERN = "\\[|\\]";
    private static final String SEMICOLON_PATTERN = "\\;";
    private static final String STRING_PATTERN = "\"([^\"\\\\]|\\\\.)*\"";
    private static final String COMMENT_PATTERN = "//[^\n]*" + "|" + "/\\*(.|\\R)*?\\*/";

    private static final Pattern PATTERN = Pattern.compile(
            "(?<KEYWORD>" + KEYWORD_PATTERN + ")"
                    + "|(?<PAREN>" + PAREN_PATTERN + ")"
                    + "|(?<BRACE>" + BRACE_PATTERN + ")"
                    + "|(?<BRACKET>" + BRACKET_PATTERN + ")"
                    + "|(?<SEMICOLON>" + SEMICOLON_PATTERN + ")"
                    + "|(?<STRING>" + STRING_PATTERN + ")"
                    + "|(?<COMMENT>" + COMMENT_PATTERN + ")"
    );

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
        codeArea.getStyleClass().add("code-area");

        // Load the CSS file for styling
        codeArea.getStylesheets().add(CodeSnippet.class.getResource("/styles/java-keywords.css").toExternalForm());

        // Apply initial syntax highlighting for the current code
        if (code != null && !code.isEmpty()) {
            codeArea.setStyleSpans(0, computeHighlighting(code));
        }

        // Listen for changes to the text and update syntax highlighting
        codeArea.textProperty().addListener((observable, oldValue, newValue) -> {
            codeArea.setStyleSpans(0, computeHighlighting(newValue));
            code = newValue; // Update the local code field
            saveToDatabase(); // Save changes to the database with debouncing
        });
    }

    private static StyleSpans<Collection<String>> computeHighlighting(String text) {
        Matcher matcher = PATTERN.matcher(text);
        StyleSpansBuilder<Collection<String>> spansBuilder = new StyleSpansBuilder<>();
        int lastMatchEnd = 0;

        while (matcher.find()) {
            String styleClass =
                    matcher.group("KEYWORD") != null ? "keyword" :
                            matcher.group("PAREN") != null ? "paren" :
                                    matcher.group("BRACE") != null ? "brace" :
                                            matcher.group("BRACKET") != null ? "bracket" :
                                                    matcher.group("SEMICOLON") != null ? "semicolon" :
                                                            matcher.group("STRING") != null ? "string" :
                                                                    matcher.group("COMMENT") != null ? "comment" :
                                                                            null;

            assert styleClass != null;
            spansBuilder.add(Collections.emptyList(), matcher.start() - lastMatchEnd);
            spansBuilder.add(Collections.singleton(styleClass), matcher.end() - matcher.start());
            lastMatchEnd = matcher.end();
        }

        spansBuilder.add(Collections.emptyList(), text.length() - lastMatchEnd);
        return spansBuilder.create();
    }

    @Override
    public Node render() {
        // Create the ScrollPane for the CodeArea
        ScrollPane scrollPane = new ScrollPane(codeArea);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent; -fx-border-color: transparent;");
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);

        // Create the Delete button
        FontAwesomeIconView deleteIcon = new FontAwesomeIconView(FontAwesomeIcon.TRASH);
        deleteIcon.getStyleClass().add("delete-icon");

        Button deleteButton = new Button("", deleteIcon); // Icon-only button
        deleteButton.setOnAction(event -> {
            System.out.println("Deleting CodeSnippet component with ID: " + getId());
            delete(); // Call the inherited delete method
        });

        // Arrange components in a VBox
        VBox container = new VBox(10); // Spacing between components
        VBox.setVgrow(scrollPane, Priority.ALWAYS);
        container.setStyle("-fx-background-color: #000000; -fx-background-radius: 10; -fx-padding: 10;");
        container.setPrefHeight(350);

        container.getChildren().addAll(scrollPane, deleteButton);

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
            TemplateService templateService = TemplateService.getInstance();
            String templateId = TemplateData.getInstance().getCurrentTemplateId();
            if (templateId == null || templateId.isEmpty()) {
                System.err.println("No current template ID set in TemplateData.");
                return;
            }

            Document updatedComponent = new Document("config", new Document("code", code)
                    .append("description", getDescription()))
                    .append("lastUpdated", new Date());

            String debouncerKey = templateId + ":" + getId();
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
