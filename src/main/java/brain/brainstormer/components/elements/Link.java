package brain.brainstormer.components.elements;

import brain.brainstormer.components.core.CoreComponent;
import brain.brainstormer.components.interfaces.Initializable;
import brain.brainstormer.service.ComponentService;
import brain.brainstormer.service.TemplateService;
import brain.brainstormer.utilGui.ComponentDialogBox;
import brain.brainstormer.utils.Debouncer;
import brain.brainstormer.utils.TemplateData;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.bson.Document;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class Link extends CoreComponent implements Initializable {
    private String linkText;
    private String url;

    public Link(String id, String description, String linkText, String url) {
        super(id, "link", description);
        this.linkText = linkText;
        this.url = url;
    }

    @Override
    public List<Node> getInputFields() {
        TextField linkTextField = new TextField(linkText);
        linkTextField.setPromptText("Enter display text for the link");

        TextField urlField = new TextField(url);
        urlField.setPromptText("Enter URL");

        linkTextField.textProperty().addListener((obs, oldText, newText) -> linkText = newText);
        urlField.textProperty().addListener((obs, oldUrl, newUrl) -> url = newUrl);

        return List.of(linkTextField, urlField);
    }

    @Override
    public Node render() {
        // Create the hyperlink
        Hyperlink link = new Hyperlink(linkText);
        link.setStyle("-fx-text-fill: #1e90ff; -fx-font-size: 16px;");
        link.setOnAction(e -> openLinkInBackground(url));

        // Create the Edit and Delete buttons
        HBox buttonContainer = new HBox(10);
        buttonContainer.setStyle("-fx-alignment: center-left;");
        buttonContainer.getStyleClass().add("button-container"); // CSS class for styling

        FontAwesomeIconView editIcon = new FontAwesomeIconView(FontAwesomeIcon.PENCIL);
        editIcon.getStyleClass().add("edit-icon");

        Button editButton = new Button("", editIcon); // Icon-only button
        editButton.setOnAction(event -> {
            // Open edit dialog
            ComponentDialogBox editDialog = new ComponentDialogBox(this, true, ComponentService.getInstance(), TemplateData.getInstance().getCurrentTemplateId());
            editDialog.showDialog();
            System.out.println("Editing Link component with ID: " + getId());
        });

        FontAwesomeIconView deleteIcon = new FontAwesomeIconView(FontAwesomeIcon.TRASH);
        deleteIcon.getStyleClass().add("delete-icon");

        Button deleteButton = new Button("", deleteIcon); // Icon-only button
        deleteButton.setOnAction(event -> {
            System.out.println("Deleting Link component with ID: " + getId());
            delete(); // Call the delete method
        });

        buttonContainer.getChildren().addAll(editButton, deleteButton);

        // Wrap everything in a VBox
        VBox container = new VBox(10); // Vertical spacing between Link and buttons
        container.setStyle("-fx-background-color: #1c1c1c; -fx-background-radius: 15; -fx-padding: 10;");
        container.setPadding(new Insets(10));
        container.setSpacing(10);
        container.getChildren().addAll(link, buttonContainer);

        applyGlobalComponentStyles(container);

        return container;
    }


    private void openLinkInBackground(String url) {
        CompletableFuture.runAsync(() -> {
            try {
                java.awt.Desktop.getDesktop().browse(new java.net.URI(url));
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }

    @Override
    public Document toDocument() {
        return new Document("_id", getId())
                .append("type", "link")
                .append("config", new Document("linkText", linkText)
                        .append("url", url)
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

            // Prepare the updated component document
            Document updatedComponent = new Document("config", new Document("linkText", linkText)
                    .append("url", url)
                    .append("description", getDescription()));

            // Debounce the save operation
            String debouncerKey = templateId + ":" + getId(); // Unique key for this Link
            new Debouncer<>(1000).debounce(debouncerKey, () -> {
                System.out.println("Debounced update triggered for Link: " + getId());
                templateService.updateComponentInTemplate(templateId, getId(), updatedComponent);
            });

        } catch (Exception e) {
            System.err.println("Failed to save Link state to database: " + e.getMessage());
        }
    }

}
