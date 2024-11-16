package brain.brainstormer.components.interfaces;

import javafx.scene.Node;
import org.bson.Document;

import java.util.List;

public interface Initializable {
    List<Node> getInputFields(); // For gathering initial values
    Document toDocument(); // Convert component data to Document format for saving
}
