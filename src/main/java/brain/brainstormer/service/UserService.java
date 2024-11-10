package brain.brainstormer.service;

import brain.brainstormer.utils.DatabaseConnection;
import com.mongodb.client.MongoCollection;
import org.bson.Document;
import org.mindrot.jbcrypt.BCrypt;
import com.mongodb.client.model.Filters;

public class UserService {

    private final MongoCollection<Document> usersCollection;

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
            return BCrypt.checkpw(password, storedHash);  // Returns true if passwords match
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
}
