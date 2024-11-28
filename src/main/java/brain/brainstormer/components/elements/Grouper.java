package brain.brainstormer.components.elements;

import brain.brainstormer.components.core.CoreComponent;
import brain.brainstormer.service.ComponentService;
import brain.brainstormer.utilGui.AddComponentDialog;
import brain.brainstormer.utils.TemplateData;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;

public class Grouper extends CoreComponent {
    private final String layout; // "HBox" or "VBox"
    private final String alignment; // Alignment like "CENTER", "space-between", etc.
    private final int spacing;
    private final List<Node> children;
    private Node container; // Main container (VBox for outer structure)
    private Node layoutContainer; // Holds the inner HBox or VBox for child layout

    public Grouper(String id, String type, String alignment, int spacing) {
        super(id, type, "A grouper component for dynamic layouts");
        this.layout = type.equals("h-group") ? "HBox" : "VBox";
        this.alignment = alignment != null ? alignment : "CENTER"; // Default alignment
        this.spacing = spacing >= 0 ? spacing : 20; // Default spacing
        this.children = new ArrayList<>(); // Empty children by default
        this.container = createContainer(); // Initialize the container
    }

    private Node createContainer() {
        VBox mainContainer = new VBox(spacing); // Outer container
        mainContainer.setSpacing(5);
//        mainContainer.setPadding(new Insets(20, 20, 20, 20));
        mainContainer.setStyle("-fx-background-color: #121212; -fx-background-radius: 20px;");

        if (layout.equals("HBox")) {
            HBox hbox = new HBox(spacing); // Inner HBox
            applyAlignment(hbox);
            hbox.setStyle("-fx-background-color: #000000; -fx-background-radius: 20px;");
            hbox.setPadding(new Insets(15, 20, 15, 20)); // Top, Right, Bottom, Left padding
            hbox.getChildren().addAll(children); // Add initial children
            layoutContainer = hbox; // Track the layout container
            mainContainer.getChildren().add(hbox);
        } else {
            VBox vbox = new VBox(spacing); // Inner VBox
            applyAlignment(vbox);
            vbox.setStyle("-fx-background-color: #000000; -fx-background-radius: 20px;");
            vbox.setPadding(new Insets(15, 20, 15, 20)); // Top, Right, Bottom, Left padding
            vbox.getChildren().addAll(children); // Add initial children
            layoutContainer = vbox; // Track the layout container
            mainContainer.getChildren().add(vbox);
        }


        // Add the "+" button for adding components
        Button plusButton = createPlusButton(layoutContainer);
        mainContainer.getChildren().add(plusButton);

        return mainContainer;
    }

    private void applyAlignment(Node layoutNode) {
        if (layoutNode instanceof HBox) {
            HBox hbox = (HBox) layoutNode;
            if ("space-between".equalsIgnoreCase(alignment)) {
                hbox.setSpacing(0); // No spacing as regions handle the space
            } else {
                hbox.setAlignment(javafx.geometry.Pos.valueOf(alignment));
            }
        } else if (layoutNode instanceof VBox) {
            VBox vbox = (VBox) layoutNode;
            if ("space-between".equalsIgnoreCase(alignment)) {
                vbox.setSpacing(0); // No spacing for space-between
            } else {
                vbox.setAlignment(javafx.geometry.Pos.valueOf(alignment));
            }
        }
    }

    private Button createPlusButton(Node parentContainer) {
        Button plusButton = new Button();
        plusButton.getStyleClass().add("grouper-plus-button");
        plusButton.setStyle("-fx-background-radius: 10000px; -fx-background-color: transparent; -fx-border-color: transparent;");
        plusButton.setPrefSize(37, 37);

        FontAwesomeIconView iconView = new FontAwesomeIconView(FontAwesomeIcon.PLUS);
        iconView.setFill(javafx.scene.paint.Color.GREY);
        iconView.setStroke(javafx.scene.paint.Color.BLACK);
        iconView.setSize("18px");
        plusButton.setGraphic(iconView);

        plusButton.setOnAction(event -> openAddComponentDialog(parentContainer));
        return plusButton;
    }

    private void openAddComponentDialog(Node parentContainer) {
        String templateId = TemplateData.getInstance().getCurrentTemplateId();

        if (templateId == null || templateId.isEmpty()) {
            System.err.println("No template ID is set. Please set a valid template ID before adding components.");
            return;
        }

        ComponentService componentService = ComponentService.getInstance();
        AddComponentDialog dialog = new AddComponentDialog(templateId, this, componentService);
        dialog.init();
    }

    @Override
    public Node render() {
        return container;
    }

    @Override
    public Document toDocument() {
        return new Document("_id", getId())
                .append("type", layout.equals("HBox") ? "h-group" : "v-group")
                .append("config", new Document("alignment", alignment)
                        .append("spacing", spacing))
                .append("children", new ArrayList<>())
                .append("createdAt", new Document("$currentDate", true))
                .append("lastUpdated", new Document("$currentDate", true));
    }

    @Override
    public void saveToDatabase() {
        // Placeholder for database save functionality
    }

    @Override
    public void delete() {

    }

    public void addChild(Node child) {
        children.add(child);

        if (layoutContainer instanceof HBox && "space-between".equalsIgnoreCase(alignment)) {
            System.out.println("space-between");
            HBox hbox = (HBox) layoutContainer;
            updateSpaceBetweenLayout(hbox, children);
        } else if (layoutContainer instanceof VBox && "space-between".equalsIgnoreCase(alignment)) {
            VBox vbox = (VBox) layoutContainer;
            updateSpaceBetweenLayout(vbox, children);
        } else if (layoutContainer instanceof HBox) {
            ((HBox) layoutContainer).getChildren().add(child);
        } else if (layoutContainer instanceof VBox) {
            ((VBox) layoutContainer).getChildren().add(child);
        }
    }

    private void updateSpaceBetweenLayout(Node layoutNode, List<Node> children) {
        if (layoutNode instanceof HBox) {
            HBox hbox = (HBox) layoutNode;
            hbox.getChildren().clear();

            for (int i = 0; i < children.size(); i++) {
                hbox.getChildren().add(children.get(i));
                if (i < children.size() - 1) {
                    hbox.getChildren().add(createStretchRegion());
                }
            }
        } else if (layoutNode instanceof VBox) {
            VBox vbox = (VBox) layoutNode;
            vbox.getChildren().clear();

            for (int i = 0; i < children.size(); i++) {
                vbox.getChildren().add(children.get(i));
                if (i < children.size() - 1) {
                    vbox.getChildren().add(createStretchRegion());
                }
            }
        }
    }

    private Region createStretchRegion() {
        Region region = new Region();
        if (layoutContainer instanceof HBox) {
            HBox.setHgrow(region, javafx.scene.layout.Priority.ALWAYS);
        } else if (layoutContainer instanceof VBox) {
            VBox.setVgrow(region, javafx.scene.layout.Priority.ALWAYS);
        }
        return region;
    }
}
