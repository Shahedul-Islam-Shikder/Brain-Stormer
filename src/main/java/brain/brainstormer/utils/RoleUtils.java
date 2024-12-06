package brain.brainstormer.utils;

public class RoleUtils {

    public static boolean canEdit(String userId) {
        TemplateData templateData = TemplateData.getInstance();
        return templateData.isAuthor(userId) || templateData.isEditor(userId);
    }

    public static boolean canDelete(String userId) {
        TemplateData templateData = TemplateData.getInstance();
        return templateData.isAuthor(userId);
    }

    public static boolean canView(String userId) {
        TemplateData templateData = TemplateData.getInstance();
        return templateData.isAuthor(userId) ||
                templateData.isEditor(userId) ||
                templateData.isViewer(userId);
    }
}
