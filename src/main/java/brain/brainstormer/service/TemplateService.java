package brain.brainstormer.service;

import brain.brainstormer.utils.DatabaseConnection;
import brain.brainstormer.utils.Debouncer;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TemplateService {
    private static TemplateService instance;

    private final MongoCollection<Document> templatesCollection;


    public TemplateService() {
        MongoDatabase database = DatabaseConnection.getInstance().getDatabase();
        this.templatesCollection = database.getCollection("templates");

    }

    public static synchronized TemplateService getInstance() {
        if (instance == null) {
            instance = new TemplateService();
        }
        return instance;
    }

    public List<Document> getUserTemplates(String userId) {
        return templatesCollection.find(Filters.eq("userId", userId)).into(new ArrayList<>());
    }

    public void addTemplate(String userId, String name, String description) {
        Document template = new Document("userId", userId)
                .append("name", name)
                .append("description", description)
                .append("dateCreated", new Date())
                .append("components", new ArrayList<>());
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

    public void deleteTemplate(String templateId) {
        System.out.println("Attempting to delete template with ID: " + templateId);
        templatesCollection.deleteOne(Filters.eq("_id", new ObjectId(templateId)));
    }

    public void updateTemplate(String templateId, String name, String description) {
        System.out.println("Attempting to update template with ID: " + templateId);
        templatesCollection.updateOne(
                Filters.eq("_id", new ObjectId(templateId)),
                Updates.combine(
                        Updates.set("name", name),
                        Updates.set("description", description)
                )
        );
    }

    public void updateComponentInTemplate(String templateId, String componentId, Document updatedComponent) {

            try {

                templatesCollection.updateOne(
                        Filters.and(
                                Filters.eq("_id", new ObjectId(templateId)),
                                Filters.eq("components._id", componentId)
                        ),
                        Updates.combine(
                                Updates.set("components.$.config", updatedComponent.get("config")),
                                Updates.set("components.$.lastUpdated", updatedComponent.get("lastUpdated"))
                        )
                );

                System.out.println("Component updated in template successfully!");
            } catch (Exception e) {
                System.err.println("Failed to update component in template: " + e.getMessage());
            }

    }




}
