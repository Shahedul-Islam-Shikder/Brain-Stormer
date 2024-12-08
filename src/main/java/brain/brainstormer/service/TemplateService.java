package brain.brainstormer.service;

import brain.brainstormer.utils.DatabaseConnection;
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
        ObjectId userObjectId = new ObjectId(userId); // Convert userId to ObjectId

        // Combine queries for user-owned templates and shared public templates
        return templatesCollection.find(Filters.or(
                Filters.eq("userId", userObjectId), // User-owned templates (public and private)
                Filters.and(
                        Filters.eq("type", "public"), // Public templates
                        Filters.or(
                                Filters.eq("editors", userObjectId), // User is an editor
                                Filters.eq("viewers", userObjectId)  // User is a viewer
                        )
                )
        )).into(new ArrayList<>());
    }






    // Add a new template
// Add a new template
    public void addTemplate(String userId, String name, String description, String type) {
        ObjectId userObjectId = new ObjectId(userId); // Convert userId to ObjectId

        Document template = new Document("userId", userObjectId) // Store as ObjectId
                .append("name", name)
                .append("description", description)
                .append("type", type) // public or private
                .append("editors", new ArrayList<>()) // Initially no editors
                .append("viewers", new ArrayList<>()) // Initially no viewers
                .append("chatMessages", new ArrayList<>()) // Initialize chat messages as an empty list
                .append("dateCreated", new Date())
                .append("components", new ArrayList<>()); // Initialize components as an empty list

        templatesCollection.insertOne(template);
    }



    // Fetch a template by its ID
    public Document getTemplateById(String templateId) {
        try {
            return templatesCollection.find(Filters.eq("_id", new ObjectId(templateId))).first();
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid template ID format: " + templateId);
            return null;
        }
    }

    // Delete a template by its ID
    public void deleteTemplate(String templateId) {
        System.out.println("Attempting to delete template with ID: " + templateId);
        templatesCollection.deleteOne(Filters.eq("_id", new ObjectId(templateId)));
    }

    // Update a template's name, description, and type
    public void updateTemplate(String templateId, String name, String description, String type) {
        System.out.println("Attempting to update template with ID: " + templateId);
        templatesCollection.updateOne(
                Filters.eq("_id", new ObjectId(templateId)),
                Updates.combine(
                        Updates.set("name", name),
                        Updates.set("description", description),
                        Updates.set("type", type)
                )
        );
    }


    // Update a specific component in a template
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



    // Add a user as an Editor
    public void addEditor(String templateId, String editorId) {
        templatesCollection.updateOne(
                Filters.eq("_id", new ObjectId(templateId)),
                Updates.addToSet("editors", new ObjectId(editorId))
        );
        System.out.println("Added user as Editor: " + editorId);
    }

    // Remove a user from Editors
    public void removeEditor(String templateId, String editorId) {
        templatesCollection.updateOne(
                Filters.eq("_id", new ObjectId(templateId)),
                Updates.pull("editors", new ObjectId(editorId))
        );
        System.out.println("Removed user from Editors: " + editorId);
    }

    // Add a user as a Viewer
    public void addViewer(String templateId, String viewerId) {
        templatesCollection.updateOne(
                Filters.eq("_id", new ObjectId(templateId)),
                Updates.addToSet("viewers", new ObjectId(viewerId))
        );
        System.out.println("Added user as Viewer: " + viewerId);
    }

    // Remove a user from Viewers
    public void removeViewer(String templateId, String viewerId) {
        templatesCollection.updateOne(
                Filters.eq("_id", new ObjectId(templateId)),
                Updates.pull("viewers", new ObjectId(viewerId))
        );
        System.out.println("Removed user from Viewers: " + viewerId);
    }

    // Fetch chat messages for a template
    public List<Document> getChatMessages(String templateId) {
        try {
            Document template = templatesCollection.find(Filters.eq("_id", new ObjectId(templateId))).first();
            if (template != null) {
                return template.getList("chatMessages", Document.class);
            }
        } catch (Exception e) {
            System.err.println("Failed to fetch chat messages: " + e.getMessage());
        }
        return new ArrayList<>(); // Return empty list if no messages found or error occurs
    }

    // Add a new chat message to a template
    public void addChatMessage(String templateId, String username, String message) {
        try {
            Document chatMessage = new Document("user", username)
                    .append("text", message)
                    .append("timestamp", new Date());

            templatesCollection.updateOne(
                    Filters.eq("_id", new ObjectId(templateId)),
                    Updates.push("chatMessages", chatMessage)
            );
            System.out.println("Chat message added for template: " + templateId);
        } catch (Exception e) {
            System.err.println("Failed to add chat message: " + e.getMessage());
        }
    }
}
