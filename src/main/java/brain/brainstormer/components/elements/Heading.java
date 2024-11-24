package brain.brainstormer.components.elements;

import brain.brainstormer.components.core.CoreComponent;
import brain.brainstormer.components.interfaces.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import org.bson.Document;

import java.util.List;
import javafx.scene.control.TextField;

public class Heading extends CoreComponent implements Initializable {
    private String title;
    private int headingLevel;

    public Heading(String id, String description, String title, int headingLevel) {
        super(id, "heading", description);
        this.title = title;
        this.headingLevel = headingLevel;
    }

    @Override
    public Node render() {
        Label headingLabel = new Label(title);

        setFontSize(headingLabel, headingLevel);
        VBox container = new VBox(headingLabel);
        return container;
    }

    private void setFontSize(Label label, int level) {
        switch (level) {
            case 1:
                label.setStyle("-fx-font-size: 32px; -fx-font-family: 'Arial';");

                break;
            case 2:
                label.setStyle("-fx-font-size: 28px; -fx-font-family: 'Arial';"); // h2 size
                break;
            case 3:
                label.setStyle("-fx-font-size: 24px; -fx-font-family: 'Arial';"); // h3 size
                break;
            default:
                label.setStyle("-fx-font-size: 20px; -fx-font-family: 'Arial';"); // Default size
                break;
        }
    }


    @Override
    public List<Node> getInputFields() {
        TextField titleField = new TextField(title);
        titleField.setPromptText("Enter heading title");

        TextField levelField = new TextField(String.valueOf(headingLevel));
        levelField.setPromptText("Enter heading level (1, 2, ...)");

        titleField.textProperty().addListener((obs, oldText, newText) -> title = newText);
        levelField.textProperty().addListener((obs, oldVal, newVal) -> {
            try {
                headingLevel = Integer.parseInt(newVal);
            } catch (NumberFormatException ignored) {
                headingLevel = 1; // default to h1 if invalid
            }
        });

        return List.of(titleField, levelField);
    }

    @Override
    public Document toDocument() {
        return new Document("_id", getId())
                .append("type", "heading")
                .append("config", new Document("title", title)
                        .append("level", headingLevel)
                        .append("description", getDescription()));
    }

    @Override
    public void saveToDatabase() {

    }
}