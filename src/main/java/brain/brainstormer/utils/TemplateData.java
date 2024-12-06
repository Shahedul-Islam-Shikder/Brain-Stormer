package brain.brainstormer.utils;

import java.util.List;

public class TemplateData {
    private static TemplateData instance;

    private String currentTemplateId;
    private String currentTemplateType; // public or private
    private List<String> editors; // List of editor user IDs
    private List<String> viewers; // List of viewer user IDs

    private TemplateData() { }

    public static TemplateData getInstance() {
        if (instance == null) {
            instance = new TemplateData();
        }
        return instance;
    }


    // Getter for Template ID
    public String getCurrentTemplateId() {
        return currentTemplateId;
    }

    // Setter for Template ID (internal use only)
    public void setCurrentTemplateId(String templateId) {
        this.currentTemplateId = templateId;
    }

    // Getter for Template Type
    public String getCurrentTemplateType() {
        return currentTemplateType;
    }

    // Setter for Template Type (internal use only)
    void setCurrentTemplateType(String templateType) {
        this.currentTemplateType = templateType;
    }

    // Getter for Editors
    public List<String> getEditors() {
        return editors;
    }

    // Setter for Editors (internal use only)
    void setEditors(List<String> editors) {
        this.editors = editors;
    }

    // Getter for Viewers
    public List<String> getViewers() {
        return viewers;
    }

    // Setter for Viewers (internal use only)
    void setViewers(List<String> viewers) {
        this.viewers = viewers;
    }

    // Reset data (internal use only, e.g., when switching templates)
    void reset() {
        currentTemplateId = null;
        currentTemplateType = null;
        if (editors != null) editors.clear();
        if (viewers != null) viewers.clear();
    }
}
