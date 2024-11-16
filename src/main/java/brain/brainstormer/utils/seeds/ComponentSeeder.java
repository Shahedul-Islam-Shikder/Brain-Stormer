package brain.brainstormer.utils.seeds;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;

public class ComponentSeeder {
    private final MongoCollection<Document> componentCollection;

    public ComponentSeeder(MongoDatabase database) {
        this.componentCollection = database.getCollection("components");
    }

    public void seedComponents() {
        // Clean the existing components collection
        componentCollection.drop();

        // Current timestamp for createdAt and lastUpdated fields
        String currentTimestamp = Instant.now().toString();

        // Define components with updated structure
        List<Document> components = Arrays.asList(
                new Document()
                        .append("type", "checkbox")
                        .append("config", new Document()
                                .append("checked", false)
                                .append("title", "Checkbox Title")
                                .append("description", "A simple checkbox for true/false states"))
                        .append("createdAt", currentTimestamp)
                        .append("lastUpdated", currentTimestamp),

                new Document()
                        .append("type", "textfield")
                        .append("config", new Document()
                                .append("text", "")
                                .append("description", "A text field for notes or inputs"))
                        .append("createdAt", currentTimestamp)
                        .append("lastUpdated", currentTimestamp),

                new Document()
                        .append("type", "datepicker")
                        .append("config", new Document()
                                .append("selectedDate", "2024-11-16")
                                .append("description", "Select a date for the event"))
                        .append("createdAt", currentTimestamp)
                        .append("lastUpdated", currentTimestamp),

                new Document()
                        .append("type", "heading")
                        .append("config", new Document()
                                .append("title", "Heading Title")
                                .append("level", 1) // Default level is h1
                                .append("description", "Heading component for section titles"))
                        .append("createdAt", currentTimestamp)
                        .append("lastUpdated", currentTimestamp),

                new Document()
                        .append("type", "line_breaker")
                        .append("config", new Document()
                                .append("description", "A simple line to separate sections"))
                        .append("createdAt", currentTimestamp)
                        .append("lastUpdated", currentTimestamp)
        );

        // Insert each component metadata into the database
        componentCollection.insertMany(components);
        System.out.println("Component metadata seeded successfully.");
    }
}
