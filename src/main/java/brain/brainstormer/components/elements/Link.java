package brain.brainstormer.components.elements;

import brain.brainstormer.components.core.CoreComponent;
import brain.brainstormer.components.interfaces.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
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
        Hyperlink link = new Hyperlink(linkText);
        link.setStyle("-fx-text-fill: #1e90ff; -fx-font-size: 16px;");
        link.setOnAction(e -> openLinkInBackground(url));

        HBox container = new HBox(link);
        container.setStyle("-fx-background-color: #1c1c1c; -fx-background-radius: 15; -fx-padding: 10;");
        container.setPadding(new Insets(10));
        container.setSpacing(10);
        container.setPrefHeight(Region.USE_COMPUTED_SIZE);
        container.setPrefWidth(Region.USE_COMPUTED_SIZE);

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

    }

    @Override
    public void delete() {

    }
}
