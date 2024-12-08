package brain.brainstormer.components.elements;

import brain.brainstormer.components.core.CoreComponent;
import brain.brainstormer.controller.TemplateController;
import brain.brainstormer.service.ComponentService;
import brain.brainstormer.utils.SceneSwitcher;
import brain.brainstormer.utils.TemplateData;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Separator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.bson.Document;

public class LineBreaker extends CoreComponent {

    public LineBreaker(String id, String description) {
        super(id, "line_breaker", description);
    }

    @Override
    public Node render() {
        // Create the separator line
        Separator line = new Separator();
        line.setPrefWidth(Double.MAX_VALUE); // Full width of the container
        line.setPrefHeight(1.0); // Set the thickness of the separator line

        // Apply CSS style for a subtle gray color
        line.setStyle("-fx-background-color: #4A4A4A;"); // Light gray color

        // Create Delete button
        HBox buttonContainer = new HBox(10);
        buttonContainer.setAlignment(Pos.CENTER_LEFT);
        buttonContainer.getStyleClass().add("button-container");

        // Create the Delete button
        FontAwesomeIconView deleteIcon = new FontAwesomeIconView(FontAwesomeIcon.TRASH);
        deleteIcon.getStyleClass().add("delete-icon");

        Button deleteButton = new Button("", deleteIcon); // Icon-only button
        deleteButton.setOnAction(event -> {
            System.out.println("Deleting Separator component with ID: " + getId());
            delete(); // Call the inherited delete method
        });

        buttonContainer.getChildren().addAll(deleteButton);

        // Wrap the separator and delete button in a VBox
        VBox container = new VBox(10); // Add spacing between line and button
        container.setSpacing(5);
        container.setPadding(new javafx.geometry.Insets(10, 0, 10, 0)); // Top and bottom padding
        container.getChildren().addAll(line, buttonContainer);

        applyGlobalComponentStyles(container);

        return container;
    }


    @Override
    public Document toDocument() {
        return new Document("_id", getId())
                .append("type", "line_breaker")
                .append("description", getDescription());
    }

    @Override
    public void saveToDatabase() {

    }


}
