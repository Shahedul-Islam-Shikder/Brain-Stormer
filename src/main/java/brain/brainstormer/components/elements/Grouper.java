package brain.brainstormer.components.elements;

import brain.brainstormer.components.core.CoreComponent;
import brain.brainstormer.components.interfaces.Initializable;
import brain.brainstormer.service.ComponentService;
import brain.brainstormer.utilGui.AddComponentDialog;
import brain.brainstormer.utilGui.ComponentDialogBox;
import brain.brainstormer.utils.TemplateData;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;

public class Grouper extends CoreComponent implements Initializable {
    private final String layout; // "HBox" or "VBox"
    private String alignment; // Alignment like "CENTER", "space-between", etc.
    private int spacing;
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


    @Override
    public List<Node> getInputFields() {
        // ComboBox for Alignment Options
        ComboBox<String> alignmentBox = new ComboBox<>();
        alignmentBox.getItems().addAll("CENTER", "TOP_LEFT", "TOP_CENTER", "TOP_RIGHT",
                "CENTER_LEFT", "CENTER_RIGHT", "BOTTOM_LEFT",
                "BOTTOM_CENTER", "BOTTOM_RIGHT", "space-between");
        alignmentBox.setValue(alignment); // Set the default alignment
        alignmentBox.setPromptText("Select Alignment");

        // TextField for Spacing
        TextField spacingField = new TextField(String.valueOf(spacing));
        spacingField.setPromptText("Enter Spacing (default: 20)");

        // Listeners for changes
        alignmentBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            alignment = newValue;
            applyAlignment(layoutContainer); // Dynamically update alignment
            saveToDatabase(); // Save changes to the database
        });

        spacingField.textProperty().addListener((observable, oldValue, newValue) -> {
            try {
                int newSpacing = Integer.parseInt(newValue);
                spacing = newSpacing >= 0 ? newSpacing : 20; // Validate spacing
                if (layoutContainer instanceof VBox) {
                    ((VBox) layoutContainer).setSpacing(spacing);
                } else if (layoutContainer instanceof HBox) {
                    ((HBox) layoutContainer).setSpacing(spacing);
                }
                saveToDatabase(); // Save changes to the database
            } catch (NumberFormatException ignored) {
                // Ignore invalid input
            }
        });

        return List.of(alignmentBox, spacingField);
    }


    private Node createContainer() {
        VBox mainContainer = new VBox(spacing);
        mainContainer.setSpacing(5);
        mainContainer.setStyle("-fx-background-color: #121212; -fx-background-radius: 20px;");

        // Preserve existing children
        if (layout.equals("HBox")) {
            HBox hbox = new HBox(spacing);
            applyAlignment(hbox);
            hbox.setStyle("-fx-background-color: #000000; -fx-background-radius: 20px;");
            hbox.setPadding(new Insets(15, 20, 15, 20));
            hbox.getChildren().addAll(children); // Add preserved children
            layoutContainer = hbox;
            mainContainer.getChildren().add(hbox);
        } else {
            VBox vbox = new VBox(spacing);
            applyAlignment(vbox);
            vbox.setStyle("-fx-background-color: #000000; -fx-background-radius: 20px;");
            vbox.setPadding(new Insets(15, 20, 15, 20));
            vbox.getChildren().addAll(children); // Add preserved children
            layoutContainer = vbox;
            mainContainer.getChildren().add(vbox);
        }

        HBox buttonContainer = createButtonContainer(layoutContainer);
        mainContainer.getChildren().add(buttonContainer);

        return mainContainer;
    }


    private HBox createButtonContainer(Node parentContainer) {
        HBox buttonContainer = new HBox(10); // Horizontal spacing between buttons
        buttonContainer.setPadding(new Insets(10, 0, 0, 0)); // Top padding
        buttonContainer.setStyle("-fx-alignment: center-left;");

        // Plus Button
        Button plusButton = createPlusButton(parentContainer);

        // Edit Button
        FontAwesomeIconView editIcon = new FontAwesomeIconView(FontAwesomeIcon.PENCIL);
        editIcon.getStyleClass().add("edit-icon");

        Button editButton = new Button("", editIcon); // Icon-only button
        editButton.setOnAction(event -> {
            // Open edit dialog for Grouper
            ComponentDialogBox editDialog = new ComponentDialogBox(
                    TemplateData.getInstance().getCurrentTemplateId(),
                    this,
                    true, // isEditing
                    ComponentService.getInstance(), this
            );

            editDialog.showDialog();
            System.out.println("Editing Grouper with ID: " + getId());
        });

        // Delete Button
        FontAwesomeIconView deleteIcon = new FontAwesomeIconView(FontAwesomeIcon.TRASH);
        deleteIcon.getStyleClass().add("delete-icon");

        Button deleteButton = new Button("", deleteIcon); // Icon-only button
        deleteButton.setOnAction(event -> {
            System.out.println("Deleting Grouper with ID: " + getId());
            delete(); // Call the inherited delete method
        });

        buttonContainer.getChildren().addAll(plusButton, editButton, deleteButton);

        return buttonContainer;
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


    public void addChild(Node child) {
        children.add(child);

        if (layoutContainer instanceof HBox && "space-between".equalsIgnoreCase(alignment)) {
            updateSpaceBetweenLayout((HBox) layoutContainer, children);
        } else if (layoutContainer instanceof VBox && "space-between".equalsIgnoreCase(alignment)) {
            updateSpaceBetweenLayout((VBox) layoutContainer, children);
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
