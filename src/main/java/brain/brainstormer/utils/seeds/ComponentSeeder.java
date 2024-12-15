package brain.brainstormer.utils.seeds;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import java.time.Instant;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class ComponentSeeder {
    private final MongoCollection<Document> componentCollection;

    public ComponentSeeder(MongoDatabase database) {
        this.componentCollection = database.getCollection("components");
    }
    //TODO add name ;

    public void seedComponents() {
        // Clean the existing components collection
        componentCollection.drop();

        // Define components with updated structure
        List<Document> components = Arrays.asList(
                new Document()
                        .append("type", "checkbox")
                        .append("config", new Document()
                                .append("checked", false)
                                .append("title", "Checkbox Title")
                                .append("description", "A simple checkbox for true/false states"))
                        .append("createdAt", new Date()) // Current timestamp
                        .append("lastUpdated", new Date()),
                new Document()
                        .append("type", "stopwatch")
                        .append("config", new Document()
                                .append("elapsedTime", 0) // Initial elapsed time in milliseconds
                                .append("isRunning", false) // Stopwatch starts in a paused state
                                .append("description", "A stopwatch to track elapsed time"))
                        .append("createdAt", new Date()) // Current timestamp
                        .append("lastUpdated", new Date()),

                new Document()
                        .append("type", "textarea")
                        .append("config", new Document()
                                .append("text", "")
                                .append("description", "A text field for notes or inputs"))
                        .append("createdAt", new Date()) // Current timestamp
                        .append("lastUpdated", new Date()),

                new Document()
                        .append("type", "datepicker")
                        .append("config", new Document()
                                .append("selectedDate", "2024-11-16")
                                .append("description", "Select a date for the event"))
                        .append("createdAt", new Date()) // Current timestamp
                        .append("lastUpdated", new Date()),

                new Document()
                        .append("type", "heading")
                        .append("config", new Document()
                                .append("title", "Heading Title")
                                .append("level", 1) // Default level is h1
                                .append("description", "Heading component for section titles"))
                        .append("createdAt", new Date()) // Current timestamp
                        .append("lastUpdated", new Date()),

                new Document()
                        .append("type", "line_breaker")
                        .append("config", new Document()
                                .append("description", "A simple line to separate sections"))
                        .append("createdAt", new Date()) // Current timestamp
                        .append("lastUpdated", new Date()),

                new Document()
                        .append("type", "link")
                        .append("config", new Document()
                                .append("linkText", "Open Documentation")
                                .append("url", "https://example.com")
                                .append("description", "A link to an external resource"))
                        .append("createdAt", new Date()) // Current timestamp
                        .append("lastUpdated", new Date()),

                new Document()
                        .append("type", "code_snippet")
                        .append("config", new Document()
                                .append("code", "public class HelloWorld { public static void main(String[] args) { System.out.println(\"Hello, World!\"); } }")
                                .append("description", "A simple Java HelloWorld program"))
                        .append("createdAt", new Date()) // Current timestamp
                        .append("lastUpdated", new Date()),

                new Document()
                        .append("type", "rich_text_editor")
                        .append("config", new Document()
                                .append("htmlContent",
                                        "<div style='font-family: Arial, sans-serif; color: #333; line-height: 1.6;'>" +
                                                "    <h1 style='color: #1E90FF; text-align: center;'>Welcome to the Rich Text Editor</h1>" +
                                                "    <p style='font-size: 16px;'>This is an <b>awesome</b> rich text editor component for your BrainStormer project!</p>" +
                                                "    <ul>" +
                                                "        <li style='color: #FF4500;'>Create beautiful formatted text</li>" +
                                                "        <li style='color: #32CD32;'>Add <i>colors</i>, <b>styles</b>, and links</li>" +
                                                "        <li style='color: #9400D3;'>Integrate with MongoDB for saving</li>" +
                                                "    </ul>" +
                                                "    <p>Start typing below to customize this content:</p>" +
                                                "</div>")
                                .append("description", "A fancy rich text editor for stylish content"))
                        .append("createdAt", new Date()) // Current timestamp
                        .append("lastUpdated", new Date()),

                new Document()
                        .append("type", "h-group")
                        .append("config", new Document("alignment", "CENTER_LEFT"))
                        .append("children", Arrays.asList())
                        .append("createdAt", new Date()) // Current timestamp
                        .append("lastUpdated", new Date()),

                new Document()
                        .append("type", "v-group")
                        .append("config", new Document("alignment", "TOP_CENTER"))
                        .append("children", Arrays.asList())
                        .append("createdAt", new Date()) // Current timestamp
                        .append("lastUpdated", new Date()),

                new Document()
                        .append("type", "image")
                        .append("config", new Document()
                                .append("imageUrl", null) // Placeholder image URL
                                .append("altText", "Default Image Alt Text")
                                .append("description", "An image component for displaying pictures"))
                        .append("createdAt", new Date()) // Current timestamp
                        .append("lastUpdated", new Date()),
                new Document()
                        .append("type", "gif")
                        .append("config", new Document()
                                .append("gifUrl", null) // Placeholder for the GIF URL
                                .append("altText", "Default GIF Alt Text")
                                .append("description", "A GIF component for displaying GIFs"))
                        .append("createdAt", new Date()) // Current timestamp
                        .append("lastUpdated", new Date()),


                new Document()
                        .append("type", "table")
                        .append("config", new Document()
                                .append("rowData", Arrays.asList(
                                        Arrays.asList("Header 1", "Header 2", "Header 3"), // Header row
                                        Arrays.asList("Row 1 Col 1", "Row 1 Col 2", "Row 1 Col 3"), // Data row
                                        Arrays.asList("Row 2 Col 1", "Row 2 Col 2", "Row 2 Col 3")  // Data row
                                ))
                                .append("description", "A table component with rows and columns"))
                        .append("createdAt", new Date())
                        .append("lastUpdated", new Date()),
                new Document()
                        .append("type", "weather")
                        .append("name", "Weather") // Adding name
                        .append("config", new Document()

                                .append("description", "A component for displaying weather information"))
                        .append("createdAt", new Date()) // Current timestamp
                        .append("lastUpdated", new Date()),

                // Adding File Component
                new Document()
                        .append("type", "file")
                        .append("config", new Document()
                                .append("fileUrl", null) // Placeholder for file URL
                                .append("fileName", "Sample File Name")
                                .append("description", "A file component for uploading, downloading, and managing files"))
                        .append("createdAt", new Date()) // Current timestamp
                        .append("lastUpdated", new Date())
        );

        // Insert each component metadata into the database
        componentCollection.insertMany(components);
        System.out.println("Component metadata seeded successfully.");
    }

}
