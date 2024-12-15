package brain.brainstormer.service;

import brain.brainstormer.config.DatabaseConnection;
import brain.brainstormer.config.SessionManager;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Updates;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.mindrot.jbcrypt.BCrypt;
import com.mongodb.client.model.Filters;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class UserService {

    private final MongoCollection<Document> usersCollection;
    private final SessionManager sessionManager = SessionManager.getInstance();

    public UserService() {
        // Retrieve the "users" collection from the MongoDB database
        usersCollection = DatabaseConnection.getInstance().getDatabase().getCollection("users");
    }

    // Register a new user with name, username, email, and hashed password
    public boolean registerUser(String name, String username, String email, String password) {
        // Check if the username or email already exists
        if (isUsernameOrEmailTaken(username, email)) {
            //!TODO: make popUP
            System.out.println("Username or email already taken.");
            return false;  // Registration failed due to duplicate username or email
        }

        // Hash the password using bcrypt
        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());

        // Create a MongoDB document with the user's information
        Document user = new Document("name", name)
                .append("username", username)
                .append("email", email)
                .append("password", hashedPassword);

        // Insert the document into the "users" collection
        usersCollection.insertOne(user);
        return true;  // Registration successful
    }

    // Authenticate user by verifying password
    public boolean loginUser(String username, String password) {
        // Find the user document by username
        Document user = usersCollection.find(new Document("username", username)).first();

        // If user is found, compare the password provided with the stored hashed password
        if (user != null) {
            String storedHash = user.getString("password");
            if( BCrypt.checkpw(password, storedHash)){

                // Set the User ID and everythin else in the session manager
                sessionManager.setUserId(user.getObjectId("_id").toString());
                sessionManager.setUsername(user.getString("username"));
                sessionManager.setEmail(user.getString("email"));





                return true;  // Returns true if password matches

            }
        }
        return false;  // Returns false if user is not found or password doesn't match
    }

    // Check if username or email is already in use
    private boolean isUsernameOrEmailTaken(String username, String email) {
        // Search for a user with the same username or email
        Document existingUser = usersCollection.find(
                Filters.or(Filters.eq("username", username), Filters.eq("email", email))
        ).first();

        // Returns true if a matching user is found, indicating username or email is taken
        return existingUser != null;
    }


    // Add a template reference to a user's roles
    public void addTemplateToUser(String userId, String templateId, String role) {
        if (!List.of("author", "editor", "viewer").contains(role)) {
            throw new IllegalArgumentException("Invalid role: " + role);
        }
        usersCollection.updateOne(
                Filters.eq("_id", new ObjectId(userId)),
                Updates.addToSet("roles." + role, new ObjectId(templateId))
        );
        System.out.println("Template added to user under role: " + role);
    }

    // Remove a template reference from a user's roles
    public void removeTemplateFromUser(String userId, String templateId, String role) {
        usersCollection.updateOne(
                Filters.eq("_id", new ObjectId(userId)),
                Updates.pull("roles." + role, new ObjectId(templateId))
        );
        System.out.println("Template removed from user under role: " + role);
    }

    // Retrieve user details with roles
    public Document getUser(String userId) {
        return usersCollection.find(Filters.eq("_id", new ObjectId(userId))).first();
    }
    public List<Document> getAllUsers() {
        try {
            return usersCollection.find().into(new ArrayList<>());
        } catch (Exception e) {
            System.err.println("Failed to fetch users: " + e.getMessage());
            return Collections.emptyList();
        }
    }

}
