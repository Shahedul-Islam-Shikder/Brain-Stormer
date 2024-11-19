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

    // Fetch all templates from the database
    public List<String> getTemplateNames() {
        List<String> templateNames = new ArrayList<>();
        for (Document doc : templatesCollection.find()) {
            templateNames.add(doc.getString("name")); // Assuming the template has a "name" field
        }
        return templateNames;
    }

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

    public MongoCollection<Document> getComponentsCollection() {
        return componentsCollection;
    }
}
