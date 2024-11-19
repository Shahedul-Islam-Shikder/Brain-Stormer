package brain.brainstormer.service;

import brain.brainstormer.components.core.CoreComponent;
import brain.brainstormer.utils.DatabaseConnection;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.List;


public class ComponentService {

    private static ComponentService instance;
    private final MongoCollection<Document> templatesCollection;
    private final MongoCollection<Document> componentsCollection; // Collection for component metadata

    private ComponentService() {
        MongoDatabase database = DatabaseConnection.getInstance().getDatabase();
        templatesCollection = database.getCollection("templates");
        componentsCollection = database.getCollection("components");
    }

//    // Fetch all templates from the database
//    public List<String> getTemplateNames() {
//        List<String> templateNames = new ArrayList<>();
//        for (Document doc : templatesCollection.find()) {
//            templateNames.add(doc.getString("name")); // Assuming the template has a "name" field
//        }
//        return templateNames;
//    }

    public static ComponentService getInstance() {
        if (instance == null) {
            instance = new ComponentService();
        }
        return instance;
    }

    // Adds a configured component to the template's component array in MongoDB
    public void addComponentToTemplate(String templateId, CoreComponent component) {
        Document componentData = component.toDocument();

        templatesCollection.updateOne(
                Filters.eq("_id", new ObjectId(templateId)),
                Updates.push("components", componentData)
        );
        System.out.println("Component added to template in MongoDB.");
    }

    // Adds multiple components to a grouper's children in MongoDB
    public void addComponentsToGrouper(String grouperId, List<CoreComponent> components) {
        if (components == null || components.isEmpty()) {
            System.out.println("No components to add to the Grouper.");
            return;
        }

        // Convert CoreComponents to Documents
        List<Document> componentDocuments = new ArrayList<>();
        for (CoreComponent component : components) {
            Document componentData = component.toDocument();
            if (componentData != null) {
                componentDocuments.add(componentData);
            }
        }

        if (componentDocuments.isEmpty()) {
            System.out.println("No valid components to add to the Grouper.");
            return;
        }

        // Use $push with $each to add multiple components in one operation
        try {
            componentsCollection.updateOne(
                    Filters.eq("_id", new ObjectId(grouperId)),
                    Updates.pushEach("children", componentDocuments)
            );
            System.out.println("Components added to Grouper in MongoDB.");
        } catch (Exception e) {
            System.err.println("Failed to add components to Grouper: " + e.getMessage());
        }
    }




    public MongoCollection<Document> getComponentsCollection() {
        return componentsCollection;
    }
}
