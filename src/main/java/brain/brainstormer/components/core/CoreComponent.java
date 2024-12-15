package brain.brainstormer.components.core;

import brain.brainstormer.controller.TemplateController;
import brain.brainstormer.service.ComponentService;
import brain.brainstormer.utils.SceneSwitcher;
import brain.brainstormer.utils.StyleUtil;
import brain.brainstormer.utils.TemplateData;
import javafx.scene.Node;
import org.bson.Document;

public abstract class CoreComponent {
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

    public abstract Node render();

    public abstract Document toDocument();

    public abstract void saveToDatabase();


    public void delete() {
        try {
            String templateId = TemplateData.getInstance().getCurrentTemplateId();
            ComponentService.getInstance().deleteComponent(templateId, getId());
            System.out.println("Component deleted with ID: " + getId());

            // Notify via WebSocket
            TemplateController controller = SceneSwitcher.getCurrentController(TemplateController.class);
            if (controller != null && controller.getSocket() != null) {
                controller.getSocket().sendMessage("{\"type\": \"update\", \"roomId\": \"" + templateId + "\"}");
            }

            // Refresh the UI
            if (controller != null) {
                controller.refreshTemplateContent(templateId);
            }
        } catch (Exception e) {
            System.err.println("Failed to delete component: " + e.getMessage());
        }
    }


    // Apply component-global.css by default to all components
    public void applyGlobalComponentStyles(Node node) {
        StyleUtil.applyCustomStylesheet(node, StyleUtil.COMPONENT_GLOBAL_STYLESHEET_PATH);
    }

    // Apply specific styles for this component
    public void applyStyles(Node node, String stylesheetPath) {
        if (node != null) {
            StyleUtil.applyCustomStylesheet(node, stylesheetPath);
        }
    }
}
