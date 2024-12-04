package brain.brainstormer.components.core;

import brain.brainstormer.controller.TemplateController;
import brain.brainstormer.service.ComponentService;
import brain.brainstormer.utils.SceneSwitcher;
import brain.brainstormer.utils.TemplateData;
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

    public abstract void saveToDatabase();
    public void delete() {

        try {

            ComponentService.getInstance().deleteComponentFromTemplate(TemplateData.getInstance().getCurrentTemplateId(), getId());
            System.out.println("Component deleted with ID: " + getId());

            TemplateController controller = SceneSwitcher.getCurrentController(TemplateController.class);
            if (controller != null) {
                controller.refreshTemplateContent(TemplateData.getInstance().getCurrentTemplateId());
            }
        } catch (Exception e) {
            System.err.println("Failed to delete component: " + e.getMessage());
        }


    }

}
