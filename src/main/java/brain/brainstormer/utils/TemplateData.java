package brain.brainstormer.utils;

public class TemplateData {
    private static TemplateData instance;
    private String currentTemplateId;

    private TemplateData() { }

    public static TemplateData getInstance() {
        if (instance == null) {
            instance = new TemplateData();
        }
        return instance;
    }

    public String getCurrentTemplateId() {
        return currentTemplateId;
    }

    public void setCurrentTemplateId(String templateId) {
        this.currentTemplateId = templateId;
    }
}
