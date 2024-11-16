package brain.brainstormer.components.core;

import javafx.scene.Node;
import org.bson.Document;

public abstract class CoreComponent {
    // This class is the base class for all components in the application.
    // It contains the basic properties that all components should have.
    // It also contains the abstract methods that all components should implement.

    private String id;
    private String type;
    private String description;

    public CoreComponent(String id, String type, String description) {
        this.id = id;
        this.type = type;
        this.description = description;
    }

    public String getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public String getDescription() {
        return description;
    }

    // render() method is used to render the component in the UI.

    public abstract Node render();

    // toDocument() method is used to convert the component to a Document object.

    public abstract Document toDocument();
}
