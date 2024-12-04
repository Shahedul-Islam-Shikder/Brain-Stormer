package brain.brainstormer.utils;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoException;
import com.mongodb.ServerApi;
import com.mongodb.ServerApiVersion;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import io.github.cdimascio.dotenv.Dotenv;
import org.bson.Document;

public class DatabaseConnection {

    private static DatabaseConnection instance;
    private MongoClient mongoClient;
    private MongoDatabase database;

    private DatabaseConnection() {
        // Load environment variables
        Dotenv dotenv = Dotenv.load();
        String connectionString = dotenv.get("MONGO_CONNECTION_STRING");

        // Configure MongoDB settings with ServerApi
        ServerApi serverApi = ServerApi.builder()
                .version(ServerApiVersion.V1)
                .build();

        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(new ConnectionString(connectionString))
                .serverApi(serverApi)
                .build();

        mongoClient = MongoClients.create(settings);
        database = mongoClient.getDatabase("BrainStormer");

        // Optional: Ping the database to verify the connection
        try {
            database.runCommand(new Document("ping", 1));
            System.out.println("Successfully connected to MongoDB!");
        } catch (MongoException e) {
            System.err.println("Failed to connect to MongoDB: " + e.getMessage());
        }
    }

    // Singleton instance accessor
    public static DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    public MongoDatabase getDatabase() {
        return database;
    }

    public void close() {
        if (mongoClient != null) {
            mongoClient.close();
        }
    }
}
