package brain.brainstormer.components.elements;

import brain.brainstormer.components.core.CoreComponent;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import org.bson.Document;

public class TextAreaComponent extends CoreComponent {
    private String text;
    private int rows;

    public TextAreaComponent(String id, String description, String text, int rows) {
        super(id, "textarea", description);
        this.text = text != null ? text : "";
        this.rows = rows > 0 ? rows : 5;
    }

    @Override
    public Node render() {
        VBox container = new VBox(5);
        container.getStyleClass().add("vbox-container");  // Apply container style

        TextArea textArea = new TextArea(text);
        textArea.setPrefRowCount(rows);
        textArea.setWrapText(true);
        textArea.getStyleClass().add("text-area");  // Apply TextArea style

        textArea.textProperty().addListener((observable, oldValue, newValue) -> {
            text = newValue;
            saveToDatabase();
        });

        container.getChildren().add(textArea);
        return container;
    }

    public Document toDocument() {
        return new Document("_id", getId())
                .append("type", "textarea")
                .append("config", new Document("text", text)
                        .append("description", getDescription()))
                .append("createdAt", "2024-11-16T08:00:00Z")
                .append("lastUpdated", "2024-11-16T09:00:00Z");
    }

    private void saveToDatabase() {
        System.out.println("Saving text to database: " + text);
    }
}
