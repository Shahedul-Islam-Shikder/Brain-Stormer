package brain.brainstormer.service;

import brain.brainstormer.utils.DatabaseConnection;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import org.bson.Document;
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
                .append("dateCreated", new Date());
        templatesCollection.insertOne(template);
    }
}
