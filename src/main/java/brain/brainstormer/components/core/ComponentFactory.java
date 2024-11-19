package brain.brainstormer.components.core;

import brain.brainstormer.components.elements.*;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.time.LocalDate;

public class ComponentFactory {

    // Factory method to create components based on metadata document
    public static CoreComponent createComponent(Document metadata) {
        if (metadata == null) {
            System.out.println("Error: Metadata document is null.");
            return null;
        }

        String type = metadata.getString("type");
        if (type == null) {
            System.out.println("Error: Component type is missing in metadata: " + metadata.toJson());
            return null;
        }

        // Retrieve `_id` as a String, whether it's stored as an `ObjectId` or `String` in MongoDB
        String id;
        if (metadata.get("_id") instanceof ObjectId) {
            id = metadata.getObjectId("_id").toString();
        } else {
            id = metadata.getString("_id");
        }

        String description = metadata.getString("description");
        Document config = metadata.get("config", Document.class);

        switch (type) {
            case "checkbox":
                if (config == null) {
                    System.out.println("Error: Config document is missing for checkbox component.");
                    return null;
                }
                boolean isChecked = config.getBoolean("checked", false);
                String title = config.getString("title");
                return new CheckBoxComponent(id, description != null ? description : "No description", isChecked, title);

            case "textarea":
                if (config == null) {
                    System.out.println("Error: Config document is missing for textarea component.");
                    return null;
                }
                String initialText = config.getString("text");
                int rows = config.getInteger("rows", 5); // Default to 5 if missing
                return new TextAreaComponent(id, description != null ? description : "No description", initialText != null ? initialText : "", rows);

            case "datepicker":
                if (config == null) {
                    System.out.println("Error: Config document is missing for datepicker component.");
                    return null;
                }
                String dateString = config.getString("selectedDate");
                LocalDate selectedDate = dateString != null && !dateString.isEmpty() ? LocalDate.parse(dateString) : null;
                return new DatePickerComponent(id, type, description != null ? description : "No description", selectedDate);

            case "heading":
                if (config == null) {
                    System.out.println("Error: Config document is missing for heading component.");
                    return null;
                }
                String headingTitle = config.getString("title");
                int level = config.getInteger("level", 1);
                return new HeadingComponent(id, description, headingTitle != null ? headingTitle : "Untitled", level);

            case "line_breaker":
                return new LineBreakerComponent(id, description != null ? description : "No description");

            case "link":
                if (config == null) {
                    System.out.println("Error: Config document is missing for link component.");
                    return null;
                }
                String linkText = config.getString("linkText");
                String url = config.getString("url");
                return new LinkComponent(id, description, linkText != null ? linkText : "Open Link", url != null ? url : "");

            case "code_snippet":
                if (config == null) {
                    System.out.println("Error: Config document is missing for code snippet component.");
                    return null;
                }
                String codeContent = config.getString("code");
                return new CodeSnippetComponent(id, description != null ? description : "Code Snippet", codeContent != null ? codeContent : "");
            case "rich_text_editor":
                if (config == null) {
                    System.out.println("Error: Config document is missing for rich text editor component.");
                    return null;
                }
                String htmlContent = config.getString("htmlContent");
                return new RichTextEditorComponent(id, description != null ? description : "Rich Text Editor", htmlContent);


            default:
                System.out.println("Component type not recognized: " + type);
                return null;
        }
    }
}
