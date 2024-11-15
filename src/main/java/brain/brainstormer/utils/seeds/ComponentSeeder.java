package brain.brainstormer.utils.seeds;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import java.util.Arrays;
import java.util.List;

public class ComponentSeeder {
    private final MongoCollection<Document> componentCollection;

    public ComponentSeeder(MongoDatabase database) {
        this.componentCollection = database.getCollection("components");
    }

    public void seedComponents() {
        List<ComponentMetadata> components = Arrays.asList(
                new ComponentMetadata("Checkbox", "A simple checkbox for true/false states", "input", "{\"checked\":false}"),
                new ComponentMetadata("TextArea", "A text area for multiline input", "input", "{\"placeholder\":\"Enter text...\"}"),
                new ComponentMetadata("DatePicker", "Select a date", "input", "{\"format\":\"yyyy-MM-dd\"}")

        );

        for (ComponentMetadata component : components) {
            componentCollection.insertOne(component.toDocument());
        }
        System.out.println("Component metadata seeded successfully.");
    }
}
