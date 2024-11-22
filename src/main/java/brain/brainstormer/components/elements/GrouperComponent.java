package brain.brainstormer.components.elements;

import brain.brainstormer.components.core.CoreComponent;
import brain.brainstormer.service.ComponentService;
import brain.brainstormer.utilGui.AddComponentDialog;
import brain.brainstormer.utils.TemplateData;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.DatePicker;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;

public class GrouperComponent extends CoreComponent {
    private final String layout; // "HBox" or "VBox"
    private final String alignment;
    private final int spacing;
    private final List<Node> children;
    private Node container; // Keep a single instance of the container

    public GrouperComponent(String id, String type, String alignment, int spacing) {
        super(id, type, "A grouper component for dynamic layouts");
        this.layout = type.equals("h-group") ? "HBox" : "VBox";
        this.alignment = alignment != null ? alignment : "CENTER_LEFT"; // Default alignment
        this.spacing = spacing >= 0 ? spacing : 20; // Default spacing
        this.children = new ArrayList<>(); // Empty children by default
        this.container = createContainer(); // Initialize the container
    }

    private Node createContainer() {
        if (layout.equals("HBox")) {
            HBox hbox = new HBox(spacing);
            hbox.setAlignment(javafx.geometry.Pos.valueOf(alignment));
            hbox.setStyle("-fx-background-color: #121212; -fx-background-radius: 20px;");
            hbox.setPadding(new Insets(20, 20, 20, 20));
            hbox.getChildren().addAll(children); // Add initial children

            // Add the "+" button
            StackPane plusButtonContainer = createPlusButton(hbox);
            hbox.getChildren().add(plusButtonContainer);

            return hbox;
        } else {
            VBox vbox = new VBox(spacing);
            vbox.setAlignment(javafx.geometry.Pos.valueOf(alignment));
            vbox.setStyle("-fx-background-color: #121212; -fx-background-radius: 20px;");
            vbox.setPadding(new Insets(20, 20, 20, 20));
            vbox.getChildren().addAll(children); // Add initial children

            // Add the "+" button
            StackPane plusButtonContainer = createPlusButton(vbox);
            vbox.getChildren().add(plusButtonContainer);

            return vbox;
        }
    }

    private StackPane createPlusButton(Node parentContainer) {
        StackPane stackPane = new StackPane();
        stackPane.setAlignment(javafx.geometry.Pos.BOTTOM_LEFT);

        Button plusButton = new Button("+");
        plusButton.getStyleClass().add("grouper-plus-button"); // External CSS class
        plusButton.setPrefHeight(32);
        plusButton.setPrefWidth(32);
        plusButton.setStyle("-fx-background-color: #121212; -fx-background-radius: 10000px; -fx-text-fill: WHITE; -fx-font-size: 18px; -fx-font-weight: bold;");

        // Add event handler to open AddComponentDialog
        plusButton.setOnAction(event -> openAddComponentDialog(parentContainer));

        stackPane.getChildren().add(plusButton);
        StackPane.setMargin(plusButton, new Insets(5, 0, 0, 5));
        return stackPane;
    }

    private void openAddComponentDialog(Node parentContainer) {
        // Retrieve the current template ID from TemplateData
        String templateId = TemplateData.getInstance().getCurrentTemplateId();

        // Ensure templateId is not null or empty before proceeding
        if (templateId == null || templateId.isEmpty()) {
            System.err.println("No template ID is set. Please set a valid template ID before adding components.");
            return;
        }

        // Initialize services and dialog
        ComponentService componentService = ComponentService.getInstance();
        AddComponentDialog dialog = new AddComponentDialog(templateId, this, componentService);

        // Open the dialog
        dialog.init();
    }


    @Override
    public Node render() {
        return container; // Always return the same container instance
    }




    @Override
    public Document toDocument() {
        return new Document("_id", getId())
                .append("type", layout.equals("HBox") ? "h-group" : "v-group")
                .append("config", new Document("alignment", alignment)
                        .append("spacing", spacing))
                .append("children", new ArrayList<>()) // Children will be dynamically updated
                .append("createdAt", new Document("$currentDate", true))
                .append("lastUpdated", new Document("$currentDate", true));
    }

    public List<Node> getChildren() {
        return children;
    }
    public void addChild(Node child) {
        children.add(child); // Add to internal list
        if (container instanceof HBox) {
            ((HBox) container).getChildren().add(children.size() - 1, child); // Add before the "+" button
        } else if (container instanceof VBox) {
            ((VBox) container).getChildren().add(children.size() - 1, child); // Add before the "+" button
        }
    }

}
