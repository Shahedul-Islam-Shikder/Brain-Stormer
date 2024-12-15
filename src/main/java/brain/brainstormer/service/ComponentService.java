package brain.brainstormer.service;

import brain.brainstormer.components.core.CoreComponent;
import brain.brainstormer.socket.Socket;
import brain.brainstormer.utils.DatabaseConnection;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import com.mongodb.client.result.UpdateResult;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.Date;
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

    public void deleteComponent(String templateId, String componentId) {
        try {
            // Step 1: Attempt a surface-level delete
            long deletedCount = templatesCollection.updateOne(
                    Filters.eq("_id", new ObjectId(templateId)),
                    Updates.pull("components", new Document("_id", componentId))
            ).getModifiedCount();

            // Step 2: If no surface-level delete occurred, attempt a nested delete
            if (deletedCount == 0) {
                templatesCollection.updateOne(
                        Filters.eq("_id", new ObjectId(templateId)), // Match the template by ID
                        Updates.set("components", deleteNestedComponent(
                                templatesCollection.find(Filters.eq("_id", new ObjectId(templateId)))
                                        .first()
                                        .getList("components", Document.class), // Retrieve the components list
                                componentId
                        ))
                );
            }

            System.out.println("Component deleted successfully: " + componentId);
        } catch (Exception e) {
            System.err.println("Failed to delete component: " + e.getMessage());
        }

        // Notify via WebSocket
        Socket.sendWebSocketUpdate();
    }

    private List<Document> deleteNestedComponent(List<Document> components, String componentId) {
        List<Document> updatedComponents = new ArrayList<>();

        for (Document component : components) {
            if (componentId.equals(component.getString("_id"))) {
                // Skip this component to "delete" it
                continue;
            } else if (component.containsKey("children")) {
                // Recursively delete from children
                List<Document> updatedChildren = deleteNestedComponent(
                        component.getList("children", Document.class),
                        componentId
                );
                component.put("children", updatedChildren);
            }
            updatedComponents.add(component);
        }

        return updatedComponents;
    }

    public void editComponents(String templateId, String componentId, Document updatedData) {
        try {
            // Step 1: Attempt a surface-level update
            long updatedCount = templatesCollection.updateOne(
                    Filters.and(
                            Filters.eq("_id", new ObjectId(templateId)), // Match the template
                            Filters.eq("components._id", componentId)    // Match the component by ID
                    ),
                    Updates.combine(
                            Updates.set("components.$.config", updatedData.get("config")),
                            Updates.set("components.$.lastUpdated", new Date())
                    )
            ).getModifiedCount();

            // Step 2: If no surface-level update occurred, attempt a nested update
            if (updatedCount == 0) {
                templatesCollection.updateOne(
                        Filters.eq("_id", new ObjectId(templateId)), // Match the template by ID
                        Updates.set("components", updateNestedComponent(
                                templatesCollection.find(Filters.eq("_id", new ObjectId(templateId)))
                                        .first()
                                        .getList("components", Document.class), // Retrieve the components list
                                componentId,
                                updatedData
                        ))
                );
            }

            System.out.println("Component updated successfully: " + componentId);
        } catch (Exception e) {
            System.err.println("Failed to edit component: " + e.getMessage());
        }

        // Notify via WebSocket
        Socket.sendWebSocketUpdate();
    }

    private List<Document> updateNestedComponent(List<Document> components, String componentId, Document updatedComponent) {
        for (Document component : components) {
            if (componentId.equals(component.getString("_id"))) {
                // Update the component directly
                component.put("config", updatedComponent.get("config"));
                component.put("lastUpdated", new Date());
                return components;
            } else if (component.containsKey("children")) {
                // Recursively update children
                List<Document> updatedChildren = updateNestedComponent(
                        component.getList("children", Document.class),
                        componentId,
                        updatedComponent
                );
                component.put("children", updatedChildren);
            }
        }
        return components;
    }




    public MongoCollection<Document> getComponentsCollection() {
        return componentsCollection;
    }
}

