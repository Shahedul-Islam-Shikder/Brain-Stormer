package brain.brainstormer.components.elements;

import brain.brainstormer.components.core.CoreComponent;
import javafx.scene.Node;
import javafx.scene.control.Separator;
import javafx.scene.layout.VBox;
import org.bson.Document;

public class LineBreakerComponent extends CoreComponent {

    public LineBreakerComponent(String id, String description) {
        super(id, "line_breaker", description);
    }

    @Override
    public Node render() {
        Separator line = new Separator();
        line.setPrefWidth(Double.MAX_VALUE); // Full width of the container
        line.setPrefHeight(1.0); // Set the thickness of the separator line

        // Apply CSS style for a subtle gray color
        line.setStyle("-fx-background-color: #4A4A4A;"); // Light gray color

        // Wrap the separator in a VBox with padding
        VBox container = new VBox(line);
        container.setSpacing(5);
        container.setPadding(new javafx.geometry.Insets(10, 0, 10, 0)); // Top and bottom padding

        return container;
    }

    @Override
    public Document toDocument() {
        return new Document("_id", getId())
                .append("type", "line_breaker")
                .append("description", getDescription());
    }
}
