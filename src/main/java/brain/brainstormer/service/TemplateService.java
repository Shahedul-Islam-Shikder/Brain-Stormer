package brain.brainstormer.service;

import brain.brainstormer.utils.DatabaseConnection;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TemplateService {
    private final MongoCollection<Document> templatesCollection;

    public TemplateService() {
        MongoDatabase database = DatabaseConnection.getInstance().getDatabase();
        templatesCollection = database.getCollection("templates");
    }

    // Retrieve templates for a specific user
    public List<Document> getUserTemplates(String userId) {
        return templatesCollection.find(Filters.eq("userId", userId)).into(new ArrayList<>());
    }

    // Add a new template for a specific user
    public void addTemplate(String userId, String name, String description) {
        Document template = new Document("userId", userId)
                .append("name", name)
                .append("description", description)
                .append("dateCreated", new Date())
                .append("components", new ArrayList<>()); // Initialize components as an empty array
        templatesCollection.insertOne(template);
    }

    public Document getTemplateById(String templateId) {
        try {
            return templatesCollection.find(Filters.eq("_id", new ObjectId(templateId))).first();
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid template ID format: " + templateId);
            return null;
        }
    }


    // Delete a template by ID
    public void deleteTemplate(String templateId) {
        System.out.println("Attempting to delete template with ID: " + templateId);
        templatesCollection.deleteOne(Filters.eq("_id", new org.bson.types.ObjectId(templateId)));
    }

    public void updateTemplate(String templateId, String name, String description) {
        System.out.println("Attempting to update template with ID: " + templateId);
        templatesCollection.updateOne(
                Filters.eq("_id", new ObjectId(templateId)),
                new Document("$set", new Document("name", name).append("description", description))
        );
    }

}
