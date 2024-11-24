package brain.brainstormer.service;

import brain.brainstormer.components.core.CoreComponent;
import brain.brainstormer.utils.DatabaseConnection;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import com.mongodb.client.result.UpdateResult;
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
        // Convert the component to a Document
        Document componentData = component.toDocument();

        // Generate a new unique ID for the component in the database
        String newComponentId = new ObjectId().toHexString();
        componentData.put("_id", newComponentId); // Assign a new ID to the database document

        // Push the updated component data to the template
        templatesCollection.updateOne(
                Filters.eq("_id", new ObjectId(templateId)), // Match the template by ID
                Updates.push("components", componentData) // Add the new component to the array
        );

        System.out.println("Component added to template in MongoDB with new database ID: " + newComponentId);
    }


    // Adds multiple components to a grouper's children in MongoDB
    public void addComponentsToGrouper(String templateId, String grouperId, List<CoreComponent> components) {
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

        try {
            // Debugging: Print input parameters
            System.out.println("Template ID: " + templateId);
            System.out.println("Grouper ID: " + grouperId);
            System.out.println("Components to Add: " + componentDocuments);



//             Perform the update
            UpdateResult result = templatesCollection.updateOne(
                    Filters.and(
                            Filters.eq("_id", new ObjectId(templateId)), // Find the template
                            Filters.eq("components._id", grouperId) // Match the specific Grouper
                    ),
                    Updates.pushEach("components.$.children", componentDocuments) // Add components to children
            );

            // Check the result of the update
            if (result.getModifiedCount() > 0) {
                System.out.println("Components successfully added to Grouper in MongoDB.");
            } else {
                System.err.println("No matching Grouper found or no updates made.");
            }


        } catch (Exception e) {
            System.err.println("Failed to add components to Grouper: " + e.getMessage());
        }
    }







    public MongoCollection<Document> getComponentsCollection() {
        return componentsCollection;
    }
}










