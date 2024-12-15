package brain.brainstormer.config;

import java.util.List;

public class TemplateData {
    private static TemplateData instance;

    private String currentTemplateId;
    private String currentTemplateType; // public or private
    private String author; // Author's userId
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

    // Setter for Template ID
    public void setCurrentTemplateId(String templateId) {
        this.currentTemplateId = templateId;
    }

    // Getter for Template Type (public or private)
    public String getCurrentTemplateType() {
        return currentTemplateType;
    }

    // Setter for Template Type
    public void setCurrentTemplateType(String templateType) {
        this.currentTemplateType = templateType;
    }

    // Getter for Author
    public String getAuthor() {
        return author;
    }

    // Setter for Author
    public void setAuthor(String author) {
        this.author = author;
    }

    // Getter for Editors
    public List<String> getEditors() {
        return editors;
    }

    // Setter for Editors
    public void setEditors(List<String> editors) {
        this.editors = editors;
    }

    // Getter for Viewers
    public List<String> getViewers() {
        return viewers;
    }

    // Setter for Viewers
    public void setViewers(List<String> viewers) {
        this.viewers = viewers;
    }

    // Role checking methods
    public boolean isAuthor(String userId) {
        return author != null && author.equals(userId);
    }

    public boolean isEditor(String userId) {
        return editors != null && editors.contains(userId);
    }

    public boolean isViewer(String userId) {
        return viewers != null && viewers.contains(userId);
    }

    // Method to check if the template is private
    public boolean isPrivate() {
        return "private".equalsIgnoreCase(currentTemplateType);
    }

    // Method to check if the template is public
    public boolean isPublic() {
        return "public".equalsIgnoreCase(currentTemplateType);
    }

    // Reset all data (useful when switching templates)
    public void reset() {
        currentTemplateId = null;
        currentTemplateType = null;
        author = null;
        if (editors != null) editors.clear();
        if (viewers != null) viewers.clear();
    }
}
