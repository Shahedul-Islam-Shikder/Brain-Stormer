package brain.brainstormer.components.core;

import brain.brainstormer.components.elements.*;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.time.LocalDate;
import java.util.List;

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
                return new CheckBox(id, description != null ? description : "No description", isChecked, title);

            case "textarea":
                if (config == null) {
                    System.out.println("Error: Config document is missing for textarea component.");
                    return null;
                }
                String initialText = config.getString("text");
                int rows = config.getInteger("rows", 5); // Default to 5 if missing
                return new TextArea(id, description != null ? description : "No description", initialText != null ? initialText : "", rows);

            case "datepicker":
                if (config == null) {
                    System.out.println("Error: Config document is missing for datepicker component.");
                    return null;
                }
                String dateString = config.getString("selectedDate");
                LocalDate selectedDate = dateString != null && !dateString.isEmpty() ? LocalDate.parse(dateString) : null;
                return new DatePicker(id, type, description != null ? description : "No description", selectedDate);

            case "heading":
                if (config == null) {
                    System.out.println("Error: Config document is missing for heading component.");
                    return null;
                }
                String headingTitle = config.getString("title");
                int level = config.getInteger("level", 1);
                return new Heading(id, description, headingTitle != null ? headingTitle : "Untitled", level);

            case "stopwatch":
                if (config == null) {
                    System.out.println("Error: Config document is missing for stopwatch component.");
                    return null;
                }
                long elapsedTime = config.get("elapsedTime") instanceof Number
                        ? ((Number) config.get("elapsedTime")).longValue()
                        : 0L; // Default to 0 if not present or not numeric
                boolean isRunning = config.getBoolean("isRunning", false); // Default to false
                return new Stopwatch(id, description != null ? description : "Stopwatch", elapsedTime, isRunning);

            case "line_breaker":
                return new LineBreaker(id, description != null ? description : "No description");

            case "link":
                if (config == null) {
                    System.out.println("Error: Config document is missing for link component.");
                    return null;
                }
                String linkText = config.getString("linkText");
                String url = config.getString("url");
                return new Link(id, description, linkText != null ? linkText : "Open Link", url != null ? url : "");

            case "code_snippet":
                if (config == null) {
                    System.out.println("Error: Config document is missing for code snippet component.");
                    return null;
                }
                String codeContent = config.getString("code");
                return new CodeSnippet(id, description != null ? description : "Code Snippet", codeContent != null ? codeContent : "");
            case "rich_text_editor":
                if (config == null) {
                    System.out.println("Error: Config document is missing for rich text editor component.");
                    return null;
                }
                String htmlContent = config.getString("htmlContent");
                return new RichTextEditor(id, description != null ? description : "Rich Text Editor", htmlContent);
            case "h-group":
            case "v-group":
                if (config == null) {
                    System.out.println("Error: Config document is missing for grouper component.");
                    return null;
                }

                String alignment = config.getString("alignment");
                int spacing = config.getInteger("spacing", 10); // Default spacing
                Grouper grouper = new Grouper(id, type, alignment != null ? alignment : "CENTER_LEFT", spacing);

                // Process children
                List<Document> children = metadata.getList("children", Document.class);
                if (children != null) {
                    for (Document child : children) {
                        CoreComponent childComponent = createComponent(child); // Use factory recursively
                        if (childComponent != null) {
                            grouper.addChild(childComponent.render()); // Render and add to Grouper
                        }
                    }
                }
                return grouper;
            case "file":
                if (config == null) {
                    System.out.println("Error: Config document is missing for file component.");
                    return null;
                }

                String fileUrl = config.getString("fileUrl");
                String fileName = config.getString("fileName");
                return new File(
                        id,
                        description != null ? description : "No description",
                        fileUrl,
                        fileName != null ? fileName : "Unnamed File"
                );


            case "image":
                if (config == null) {
                    System.out.println("Error: Config document is missing for image component.");
                    return null;
                }
                String imageUrl = config.getString("imageUrl");
                String altText = config.getString("altText");
                return new Image(
                        id,
                        description != null ? description : "No description",
                        imageUrl, // Use the actual imageUrl or leave it null
                        altText != null ? altText : "Image Component" // Default alt text
                );
            case "gif":
                if (config == null) {
                    System.out.println("Error: Config document is missing for gif component.");
                    return null;
                }
                String gifUrl = config.getString("gifUrl");
                String gifAltText = config.getString("altText");
                return new Gif(
                        id,
                        description != null ? description : "No description",
                        gifUrl, // Use the actual gifUrl or leave it null
                        gifAltText != null ? gifAltText : "GIF Component" // Default alt text
                );
            case "weather":
                // Handle the weather component
                return new Weather( id, description != null ? description : "Weather component","Dhaka" );
            default:
                System.out.println("Component type not recognized: " + type);
                return null;
        }
    }
}
