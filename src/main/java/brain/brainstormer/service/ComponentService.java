package brain.brainstormer.service;

import brain.brainstormer.utils.DatabaseConnection;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Projections;
import com.mongodb.client.model.Updates;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.List;

public class ComponentService {

    private static ComponentService instance; // Singleton instance
    private final MongoCollection<Document> componentsCollection;
    private final MongoCollection<Document> templatesCollection;

    // Private constructor to prevent direct instantiation
    private ComponentService() {
        MongoDatabase database = DatabaseConnection.getInstance().getDatabase();
        componentsCollection = database.getCollection("components");
        templatesCollection = database.getCollection("templates");
    }

    // Static method to get the singleton instance
    public static ComponentService getInstance() {
        if (instance == null) {
            instance = new ComponentService();
        }
        return instance;
    }

    // Retrieve all component names
    public List<String> getAllComponentNames() {
        List<String> componentNames = new ArrayList<>();
        for (Document doc : componentsCollection.find().projection(Projections.include("name"))) {
            componentNames.add(doc.getString("name"));
        }
        return componentNames;
    }

    // Add the selected component to a template by templateId
    public void addComponentToTemplate(String templateId, String componentName) {
        Document component = componentsCollection.find(new Document("name", componentName)).first();

        if (component != null) {
            // Prepare component data to add to the template
            Document componentData = new Document("name", component.getString("name"))
                    .append("description", component.getString("description"))
                    .append("defaultProperties", component.getString("defaultProperties"));

            // Update the template with the new component in a flattened "components" array
            templatesCollection.updateOne(
                    new Document("_id", new ObjectId(templateId)),
                    new Document("$push", new Document("components", componentData))
            );

            System.out.println("Component added to template in MongoDB.");
        } else {
            System.out.println("Component not found in components collection.");
        }
    }

    public void updateComponentInTemplate(String templateId, String componentId, Document updatedComponentData) {
        templatesCollection.updateOne(
                Filters.and(
                        Filters.eq("_id", new ObjectId(templateId)),  // Find the template
                        Filters.elemMatch("components", Filters.eq("id", componentId)) // Find the component in the components array
                ),
                Updates.set("components.$", updatedComponentData) // Update the matched component
        );

        System.out.println("Component updated in template ID: " + templateId);
    }

    // Add this method to get the components collection
    public MongoCollection<Document> getComponentsCollection() {
        return componentsCollection;
    }
}
